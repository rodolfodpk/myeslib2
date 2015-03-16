package org.myeslib.stack1.infra;

import org.apache.commons.beanutils.BeanUtils;
import org.junit.Test;
import org.myeslib.infra.Snapshot;
import org.myeslib.sampledomain.aggregates.inventoryitem.InventoryItem;
import org.myeslib.stack1.infra.Stack1Snapshot;

import java.util.UUID;
import java.util.function.Function;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class Stack1SnapshotTest {

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
        final InventoryItem item = create();
        final Snapshot<InventoryItem> snapshot = new Stack1Snapshot<>(item, 1L, injectFunction);
        assertThat(snapshot.getAggregateInstance(), is(item));

    }

    @Test
    public void getVersion() throws Exception {
        final InventoryItem item = create();
        final Snapshot<InventoryItem> snapshot = new Stack1Snapshot<>(item, 1L, injectFunction);
        assertThat(snapshot.getVersion(), is(1L));
    }

    @Test
    public void equals() throws Exception {
        final InventoryItem item = create();
        final Snapshot<InventoryItem> snapshot = new Stack1Snapshot<>(item, 1L, injectFunction);
        assertThat(snapshot.equals(new Stack1Snapshot<>(item, 1L, injectFunction)), is(true));
    }

    @Test
    public void mutatingAggregateDoesNotAffectSnapshot() throws Exception {
        final InventoryItem item = create();
        final InventoryItem identicalItem = (InventoryItem) BeanUtils.cloneBean(item);
        final Snapshot<InventoryItem> snapshot = new Stack1Snapshot<>(item, 0L, injectFunction);
        assertThat(snapshot.getAggregateInstance(), is(item));
        final InventoryItem itemFromSnapshot = snapshot.getAggregateInstance();
        itemFromSnapshot.setDescription("notAnymore");
        assertThat(snapshot.getAggregateInstance(), is(identicalItem));
    }

    InventoryItem create() {
        final InventoryItem item = new InventoryItem();
        item.setId(UUID.randomUUID());
        item.setDescription("item4test");
        item.setAvailable(10);
        return item;
    }
}