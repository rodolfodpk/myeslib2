package org.myeslib.infra;

import org.myeslib.core.AggregateRoot;

import java.util.function.Function;

public interface InteractionContextFactory<A  extends AggregateRoot> extends Function<A, InteractionContext> {
}
