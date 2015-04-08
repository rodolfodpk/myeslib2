package org.myeslib.sampledomain

import org.myeslib.core.EventSourced
import org.myeslib.data.Event
import org.myeslib.data.UnitOfWork
import org.myeslib.infra.WriteModelDao
import org.myeslib.infra.commandbus.CommandBus
import spock.lang.Specification


public abstract class Stack1BaseSpec<K, E extends EventSourced> extends Specification {

    protected abstract CommandBus getCommandBus()

    protected abstract WriteModelDao<K, E> getUnitOfWorkDao()

    protected <C> C command(C cmd) {
        getCommandBus().post(cmd)
        return C
    }

    protected List<Event> lastCmdEvents(K id) {
        def unitOfWorkList = getUnitOfWorkDao().getFull(id)
        // println(" *** " + unitOfWorkList)
        def lastUnitOfWork = unitOfWorkList.last()
        def events = lastUnitOfWork.events
        events as List<Event>
    }

    protected List<Event> allEvents(K id) {
        def events = flatMap(getUnitOfWorkDao().getFull(id))
        // println(" *** " + events)
        events as List<Event>
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
