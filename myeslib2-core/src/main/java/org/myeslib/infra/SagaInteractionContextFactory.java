package org.myeslib.infra;

import org.myeslib.core.AggregateRoot;

import java.util.function.Function;

public interface SagaInteractionContextFactory<A  extends AggregateRoot> extends Function<A, SagaInteractionContext> {
}
