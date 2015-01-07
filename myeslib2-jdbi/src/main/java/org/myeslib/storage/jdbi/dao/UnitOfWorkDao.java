package org.myeslib.storage.jdbi.dao;

import org.myeslib.core.data.UnitOfWork;
import org.myeslib.core.data.UnitOfWorkHistory;
import org.skife.jdbi.v2.Handle;

import java.util.List;
import java.util.UUID;

public interface UnitOfWorkDao<K> {

    UnitOfWorkHistory getFull(K id);

    UnitOfWorkHistory getPartial(K id, Long biggerThanThisVersion);

    void append(K id, UnitOfWork uow);

    void appendBatch(K id, UnitOfWork... uow);

    default void appendBatch(K id, List<UnitOfWork> uowList) {
        appendBatch(id, uowList.toArray(new UnitOfWork[uowList.size()]));
    }

}
