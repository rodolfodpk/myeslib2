package org.myeslib.jdbi.storage.dao;

import org.myeslib.data.CommandResults;
import org.myeslib.data.UnitOfWork;

import java.util.List;

public interface UnitOfWorkDao<K> {

    List<UnitOfWork> getFull(K id);

    List<UnitOfWork> getPartial(K id, Long biggerThanThisVersion);

    void append(CommandResults<K> commandResults);

    CommandResults<K> getCommandResults(K commandId);
}
