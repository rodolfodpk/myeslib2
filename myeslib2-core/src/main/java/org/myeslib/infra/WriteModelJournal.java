package org.myeslib.infra;

import org.myeslib.data.Command;
import org.myeslib.data.CommandId;
import org.myeslib.data.UnitOfWork;

public interface WriteModelJournal<K> {

    void append(K targetId, CommandId commandId, Command command, UnitOfWork unitOfWork);

}