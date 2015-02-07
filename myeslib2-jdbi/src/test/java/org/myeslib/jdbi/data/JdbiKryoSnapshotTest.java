package org.myeslib.jdbi.data;

import com.esotericsoftware.kryo.Kryo;
import org.junit.Before;
import org.junit.Test;
import org.myeslib.data.Snapshot;
import org.myeslib.sampledomain.aggregates.inventoryitem.InventoryItem;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class JdbiKryoSnapshotTest {

    Kryo kryo ;

    @Before
    public void setup() {
        kryo = new Kryo();
        kryo.register(InventoryItem.class);
    }


    @Test
    public void getAggregateInstance() throws Exception {
        InventoryItem item = InventoryItem.builder().id(UUID.randomUUID()).description("item4test").available(0).build();
        Snapshot<InventoryItem> snapshot = new JdbiKryoSnapshot<>(item, 1L, kryo);
        assertThat(snapshot.getAggregateInstance(), is(item));

    }

    @Test
    public void getVersion() throws Exception {
        InventoryItem item = InventoryItem.builder().id(UUID.randomUUID()).description("item4test").available(0).build();
        Snapshot<InventoryItem> snapshot = new JdbiKryoSnapshot<>(item, 1L, kryo);
        assertThat(snapshot.getVersion(), is(1L));
    }

    @Test
    public void equals() throws Exception {
        InventoryItem item = InventoryItem.builder().id(UUID.randomUUID()).description("item4test").available(0).build();
        Snapshot<InventoryItem> snapshot = new JdbiKryoSnapshot<>(item, 1L, kryo);
        assertThat(snapshot.equals(new JdbiKryoSnapshot<>(item, 1L, kryo)), is(true));
    }

    @Test
    public void mutatingAggregateDoesNotAffectSnapshot() throws Exception {
        final InventoryItem item = InventoryItem.builder().id(UUID.randomUUID()).description("item4test").available(0).build();
        final InventoryItem identicalItem = InventoryItem.builder().id(item.getId()).description(item.getDescription()).available(item.getAvailable()).build();
        final Snapshot<InventoryItem> snapshot = new JdbiKryoSnapshot<>(item, 0L, kryo);
        assertThat(snapshot.getAggregateInstance(), is(item));
        final InventoryItem itemFromSnapshot = snapshot.getAggregateInstance();
        itemFromSnapshot.setDescription("notAnymore");
        assertThat(snapshot.getAggregateInstance(), is(identicalItem));
    }

}