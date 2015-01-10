package org.myeslib.function;

import org.myeslib.core.AggregateRoot;
import org.myeslib.core.Event;

// experimental
public interface EventHandler<A extends AggregateRoot, E extends Event> {

    A handle(E event);

}
