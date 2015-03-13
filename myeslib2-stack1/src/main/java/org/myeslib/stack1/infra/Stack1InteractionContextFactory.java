package org.myeslib.stack1.infra;

import org.myeslib.core.AggregateRoot;
import org.myeslib.infra.InteractionContext;
import org.myeslib.infra.InteractionContextFactory;

public class Stack1InteractionContextFactory<A extends AggregateRoot> implements InteractionContextFactory<A> {
    @Override
    public InteractionContext apply(A a) {
        return new MultiMethodInteractionContext(a);
    }
}
