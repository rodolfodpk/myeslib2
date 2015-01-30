package org.myeslib.jdbi.storage.dao;

import org.myeslib.core.Command;
import org.myeslib.data.CommandResults;
import org.myeslib.data.UnitOfWork;
import org.myeslib.data.UnitOfWorkHistory;

import java.util.List;

public interface UnitOfWorkDao<K> {

    UnitOfWorkHistory getFull(K id);

    UnitOfWorkHistory getPartial(K id, Long biggerThanThisVersion);

    void append(CommandResults<K> commandResults);

    Command<K> getCommand(K commandId);
}
