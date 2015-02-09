package org.myeslib.jdbi.infra.helpers.factories;

import org.myeslib.jdbi.data.JdbiKryoSnapshot;
import org.myeslib.sampledomain.aggregates.inventoryitem.InventoryItem;

public interface InventoryItemSnapshotFactory {

    /*
    According to https://github.com/google/guice/wiki/AssistedInject , returning a Snapshot<InventoryItem> should work.
     */
    JdbiKryoSnapshot<InventoryItem> create(InventoryItem instance, Long version);

}
