package org.myeslib.stack1.infra.snapshot;

import com.esotericsoftware.kryo.Kryo;
import com.google.gson.Gson;
import org.apache.commons.beanutils.BeanUtils;
import org.myeslib.infra.Snapshot;
import org.myeslib.sampledomain.aggregates.inventoryitem.InventoryItem;
import org.myeslib.stack1.infra.Stack1Snapshot;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.lang.reflect.InvocationTargetException;
import java.util.UUID;
import java.util.function.Function;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class SnapshotMicroBench {

    static Kryo kryo = new Kryo();
    static Gson gson = new Gson();
    static Function<InventoryItem, InventoryItem> injectFunction = item -> item;

    public static void main(String[] args) throws RunnerException {

        kryo.register(InventoryItem.class);

        Options opt = new OptionsBuilder()
                .include(SnapshotMicroBench.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }


    @Benchmark
    public void gson() {

        final InventoryItem item =  create();
        final InventoryItem identicalItem = clone(item);
        final Snapshot<InventoryItem> snapshot = new Stack1GsonSnapshot<>(InventoryItem.class, item, 0L, gson, injectFunction);
        assertThat(snapshot.getAggregateInstance(), is(item));
        final InventoryItem itemFromSnapshot = snapshot.getAggregateInstance();
        itemFromSnapshot.setDescription("notAnymore");
        assertThat(snapshot.getAggregateInstance(), is(identicalItem));

    }

    @Benchmark
    public void beanutils() {

        final InventoryItem item =  create();
        final InventoryItem identicalItem = clone(item);
        final Snapshot<InventoryItem> snapshot = new Stack1BeanUtilsSnapshot<>(item, 0L, injectFunction);
        assertThat(snapshot.getAggregateInstance(), is(item));
        final InventoryItem itemFromSnapshot = snapshot.getAggregateInstance();
        itemFromSnapshot.setDescription("notAnymore");
        assertThat(snapshot.getAggregateInstance(), is(identicalItem));

    }

    @Benchmark
    public void kryo() {

        final InventoryItem item = create();
        final InventoryItem identicalItem = clone(item);
        final Snapshot<InventoryItem> snapshot = new Stack1KryoSnapshot<>(item, 0L, injectFunction, kryo);
        assertThat(snapshot.getAggregateInstance(), is(item));
        final InventoryItem itemFromSnapshot = snapshot.getAggregateInstance();
        itemFromSnapshot.setDescription("notAnymore");
        assertThat(snapshot.getAggregateInstance(), is(identicalItem));

    }

    @Benchmark
    public void spring() {

        final InventoryItem item =  create();
        final InventoryItem identicalItem = clone(item);
        final Snapshot<InventoryItem> snapshot = new Stack1SpringBeanSnapshot<>(item, 0L, injectFunction);
        assertThat(snapshot.getAggregateInstance(), is(item));
        final InventoryItem itemFromSnapshot = snapshot.getAggregateInstance();
        itemFromSnapshot.setDescription("notAnymore");
        assertThat(snapshot.getAggregateInstance(), is(identicalItem));

    }

    @Benchmark
    public void cloning() {

        final InventoryItem item =  create();
        final InventoryItem identicalItem = clone(item);
        final Snapshot<InventoryItem> snapshot = new Stack1CloningSnapshot<>(item, 0L, injectFunction);
        assertThat(snapshot.getAggregateInstance(), is(item));
        final InventoryItem itemFromSnapshot = snapshot.getAggregateInstance();
        itemFromSnapshot.setDescription("notAnymore");
        assertThat(snapshot.getAggregateInstance(), is(identicalItem));

    }

    @Benchmark
    public void core() {

        final InventoryItem item =  create();
        final InventoryItem identicalItem = clone(item);
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
