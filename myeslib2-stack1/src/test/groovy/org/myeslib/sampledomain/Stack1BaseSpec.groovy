package org.myeslib.sampledomain

import org.myeslib.core.Event
import spock.lang.Specification

class Stack1BaseSpec  extends Specification {


    protected <C> C command(C cmd) {
        commandBus.post(cmd)
        return C
    }

    protected  <K> List<Event> lastCmdEvents(K id) {
        unitOfWorkDao.getFull(id).get(1).events
    }
}
