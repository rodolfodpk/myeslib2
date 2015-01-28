package org.myeslib.jdbi.function;

import org.myeslib.core.Event;
import org.myeslib.sampledomain.aggregates.inventoryitem.InventoryItem;
import org.myeslib.sampledomain.aggregates.inventoryitem.events.InventoryDecreased;
import org.myeslib.sampledomain.aggregates.inventoryitem.events.InventoryIncreased;
import org.myeslib.sampledomain.aggregates.inventoryitem.events.InventoryItemCreated;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SnapshotComputingMicroBench {

    public static void main(String[] args) throws RunnerException {

        Options opt = new OptionsBuilder()
                .include(SnapshotComputingMicroBench.class.getSimpleName())
                .forks(1)
                .build();

        new Runner(opt).run();
    }

    @Benchmark
    public void multiMethodEngine() {

        MutableSnapshotComputing<InventoryItem> engine = new MutableSnapshotComputing<>();

        InventoryItem item = InventoryItem.builder().build();

        engine.applyEventsOn(item, events());

      //  assert(item.getAvailable()==0);

    }


    @Benchmark
    public void eventBusEngine() {

        EventBusSnapshotComputing<InventoryItem> engine = new EventBusSnapshotComputing<>();

        InventoryItem item = InventoryItem.builder().build();

        engine.applyEventsOn(item, events());

       // assert(item.getAvailable()==0);

    }

    List<Event> events() {

        List<Event> events = new ArrayList<>();

        events.add(InventoryItemCreated.create(UUID.randomUUID(), "item1"));

        for (int i=0; i<100; i++) {
            events.add(InventoryIncreased.create(1));
            events.add(InventoryDecreased.create(1));
        }

        return events;

    }


}
