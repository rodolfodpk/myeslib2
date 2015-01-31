package org.myeslib.storage;

import org.myeslib.core.Command;
import org.myeslib.data.UnitOfWork;

import java.util.UUID;

public interface UnitOfWorkJournal<K> {

    void append(K targetId, UUID commandId, Command command, UnitOfWork unitOfWork);

}