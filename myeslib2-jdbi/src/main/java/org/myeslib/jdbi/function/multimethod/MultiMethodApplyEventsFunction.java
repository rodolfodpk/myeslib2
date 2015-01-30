package org.myeslib.jdbi.function.multimethod;

import org.myeslib.core.AggregateRoot;
import org.myeslib.core.Event;
import org.myeslib.function.ApplyEventsFunction;

import java.util.List;

public class MultiMethodApplyEventsFunction<A extends AggregateRoot> implements ApplyEventsFunction<A> {

    @Override
    public A apply(A a, List<Event> events) {
        _applyEventsOn(a, events);
        return a;
    }

    private void _applyEventsOn(AggregateRoot instance, List<? extends Event> events) {
        MultiMethod mm = MultiMethod.getMultiMethod(instance.getClass(), "on");
        for (Event event : events) {
            try {
                mm.invoke(instance, event);
            } catch (Exception e) {
                throw new RuntimeException("Error when executing with reflection", e.getCause());
            }
        }
    }


}
