package org.myeslib.infra;

import org.myeslib.core.Command;
import org.myeslib.core.CommandId;
import org.myeslib.data.UnitOfWork;

public interface UnitOfWorkJournal<K> {

    void append(K targetId, CommandId commandId, Command command, UnitOfWork unitOfWork);

}