package org.myeslib.infra;

import org.myeslib.core.AggregateRoot;
import org.myeslib.data.Event;

import java.util.List;
import java.util.function.BiFunction;

public interface ApplyEventsFunction<A extends AggregateRoot> extends BiFunction<A, List<Event>, A> {

}
