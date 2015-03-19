package org.myeslib.stack1;

import org.myeslib.core.EventSourced;
import org.myeslib.data.Event;

import java.util.List;
import java.util.function.BiFunction;

@Deprecated
public class EventBusApplyEventsFunction<A extends EventSourced> implements BiFunction<A, List<Event>, A> {

    @Override
    public A apply(A a, List<Event> events) {
        _applyEventsOn(a, events);
        return a;
    }

    private void _applyEventsOn(final EventSourced instance, final List<? extends Event> events) {
        EventBusInteractionContext bus = new EventBusInteractionContext(instance);
        events.forEach(bus::emit);
    }

}
