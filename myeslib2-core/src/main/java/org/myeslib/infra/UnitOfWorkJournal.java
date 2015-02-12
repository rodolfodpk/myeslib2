package org.myeslib.infra;

import org.myeslib.data.Command;
import org.myeslib.data.CommandId;
import org.myeslib.data.UnitOfWork;

public interface UnitOfWorkJournal<K> {

    void append(K targetId, CommandId commandId, Command command, UnitOfWork unitOfWork);

}