package org.myeslib.function;

import org.myeslib.core.Event;

import java.util.List;

public interface InteractionContext {

    void apply(Event event);

    List<Event> getEvents();
}
