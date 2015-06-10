package org.myeslib.stack1.infra;

import org.myeslib.core.EventSourced;
import org.myeslib.data.Command;
import org.myeslib.data.EventMessage;
import org.myeslib.data.EventMessageId;
import org.myeslib.data.UnitOfWork;
import org.myeslib.infra.Consumers;
import org.myeslib.infra.WriteModelDao;
import org.myeslib.infra.WriteModelJournal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class Stack1Journal<K, E extends EventSourced> implements WriteModelJournal<K, E> {

    static final Logger logger = LoggerFactory.getLogger(Stack1Journal.class);

    private final WriteModelDao<K, E> dao;
    private final Consumers<E> consumers;

    @Inject
    public Stack1Journal(WriteModelDao<K, E> dao, Consumers<E> consumers) {
        this.dao = dao;
        this.consumers = consumers;
    }

    @Override
    public void append(K targetId, Command command, UnitOfWork unitOfWork) {
        checkNotNull(targetId);
        checkNotNull(command);
        checkNotNull(unitOfWork);
        checkArgument(unitOfWork.getCommandId().equals(command.getCommandId()));
        dao.append(targetId, command, unitOfWork);
        final List<EventMessage> eventMessages = unitOfWork.getEvents().stream()
                    .map((event) -> new EventMessage(EventMessageId.create(), event, targetId.toString(), unitOfWork.getVersion()))
                    .collect(Collectors.toList());
        for (Consumer<List<EventMessage>> consumer : consumers.eventMessageConsumers()) {
            consumer.accept(eventMessages);
        }
    }

}
