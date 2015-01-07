package org.myeslib.storage.jdbi;

import org.myeslib.core.data.UnitOfWork;
import org.myeslib.core.storage.UnitOfWorkJournal;
import org.myeslib.storage.jdbi.dao.UnitOfWorkDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

public class JdbiUuidUnitOfWorkJournal implements UnitOfWorkJournal<UUID> {

    static final Logger logger = LoggerFactory.getLogger(JdbiUuidUnitOfWorkJournal.class);

    private final UnitOfWorkDao<UUID> dao;

    public JdbiUuidUnitOfWorkJournal(UnitOfWorkDao<UUID> dao) {
        checkNotNull(dao);
        this.dao = dao;
    }

    @Override
    public void append(final UUID id, final UnitOfWork uow) {
        dao.append(id, uow);
    }

    @Override public void appendBatch(UUID id, List<UnitOfWork> uowList) {
        dao.appendBatch(id, uowList);
    }

}
