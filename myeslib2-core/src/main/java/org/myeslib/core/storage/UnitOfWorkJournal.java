package org.myeslib.core.storage;

import org.myeslib.core.data.UnitOfWork;

import java.util.List;

public interface UnitOfWorkJournal<K> {

    void append(final K id, final UnitOfWork uow);

    void appendBatch(final K id, final List<UnitOfWork> uowList);

}