package org.myeslib.storage;

import org.myeslib.core.Command;
import org.myeslib.data.UnitOfWork;

public interface UnitOfWorkJournal<K> {

    void append(Command<K> command, UnitOfWork unitOfWork);

}