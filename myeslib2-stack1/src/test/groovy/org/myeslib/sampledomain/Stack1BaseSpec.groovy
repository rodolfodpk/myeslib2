package org.myeslib.sampledomain

import com.google.inject.Inject
import com.google.inject.Injector
import org.myeslib.data.Event
import org.myeslib.data.UnitOfWork
import org.myeslib.stack1.infra.dao.UnitOfWorkDao
import org.myeslib.stack1.infra.helpers.DatabaseHelper
import spock.lang.Specification

abstract class Stack1BaseSpec<K>  extends Specification {

    static Injector injector;

    @Inject
    UnitOfWorkDao<K> unitOfWorkDao;

    protected abstract commandBus()

    protected <C> C command(C cmd) {
        commandBus().post(cmd)
        return C
    }

    protected List<Event> lastCmdEvents(K id) {
        unitOfWorkDao.getFull(id).last().events
    }

    protected List<Event> allEvents(K id) {
        flatMap(unitOfWorkDao.getFull(id))
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

    def setup() {
        injector.injectMembers(this);
        injector.getInstance(DatabaseHelper.class).initDb();
    }
}
