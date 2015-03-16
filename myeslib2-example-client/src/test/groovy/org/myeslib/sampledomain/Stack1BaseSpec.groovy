package org.myeslib.sampledomain

import com.google.common.eventbus.EventBus
import org.myeslib.data.Event
import org.myeslib.data.UnitOfWork
import org.myeslib.infra.UnitOfWorkDao
import spock.lang.Specification


public abstract class Stack1BaseSpec<K> extends Specification {

    protected abstract EventBus getCommandBus()

    protected abstract UnitOfWorkDao<K> getUnitOfWorkDao()

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
