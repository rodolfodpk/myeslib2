package org.myeslib.stack1.infra;

import org.myeslib.core.AggregateRoot;
import org.myeslib.core.Event;
import org.myeslib.infra.ApplyEventsFunction;

import java.util.List;

@Deprecated
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
