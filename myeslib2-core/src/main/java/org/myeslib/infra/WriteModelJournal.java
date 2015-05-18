package org.myeslib.infra;

import org.myeslib.core.EventSourced;
import org.myeslib.data.Command;
import org.myeslib.data.UnitOfWork;

public interface WriteModelJournal<K, E extends EventSourced> {

    void append(K targetId, Command command, UnitOfWork unitOfWork);

//    void append(Saga saga, K targetId, Command command, UnitOfWork unitOfWork);

//    void append(AggregateRoot ar, K targetId, Command command, UnitOfWork unitOfWork);

}