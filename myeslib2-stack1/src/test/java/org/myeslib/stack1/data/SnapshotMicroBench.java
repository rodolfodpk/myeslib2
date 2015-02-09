package org.myeslib.stack1.data;

import com.esotericsoftware.kryo.Kryo;
import com.google.gson.Gson;
import org.myeslib.data.Snapshot;
import org.myeslib.sampledomain.aggregates.inventoryitem.InventoryItem;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class SnapshotMicroBench {

    static Kryo kryo = new Kryo();
    static Gson gson = new Gson();

    public static void main(String[] args) throws RunnerException {

        kryo.register(InventoryItem.class);

        Options opt = new OptionsBuilder()
                .include(SnapshotMicroBench.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }

    @Benchmark
    public void kryo() {

        final InventoryItem item = InventoryItem.builder().id(UUID.randomUUID()).description("item4test").available(0).build();
        final InventoryItem identicalItem = InventoryItem.builder().id(item.getId()).description(item.getDescription()).available(item.getAvailable()).build();
        final Snapshot<InventoryItem> snapshot = new Stack1KryoSnapshot<>(item, 0L, kryo);
        assertThat(snapshot.getAggregateInstance(), is(item));
        final InventoryItem itemFromSnapshot = snapshot.getAggregateInstance();
        itemFromSnapshot.setDescription("notAnymore");
        assertThat(snapshot.getAggregateInstance(), is(identicalItem));

    }

    @Benchmark
    public void gson() {

        final InventoryItem item = InventoryItem.builder().id(UUID.randomUUID()).description("item4test").available(0).build();
        final InventoryItem identicalItem = InventoryItem.builder().id(item.getId()).description(item.getDescription()).available(item.getAvailable()).build();
        final Snapshot<InventoryItem> snapshot = new Stack1GsonSnapshot<>(InventoryItem.class, item, 0L, gson);
        assertThat(snapshot.getAggregateInstance(), is(item));
        final InventoryItem itemFromSnapshot = snapshot.getAggregateInstance();
        itemFromSnapshot.setDescription("notAnymore");
        assertThat(snapshot.getAggregateInstance(), is(identicalItem));

    }

}
