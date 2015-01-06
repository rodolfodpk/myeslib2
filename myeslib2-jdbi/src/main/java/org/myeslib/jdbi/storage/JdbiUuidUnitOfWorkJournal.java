package org.myeslib.jdbi.storage;

import org.myeslib.core.AggregateRoot;
import org.myeslib.core.data.UnitOfWork;
import org.myeslib.core.storage.UnitOfWorkJournal;
import org.myeslib.jdbi.storage.dao.UnitOfWorkDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

public class JdbiUuidUnitOfWorkJournal<A extends AggregateRoot> implements UnitOfWorkJournal<UUID> {

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

}
