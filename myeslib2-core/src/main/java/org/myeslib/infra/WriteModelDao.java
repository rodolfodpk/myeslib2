package org.myeslib.infra;

import org.myeslib.core.EventSourced;
import org.myeslib.data.Command;
import org.myeslib.data.CommandId;
import org.myeslib.data.UnitOfWork;

import java.util.List;

public interface WriteModelDao<K, E extends EventSourced> {

    List<UnitOfWork> getFull(K id);

    List<UnitOfWork> getPartial(K id, Long biggerThanThisVersion);

    void append(K targetId, Command command, UnitOfWork unitOfWork);

    Command getCommand(CommandId commandId);
}
