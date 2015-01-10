package org.myeslib.jdbi.storage;

import org.myeslib.data.UnitOfWork;
import org.myeslib.jdbi.storage.dao.UnitOfWorkDao;
import org.myeslib.storage.UnitOfWorkJournal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

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
