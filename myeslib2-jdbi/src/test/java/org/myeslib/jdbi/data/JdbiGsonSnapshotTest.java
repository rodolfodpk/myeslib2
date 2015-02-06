package org.myeslib.jdbi.data;

import com.google.gson.Gson;
import org.junit.Test;
import org.myeslib.data.Snapshot;
import org.myeslib.sampledomain.aggregates.inventoryitem.InventoryItem;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class JdbiGsonSnapshotTest {

    final Gson gson = new Gson();

    @Test
    public void getAggregateInstance() throws Exception {
        InventoryItem item = InventoryItem.builder().id(UUID.randomUUID()).description("item4test").available(0).build();
        Snapshot<InventoryItem> snapshot = new JdbiGsonSnapshot<>(InventoryItem.class, item, 1L, gson);
        assertThat(snapshot.getAggregateInstance(), is(item));

    }

    @Test
    public void getVersion() throws Exception {
        InventoryItem item = InventoryItem.builder().id(UUID.randomUUID()).description("item4test").available(0).build();
        Snapshot<InventoryItem> snapshot = new JdbiGsonSnapshot<>(InventoryItem.class, item, 1L, gson);
        assertThat(snapshot.getVersion(), is(1L));
    }

    @Test
    public void equals() throws Exception {
        InventoryItem item = InventoryItem.builder().id(UUID.randomUUID()).description("item4test").available(0).build();
        Snapshot<InventoryItem> snapshot = new JdbiGsonSnapshot<>(InventoryItem.class, item, 1L, gson);
        assertThat(snapshot, is(new JdbiGsonSnapshot<>(InventoryItem.class, item, 1L, gson)));
    }

    @Test
    public void mutatingAggregateDoesNotAffectSnapshot() throws Exception {
        final InventoryItem item = InventoryItem.builder().id(UUID.randomUUID()).description("item4test").available(0).build();
        final InventoryItem identicalItem = InventoryItem.builder().id(item.getId()).description(item.getDescription()).available(item.getAvailable()).build();
        final Snapshot<InventoryItem> snapshot = new JdbiGsonSnapshot<>(InventoryItem.class, item, 0L, gson);
        assertThat(snapshot.getAggregateInstance(), is(item));
        final InventoryItem itemFromSnapshot = snapshot.getAggregateInstance();
        itemFromSnapshot.setDescription("notAnymore");
        assertThat(snapshot.getAggregateInstance(), is(identicalItem));
    }

}