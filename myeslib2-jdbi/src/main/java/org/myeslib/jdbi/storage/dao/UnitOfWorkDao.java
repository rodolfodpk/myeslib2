package org.myeslib.jdbi.storage.dao;

import org.myeslib.core.data.UnitOfWork;
import org.myeslib.core.data.UnitOfWorkHistory;

public interface UnitOfWorkDao<K> {

    UnitOfWorkHistory get(K id);

    UnitOfWorkHistory getPartial(K id, Long biggerThanThisVersion);

    void append(K id, UnitOfWork uow);

}
