package org.myeslib.stack1.infra.helpers.factories;

import org.myeslib.stack1.data.Stack1KryoSnapshot;
import org.myeslib.sampledomain.aggregates.inventoryitem.InventoryItem;

public interface InventoryItemSnapshotFactory {

    /*
    According to https://github.com/google/guice/wiki/AssistedInject , returning a Snapshot<InventoryItem> should work.
     */
    Stack1KryoSnapshot<InventoryItem> create(InventoryItem instance, Long version);

}
