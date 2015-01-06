package org.myeslib.core.data.test;

import org.myeslib.core.Event;

import java.util.UUID;

@SuppressWarnings("serial")
public class EventJustForTest implements Event {

    private final UUID id;
    private final int something;

    public EventJustForTest(UUID id, int something) {
        this.id = id;
        this.something = something;
    }

    public UUID getId() {
        return id;
    }

    public int getSomething() {
        return something;
    }

}
