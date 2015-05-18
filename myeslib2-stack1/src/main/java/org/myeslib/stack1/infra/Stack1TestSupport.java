package org.myeslib.stack1.infra;

import org.myeslib.core.EventSourced;
import org.myeslib.data.Command;
import org.myeslib.data.Event;
import org.myeslib.data.UnitOfWork;
import org.myeslib.infra.WriteModelDao;
import org.myeslib.infra.commandbus.CommandBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public abstract class Stack1TestSupport<K, E extends EventSourced> {

    static final Logger logger = LoggerFactory.getLogger(Stack1TestSupport.class);

    protected abstract CommandBus<E> getCommandBus();

    protected abstract WriteModelDao<K, E> getWriteModelDao();

    protected <C extends Command> C command(C cmd) {
        getCommandBus().post(cmd);
        return cmd;
    }

    protected List<Event> lastCmdEvents(K id) {
        List<UnitOfWork> unitOfWorkList = getWriteModelDao().getFull(id);
        if (unitOfWorkList.isEmpty()) {
            return new ArrayList<>();
        }
        UnitOfWork lastUnitOfWork = unitOfWorkList.stream().reduce((previous, current) -> current).get();
        return lastUnitOfWork.getEvents();
    }

    protected List<Event> allEvents(K id) {
        return flatMap(getWriteModelDao().getFull(id));
    }

    List<Event> flatMap(final List<UnitOfWork> unitOfWorks) {
        logger.debug("unitOfWorks -> {}", unitOfWorks);
        List<Event> events = new ArrayList<>();
        for (UnitOfWork uow : unitOfWorks) {
            for (Event event : uow.getEvents()) {
                events.add(event);
            }
        }
        return events;
    }

}
