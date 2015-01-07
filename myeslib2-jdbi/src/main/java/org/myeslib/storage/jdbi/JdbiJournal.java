package org.myeslib.storage.jdbi;

import org.myeslib.core.data.UnitOfWork;
import org.myeslib.core.storage.UnitOfWorkJournal;
import org.myeslib.storage.jdbi.dao.UnitOfWorkDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

public class JdbiJournal<K> implements UnitOfWorkJournal<K> {

    static final Logger logger = LoggerFactory.getLogger(JdbiJournal.class);

    private final UnitOfWorkDao<K> dao;

    public JdbiJournal(UnitOfWorkDao<K> dao) {
        checkNotNull(dao);
        this.dao = dao;
    }

    @Override
    public void append(final K id, final UnitOfWork uow) {
        dao.append(id, uow);
    }

    @Override public void appendBatch(K id, List<UnitOfWork> uowList) {
        dao.appendBatch(id, uowList);
    }

}
