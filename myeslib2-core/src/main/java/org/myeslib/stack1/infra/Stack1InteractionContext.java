package org.myeslib.stack1.infra;

import org.myeslib.core.EventSourced;
import org.myeslib.data.Event;
import org.myeslib.infra.InteractionContext;
import org.myeslib.infra.exceptions.ApplyEventsException;
import org.myeslib.stack1.infra.helpers.MultiMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static autovalue.shaded.com.google.common.common.base.Preconditions.checkNotNull;

public class Stack1InteractionContext implements InteractionContext {

    private static final Logger logger = LoggerFactory.getLogger(Stack1InteractionContext.class);
    private final EventSourced eventSourced;
    private final MultiMethod mm ;
    private final List<Event> events;

    public Stack1InteractionContext(EventSourced eventSourced) {
        this.mm = MultiMethod.getMultiMethod(eventSourced.getClass(), "on");
        this.events = new ArrayList<>();
        checkNotNull(eventSourced);
        this.eventSourced = eventSourced;
    }

    @Override
    public void emit(Event event) {
        checkNotNull(event);
        logger.debug("applying event {} generated by an operation on {}", event, eventSourced);
        _applyEventsOn(eventSourced, event);
        logger.debug("status after event applied {}", eventSourced);
        events.add(event);
    }

    @Override
    public List<Event> getEmittedEvents() {
        return Collections.unmodifiableList(events);
    }

    private void _applyEventsOn(EventSourced instance, Event event) {
        try {
            mm.invoke(instance, event);
        } catch (Exception e) {
            throw new ApplyEventsException(e.getCause());
        }
    }
}
