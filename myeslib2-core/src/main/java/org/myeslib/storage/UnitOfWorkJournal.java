package org.myeslib.storage;

import org.myeslib.data.UnitOfWork;

import java.util.List;

public interface UnitOfWorkJournal<K> {

    void append(final K id, final UnitOfWork uow);

    @Deprecated
    void appendBatch(final K id, final List<UnitOfWork> uowList);

}