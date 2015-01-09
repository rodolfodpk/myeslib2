package org.myeslib.core;

// candidate
public interface EventHandler<A extends AggregateRoot, E extends Event> {

    A handle(E event);

}
