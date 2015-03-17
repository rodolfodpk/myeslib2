package org.myeslib.sampledomain.aggregates.inventoryitem;

import org.myeslib.core.Saga;
import org.myeslib.infra.SagaInteractionContext;

public class Saga4test implements Saga {

    @Override
    public SagaInteractionContext getInteractionContext() {
        return null;
    }

    @Override
    public void setInteractionContext(SagaInteractionContext interactionContext) {

    }
}
