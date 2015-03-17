package org.myeslib.stack1.infra;

import org.myeslib.core.EventSourced;
import org.myeslib.data.Event;
import org.myeslib.infra.ApplyEventsFunction;
import org.myeslib.stack1.infra.helpers.MultiMethod;

import java.util.List;

public class Stack1ApplyEventsFunction<A extends EventSourced> implements ApplyEventsFunction<A> {

    @Override
    public A apply(A a, List<Event> events) {
        _applyEventsOn(a, events);
        return a;
    }

    private void _applyEventsOn(EventSourced instance, List<? extends Event> events) {
        MultiMethod mm = MultiMethod.getMultiMethod(instance.getClass(), "on");
        for (Event event : events) {
            try {
                mm.invoke(instance, event);
            } catch (Exception e) {
                throw new RuntimeException("Error when applying events via reflection", e.getCause());
            }
        }
    }

}
