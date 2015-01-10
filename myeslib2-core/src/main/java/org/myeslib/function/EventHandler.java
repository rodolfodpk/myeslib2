package org.myeslib.function;

import org.myeslib.core.AggregateRoot;
import org.myeslib.core.Event;

// candidate
public interface EventHandler<A extends AggregateRoot, E extends Event> {

    A handle(E event);

}
