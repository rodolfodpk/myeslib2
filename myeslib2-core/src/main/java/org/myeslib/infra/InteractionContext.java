package org.myeslib.infra;

import org.myeslib.data.Event;

import java.util.List;

public interface InteractionContext {

    void emit(Event event);

    List<Event> getEmittedEvents();

}
