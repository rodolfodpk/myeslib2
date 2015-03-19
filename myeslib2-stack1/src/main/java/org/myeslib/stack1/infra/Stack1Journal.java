package org.myeslib.stack1.infra;

import org.myeslib.data.*;
import org.myeslib.infra.WriteModelDao;
import org.myeslib.infra.WriteModelJournal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class Stack1Journal<K> implements WriteModelJournal<K> {

    static final Logger logger = LoggerFactory.getLogger(Stack1Journal.class);

    private final WriteModelDao<K> dao;
    private final List<Consumer<EventMessage>> consumers;

    @Inject
    public Stack1Journal(WriteModelDao<K> dao, List<Consumer<EventMessage>> consumers) {
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
        for (Consumer<EventMessage> consumer : consumers) {
            logger.debug("consumer.post {}", unitOfWork);
            for (Event event : unitOfWork.getEvents()) {
                consumer.accept(new EventMessage(EventMessageId.create(), event));
            }
        }
    }

}
