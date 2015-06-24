package org.myeslib.stack1.infra.dao;

import org.myeslib.core.EventSourced;
import org.myeslib.data.Command;
import org.myeslib.data.CommandId;
import org.myeslib.data.UnitOfWork;
import org.myeslib.infra.WriteModelDao;
import org.myeslib.infra.dao.config.CmdSerialization;
import org.myeslib.infra.dao.config.DbMetadata;
import org.myeslib.infra.dao.config.UowSerialization;
import org.myeslib.infra.exceptions.CommandExecutionException;
import org.myeslib.infra.exceptions.ConcurrencyException;
import org.myeslib.stack1.infra.helpers.jdbi.ClobToStringMapper;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.TransactionIsolationLevel;
import org.skife.jdbi.v2.tweak.HandleCallback;
import org.skife.jdbi.v2.tweak.ResultSetMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class Stack1JdbiDao<K, E extends EventSourced> implements WriteModelDao<K, E> {

    static final Logger logger = LoggerFactory.getLogger(Stack1JdbiDao.class);

    private final UowSerialization<E> uowSer;
    private final CmdSerialization<E> cmdSer;
    private final DbMetadata<E> dbMetadata;
    private final DBI dbi;

    @Inject
    public Stack1JdbiDao(UowSerialization<E> uowSer, CmdSerialization<E> cmdSer, DbMetadata<E> dbMetadata, DBI dbi) {
        this.uowSer = uowSer;
        this.cmdSer = cmdSer;
        this.dbMetadata = dbMetadata;
        this.dbi = dbi;
    }

    /*
     * (non-Javadoc)
     * @see org.myeslib.stack1.AggregateRootHistoryReader#getSnapshot(java.lang.Object)
     */
    @Override
    public List<UnitOfWork> getFull(final K id) {
        return getPartial(id, 0L);
    }

    /*
         * (non-Javadoc)
         * @see org.myeslib.stack1.AggregateRootHistoryReader#getPartial(java.lang.Object)
         */
    @Override
    public List<UnitOfWork> getPartial(K id, Long biggerThanThisVersion) {

        final List<UnitOfWork> arh = new ArrayList<>();

        logger.debug("will load {} from {}", id.toString(), dbMetadata.aggregateRootTable);

        List<UowRecord> unitsOfWork = dbi
                .withHandle(new HandleCallback<List<UowRecord>>() {

                                String sql = String.format("select id, version, uow_data, seq_number " +
                                        "from %s where id = :id " +
                                        " and version > :version " +
                                        "order by version", dbMetadata.unitOfWorkTable);

                                public List<UowRecord> withHandle(Handle h) {
                                    return h.createQuery(sql)
                                            .bind("id", id.toString())
                                            .bind("version", biggerThanThisVersion)
                                            .map(new UowRecordMapper()).list();
                                }
                            }
                );

        if (unitsOfWork == null) {
            logger.debug("found none unit of work for id {} and version > {} on {}", id.toString(), biggerThanThisVersion, dbMetadata.unitOfWorkTable);
            return new ArrayList<>();
        }

        logger.debug("found {} units of work for id {} and version > {} on {}", unitsOfWork.size(), id.toString(), biggerThanThisVersion, dbMetadata.unitOfWorkTable);
        for (UowRecord r : unitsOfWork) {
            logger.debug("converting to uow from {}", r.uowData);
            Function<String, UnitOfWork> f = uowSer.fromStringFunction;
            UnitOfWork uow = f.apply(r.uowData);
            logger.debug(uow.toString());
            arh.add(uow);
        }

        return Collections.unmodifiableList(arh);
    }

    @Override
    public void append(final K targetId, final Command command, final UnitOfWork unitOfWork) {

        checkNotNull(targetId);
        checkNotNull(command);
        checkNotNull(unitOfWork);
        checkArgument(unitOfWork.getCommandId().equals(command.getCommandId()));

        String insertUowSql = String.format("insert into %s (id, uow_data, version, inserted_on) values (:id, :uow_data, :version, :inserted_on)", dbMetadata.unitOfWorkTable);
        String insertCommandSql = String.format("insert into %s (id, cmd_data) values (:id, :cmd_data)", dbMetadata.commandTable);

        logger.debug(insertUowSql);

        logger.debug("appending uow to {} with id {}", dbMetadata.aggregateRootTable, targetId);

        try {
            dbi.inTransaction(TransactionIsolationLevel.READ_COMMITTED, (conn, status) -> {
                        int result1 = conn.createStatement(insertUowSql)
                                .bind("id", targetId.toString())
                                .bind("uow_data", uowSer.toStringFunction.apply(unitOfWork))
                                .bind("version", unitOfWork.getVersion())
                                .bind("inserted_on", unitOfWork.getCreatedOn())
                                .execute() ;
                        int result2 = conn.createStatement(insertCommandSql)
                                .bind("id", command.getCommandId().toString())
                                .bind("cmd_data", cmdSer.toStringFunction.apply(command))
                                .execute() ;
                        return result1 + result2 == 2;
                    }
            );
        } catch (Exception e) {
            CommandExecutionException exception = convertFrom(e);
            logger.error(exception.getMessage());
            throw exception;
        }

    }

    @Override
    public Command getCommand(final CommandId commandId) {
        return dbi
                .withHandle(new HandleCallback<Command>() {
                                final String sql = String.format("select id, cmd_data " +
                                        "from %s where id = :id ", dbMetadata.commandTable);
                                public Command withHandle(final Handle h) {
                                    return h.createQuery(sql)
                                            .bind("id", commandId.toString())
                                            .map((i, r, ctx) -> {
                                                final String cmdData = new ClobToStringMapper("cmd_data").map(i, r, ctx);
                                                final Command cmd = cmdSer.fromStringFunction.apply(cmdData);
                                                return cmd;
                                            }).first();
                                }
                            }
                );
    }

    private CommandExecutionException convertFrom(Exception e) {
        final String msg = e.getCause() != null ? e.getCause().getMessage() : "unable to append to database";
        if (msg !=null && msg.contains("does not match the last version")) {
            return new ConcurrencyException(msg);
        }
        return new CommandExecutionException(msg);
    }

    public static class UowRecord {
        final String id;
        final Long version;
        final String uowData;
        final Long seqNumber;

        public UowRecord(String id, Long version, String uowData, Long seqNumber) {
            this.id = id;
            this.version = version;
            this.uowData = uowData;
            this.seqNumber = seqNumber;
        }

    }

    public static class UowRecordMapper implements ResultSetMapper<UowRecord> {
        @Override
        public UowRecord map(int index, ResultSet r, StatementContext ctx)
                throws SQLException {
            String id = r.getString("id");
            Long version = r.getBigDecimal("version").longValue();
            String uowData = new ClobToStringMapper("uow_data").map(index, r, ctx);
            BigDecimal bdSeqNumber = r.getBigDecimal("seq_number");
            Long seqNumber = bdSeqNumber == null ? null : bdSeqNumber.longValue();
            return new UowRecord(id, version, uowData, seqNumber);
        }
    }

}
