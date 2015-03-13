package org.myeslib.stack1.infra;

import org.myeslib.core.AggregateRoot;
import org.myeslib.infra.SagaInteractionContext;
import org.myeslib.infra.SagaInteractionContextFactory;

public class Stack1SagaInteractionContextFactory<A extends AggregateRoot> implements SagaInteractionContextFactory<A> {
    @Override
    public SagaInteractionContext apply(A a) {
        return new MultiMethodSagaInteractionContext(a);
    }
}
