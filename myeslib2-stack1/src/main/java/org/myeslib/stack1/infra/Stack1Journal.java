package org.myeslib.stack1.infra;

import com.google.common.eventbus.EventBus;
import org.myeslib.data.Command;
import org.myeslib.data.CommandId;
import org.myeslib.data.Event;
import org.myeslib.data.UnitOfWork;
import org.myeslib.infra.UnitOfWorkJournal;
import org.myeslib.stack1.infra.dao.UnitOfWorkDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

public class Stack1Journal<K> implements UnitOfWorkJournal<K> {

    static final Logger logger = LoggerFactory.getLogger(Stack1Journal.class);

    private final UnitOfWorkDao<K> dao;
    private final EventBus[] eventBuses;

    public Stack1Journal(UnitOfWorkDao<K> dao, EventBus... eventBuses) {
        checkNotNull(dao);
        this.dao = dao;
        checkNotNull(eventBuses);
        this.eventBuses = eventBuses;
    }

    public Stack1Journal(UnitOfWorkDao<K> dao) {
        checkNotNull(dao);
        this.dao = dao;
        this.eventBuses = new EventBus[]{};
    }

    @Override
    public void append(K targetId, CommandId commandId, Command command, UnitOfWork unitOfWork) {
        try {
            dao.append(targetId, commandId, command, unitOfWork);
            for (EventBus bus : eventBuses) {
                logger.debug("bus.post {}", unitOfWork);
                // unitOfWork instead of List<Event> in order to support impotency
                bus.post(unitOfWork);
            }
        } catch (Exception e) {
            throw e ;
        }
    }

}
