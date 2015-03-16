package org.myeslib.stack1.data;

import com.esotericsoftware.kryo.Kryo;
import com.google.gson.Gson;
import org.apache.commons.beanutils.BeanUtils;
import org.myeslib.data.Snapshot;
import org.myeslib.sampledomain.aggregates.inventoryitem.InventoryItem;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.lang.reflect.InvocationTargetException;
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

        final InventoryItem item = create();
        final InventoryItem identicalItem = clone(item);
        final Snapshot<InventoryItem> snapshot = new Stack1KryoSnapshot<>(item, 0L, kryo);
        assertThat(snapshot.getAggregateInstance(), is(item));
        final InventoryItem itemFromSnapshot = snapshot.getAggregateInstance();
        itemFromSnapshot.setDescription("notAnymore");
        assertThat(snapshot.getAggregateInstance(), is(identicalItem));

    }

    @Benchmark
    public void gson() {

        final InventoryItem item =  create();
        final InventoryItem identicalItem = clone(item);
        final Snapshot<InventoryItem> snapshot = new Stack1GsonSnapshot<>(InventoryItem.class, item, 0L, gson);
        assertThat(snapshot.getAggregateInstance(), is(item));
        final InventoryItem itemFromSnapshot = snapshot.getAggregateInstance();
        itemFromSnapshot.setDescription("notAnymore");
        assertThat(snapshot.getAggregateInstance(), is(identicalItem));

    }

    @Benchmark
    public void beanutils() {

        final InventoryItem item =  create();
        final InventoryItem identicalItem = clone(item);
        final Snapshot<InventoryItem> snapshot = new Stack1BeanUtilsSnapshot<>(item, 0L);
        assertThat(snapshot.getAggregateInstance(), is(item));
        final InventoryItem itemFromSnapshot = snapshot.getAggregateInstance();
        itemFromSnapshot.setDescription("notAnymore");
        assertThat(snapshot.getAggregateInstance(), is(identicalItem));

    }

    @Benchmark
    public void spring() {

        final InventoryItem item =  create();
        final InventoryItem identicalItem = clone(item);
        final Snapshot<InventoryItem> snapshot = new Stack1SpringBeanSnapshot<>(item, 0L);
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

    InventoryItem clone(InventoryItem item) {
        try {
            return (InventoryItem) BeanUtils.cloneBean(item);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        return null;
    }

}
