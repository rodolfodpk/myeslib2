package org.myeslib.jdbi.infra.helpers.factories;

import org.myeslib.jdbi.data.JdbiKryoSnapshot;
import org.myeslib.sampledomain.aggregates.inventoryitem.InventoryItem;

public interface InventoryItemSnapshotFactory {

    JdbiKryoSnapshot<InventoryItem> create(InventoryItem instance, Long version);

}
