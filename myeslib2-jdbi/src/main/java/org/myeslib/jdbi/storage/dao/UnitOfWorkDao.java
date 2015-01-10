package org.myeslib.jdbi.storage.dao;

import org.myeslib.data.UnitOfWork;
import org.myeslib.data.UnitOfWorkHistory;

import java.util.List;

public interface UnitOfWorkDao<K> {

    UnitOfWorkHistory getFull(K id);

    UnitOfWorkHistory getPartial(K id, Long biggerThanThisVersion);

    void append(K id, UnitOfWork uow);

    void appendBatch(K id, UnitOfWork... uow);

    default void appendBatch(K id, List<UnitOfWork> uowList) {
        appendBatch(id, uowList.toArray(new UnitOfWork[uowList.size()]));
    }

}
