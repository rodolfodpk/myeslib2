package org.myeslib.stack1.infra;

import org.junit.Test;
import org.myeslib.infra.Snapshot;
import sampledomain.aggregates.inventoryitem.InventoryItem;

import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class Stack1SnapshotTest {

    final Supplier<InventoryItem> supplier = () -> InventoryItem.builder().build();
    final Function<InventoryItem, InventoryItem> injectFunction = item -> item;

    @Test
    public void shouldHaveAggregateRootInteractionContext() throws Exception {
        // TODO
    }

    @Test
    public void shouldHaveSagaInteractionContext() throws Exception {
        // TODO
    }

    @Test
    public void getAggregateInstance() throws Exception {
        final InventoryItem item = create(UUID.randomUUID());
        final Snapshot<InventoryItem> snapshot = new Stack1Snapshot<>(item, 1L, supplier, injectFunction);
        assertThat(snapshot.getAggregateInstance(), is(item));

    }

    @Test
    public void getVersion() throws Exception {
        final InventoryItem item = create(UUID.randomUUID());
        final Snapshot<InventoryItem> snapshot = new Stack1Snapshot<>(item, 1L, supplier, injectFunction);
        assertThat(snapshot.getVersion(), is(1L));
    }

    @Test
    public void equals() throws Exception {
        final InventoryItem item = create(UUID.randomUUID());
        final Snapshot<InventoryItem> snapshot = new Stack1Snapshot<>(item, 1L, supplier, injectFunction);
        assertThat(snapshot.equals(new Stack1Snapshot<>(item, 1L, supplier, injectFunction)), is(true));
    }

    @Test
    public void mutatingAggregateDoesNotAffectSnapshot() throws Exception {
        final UUID uuid = UUID.randomUUID();
        final InventoryItem item = create(uuid);
        final InventoryItem identicalItem = create(uuid);
        final Snapshot<InventoryItem> snapshot = new Stack1Snapshot<>(item, 0L, supplier, injectFunction);
        assertThat(snapshot.getAggregateInstance(), is(item));
        final InventoryItem itemFromSnapshot = snapshot.getAggregateInstance();
        itemFromSnapshot.setDescription("notAnymore");
        assertThat(snapshot.getAggregateInstance(), is(identicalItem));
    }

    InventoryItem create(UUID uuid) {
        final InventoryItem item = InventoryItem.builder().build();
        item.setId(uuid);
        item.setDescription("item4test");
        item.setAvailable(10);
        return item;
    }
}