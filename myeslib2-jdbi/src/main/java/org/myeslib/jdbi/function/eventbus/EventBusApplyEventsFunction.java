package org.myeslib.jdbi.function.eventbus;

import org.myeslib.core.AggregateRoot;
import org.myeslib.core.Event;
import org.myeslib.function.ApplyEventsFunction;

import javax.annotation.concurrent.NotThreadSafe;
import java.util.List;

@NotThreadSafe
public class EventBusApplyEventsFunction<A extends AggregateRoot> implements ApplyEventsFunction<A> {

    @Override
    public A apply(A a, List<Event> events) {
        _applyEventsOn(a, events);
        return a;
    }

    private void _applyEventsOn(final AggregateRoot instance, final List<? extends Event> events) {
        EventBusInteractionContext bus = new EventBusInteractionContext(instance);
        events.forEach(bus::apply);
    }

}
