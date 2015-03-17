package org.myeslib.infra;

import org.myeslib.core.EventSourced;
import org.myeslib.data.Event;

import java.util.List;
import java.util.function.BiFunction;

public interface ApplyEventsFunction<A extends EventSourced> extends BiFunction<A, List<Event>, A> {

}
