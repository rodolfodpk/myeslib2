package org.myeslib.core.data.test;

import java.util.UUID;

import org.myeslib.core.Event;

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
