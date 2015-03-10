package org.myeslib.sampledomain

import com.google.common.eventbus.EventBus
import com.google.inject.Injector
import org.myeslib.data.Event
import org.myeslib.data.UnitOfWork
import org.myeslib.stack1.infra.dao.UnitOfWorkDao
import spock.lang.Specification

abstract class Stack1BaseSpec<K>  extends Specification {

    static Injector injector;

    protected abstract EventBus commandBus()

    protected abstract UnitOfWorkDao<K> unitOfWorkDao()

    protected <C> C command(C cmd) {
        commandBus().post(cmd)
        return C
    }

    protected List<Event> lastCmdEvents(K id) {
        def unitOfWorkList = unitOfWorkDao().getFull(id)
        def lastUnitOfWork = unitOfWorkList.last()
        def events = lastUnitOfWork.events
        events as List<Event>
    }

    protected List<Event> allEvents(K id) {
        flatMap(unitOfWorkDao().getFull(id))
    }

    List<Event> flatMap(final List<UnitOfWork> unitOfWorks) {
        List<Event> events = new ArrayList<>();
        for (UnitOfWork uow : unitOfWorks) {
            for (Event event : uow.events) {
                events.add(event)
            }
        }
        events
    }

}
