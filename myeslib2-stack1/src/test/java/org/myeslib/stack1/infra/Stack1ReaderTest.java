package org.myeslib.stack1.infra;

import com.esotericsoftware.kryo.Kryo;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.myeslib.data.Event;
import org.myeslib.infra.Snapshot;
import org.myeslib.data.UnitOfWork;
import org.myeslib.infra.ApplyEventsFunction;
import org.myeslib.infra.WriteModelDao;
import org.myeslib.sampledomain.aggregates.inventoryitem.InventoryItem;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.CreateInventoryItem;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.IncreaseInventory;
import org.myeslib.sampledomain.aggregates.inventoryitem.events.InventoryIncreased;
import org.myeslib.sampledomain.aggregates.inventoryitem.events.InventoryItemCreated;
import org.myeslib.data.CommandId;
import org.myeslib.data.UnitOfWorkId;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class Stack1ReaderTest {

    @Mock
    Supplier<InventoryItem> supplier;

    @Mock
    WriteModelDao<UUID> dao;

    @Mock
    ApplyEventsFunction<InventoryItem> applyEventsFunction;

    Cache<UUID, Snapshot<InventoryItem>> cache;

    Kryo kryo ;

    final Function<InventoryItem, InventoryItem> injectFunction = item -> item;

    @Before
    public void init() throws Exception {
        kryo = new Kryo();
        kryo.register(InventoryItem.class);
        cache = CacheBuilder.newBuilder().maximumSize(1000).build();
    }

    @Test
    public void lastSnapshotNullEmptyHistory() throws ExecutionException {

        UUID id = UUID.randomUUID();

        Stack1Reader<UUID, InventoryItem> reader = new Stack1Reader<>(supplier, dao, cache, applyEventsFunction, injectFunction);

        InventoryItem instance =  new InventoryItem(id, "item1", 0, null, null);
        Snapshot<InventoryItem> expectedSnapshot = new Stack1Snapshot<>(instance, 0L, injectFunction);
        List<UnitOfWork> expectedHistory = new ArrayList<>();
        List<Event> expectedEvents = new ArrayList<>();

        when(supplier.get()).thenReturn(instance);
        when(dao.getFull(id)).thenReturn(expectedHistory);
        when(applyEventsFunction.apply(instance, expectedEvents)).thenReturn(expectedSnapshot.getAggregateInstance());

        assertThat(reader.getSnapshot(id), is(expectedSnapshot));

        verify(supplier).get();
        verify(dao).getFull(id);
        verify(applyEventsFunction).apply(instance, expectedEvents);

    }

    @Test
    public void lastSnapshotNullWithHistory() {

        UUID id = UUID.randomUUID();

        InventoryItem expectedInstance = new InventoryItem(id, "item1", 0, null, null);
        Snapshot<InventoryItem> expectedSnapshot = new Stack1Snapshot<>(expectedInstance, 1L, injectFunction);

        CreateInventoryItem command = CreateInventoryItem.create(CommandId.create(), id);
        
        UnitOfWork newUow = UnitOfWork.create(UnitOfWorkId.create(), command.getCommandId(), 0L, Arrays.asList(InventoryItemCreated.create(id, "item1")));

        List<UnitOfWork> expectedHistory = Lists.newArrayList(newUow);
        List<Event> expectedEvents = new ArrayList<>(newUow.getEvents());

        when(supplier.get()).thenReturn(expectedInstance);
        when(dao.getFull(id)).thenReturn(expectedHistory);
        when(applyEventsFunction.apply(expectedInstance, expectedEvents)).thenReturn(expectedSnapshot.getAggregateInstance());

        Stack1Reader<UUID, InventoryItem> reader = new Stack1Reader<>(supplier, dao, cache, applyEventsFunction, injectFunction);

        assertThat(reader.getSnapshot(id), is(expectedSnapshot));

        verify(supplier).get();
        verify(dao).getFull(id);
        verify(applyEventsFunction).apply(expectedInstance, expectedEvents);

    }

    @Test
    public void lastSnapshotNotNullButUpToDate() {

        UUID id = UUID.randomUUID();

        Long expectedVersion = 1L;
        String expectedDescription = "item1";

        InventoryItem expectedInstance = new InventoryItem(id, expectedDescription, 0, null, null);

        CreateInventoryItem command = CreateInventoryItem.create(CommandId.create(), id);
        
        UnitOfWork currentUow = UnitOfWork.create(UnitOfWorkId.create(), command.getCommandId(), 0L, Arrays.asList(InventoryItemCreated.create(id, expectedDescription)));

        List<UnitOfWork> expectedHistory = Lists.newArrayList(currentUow);
        List<Event> expectedEvents = new ArrayList<>(currentUow.getEvents());

        Snapshot<InventoryItem> expectedSnapshot = new Stack1Snapshot<>(expectedInstance, expectedVersion, injectFunction);

        cache.put(id, expectedSnapshot);

        when(dao.getPartial(id, expectedVersion)).thenReturn(expectedHistory);
        when(applyEventsFunction.apply(expectedInstance, expectedEvents)).thenReturn(expectedSnapshot.getAggregateInstance());

        Stack1Reader<UUID, InventoryItem> reader = new Stack1Reader<UUID, InventoryItem>(supplier, dao, cache, applyEventsFunction, injectFunction);

        assertThat(reader.getSnapshot(id), is(expectedSnapshot));

        verify(supplier, times(0)).get();
        verify(dao, times(1)).getPartial(id, expectedVersion);
        verify(dao, times(0)).getFull(id);
        verify(applyEventsFunction).apply(expectedInstance, expectedEvents);

    }

    @Test
    public void lastSnapshotNotNullButNotUpToDate() {

        UUID id = UUID.randomUUID();

        Long currentVersion = 1L;
        String expectedDescription = "item1";

        InventoryItem currentInstance = new InventoryItem(id, expectedDescription, 0, null, null);

        Snapshot<InventoryItem> currentSnapshot = new Stack1Snapshot<>(currentInstance, currentVersion, injectFunction);

        cache.put(id, currentSnapshot);

        IncreaseInventory command = IncreaseInventory.create(CommandId.create(), id, 2);

        UnitOfWork partialUow = UnitOfWork.create(UnitOfWorkId.create(), command.getCommandId(), currentVersion, Arrays.asList(InventoryIncreased.create(2)));

        List<UnitOfWork> remainingHistory = Lists.newArrayList(partialUow);
        List<Event> expectedEvents = new ArrayList<>(partialUow.getEvents());

        Long expectedVersion = 2L;
        InventoryItem expectedInstance = new InventoryItem(id, expectedDescription, 2, null, null);

        Snapshot<InventoryItem> expectedSnapshot = new Stack1Snapshot<>(expectedInstance, expectedVersion, injectFunction);

        when(dao.getPartial(id, currentVersion)).thenReturn(remainingHistory);
        when(applyEventsFunction.apply(currentInstance, expectedEvents)).thenReturn(expectedSnapshot.getAggregateInstance());

        Stack1Reader<UUID, InventoryItem> reader = new Stack1Reader<>(supplier, dao, cache, applyEventsFunction, injectFunction);

        assertThat(reader.getSnapshot(id), is(expectedSnapshot));

        verify(supplier, times(0)).get();
        verify(dao, times(1)).getPartial(id, currentVersion);
        verify(dao, times(0)).getFull(id);
        verify(applyEventsFunction).apply(currentInstance, expectedEvents);

    }

}

