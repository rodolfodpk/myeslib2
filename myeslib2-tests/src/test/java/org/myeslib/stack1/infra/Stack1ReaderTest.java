package org.myeslib.stack1.infra;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.myeslib.data.CommandId;
import org.myeslib.data.Event;
import org.myeslib.data.UnitOfWork;
import org.myeslib.data.UnitOfWorkId;
import org.myeslib.infra.Snapshot;
import org.myeslib.infra.WriteModelDao;
import sampledomain.aggregates.inventoryitem.InventoryItem;
import sampledomain.aggregates.inventoryitem.commands.CreateInventoryItem;
import sampledomain.aggregates.inventoryitem.commands.IncreaseInventory;
import sampledomain.aggregates.inventoryitem.events.InventoryIncreased;
import sampledomain.aggregates.inventoryitem.events.InventoryItemCreated;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class Stack1ReaderTest {

    @Mock
    WriteModelDao<UUID, InventoryItem> dao;
    @Mock
    BiFunction<InventoryItem, List<Event>, InventoryItem> applyEventsFunction;
    @Mock
    Supplier<InventoryItem> supplier ;
    @Mock
    SnapshotFactory<InventoryItem> snapshotFactory;

    Cache<UUID, Snapshot<InventoryItem>> cache;
    Function<InventoryItem,InventoryItem> injectFunction;

    @Before
    public void init() throws Exception {
        cache = CacheBuilder.newBuilder().maximumSize(1000).build();
        injectFunction = (item) -> item;
    }

    @Test
    public void emptyCacheEmptyHistory() throws ExecutionException {

        UUID id = UUID.randomUUID();

        Stack1Reader<UUID, InventoryItem> reader = new Stack1Reader<>(supplier, dao, cache, applyEventsFunction, snapshotFactory);

        InventoryItem instance = InventoryItem.builder().build();
        Snapshot<InventoryItem> expectedSnapshot = new Stack1Snapshot<>(instance, 0L, supplier, injectFunction);
        List<UnitOfWork> expectedHistory = new ArrayList<>();
        List<Event> expectedEvents = new ArrayList<>();

        when(dao.getFull(id)).thenReturn(expectedHistory);
        when(supplier.get()).thenReturn(instance);
        when(applyEventsFunction.apply(instance, expectedEvents)).thenReturn(instance);
        when(snapshotFactory.create(instance, 0L)).thenReturn(expectedSnapshot);

        assertThat(reader.getSnapshot(id), is(expectedSnapshot));

        verify(dao).getFull(id);
        verify(supplier).get();
        verify(applyEventsFunction).apply(instance, expectedEvents);
        verify(snapshotFactory).create(instance, 0L);

    }

    @Test
    public void emptyCacheWithHistory() {

        UUID id = UUID.randomUUID();

        InventoryItem expectedInstance = InventoryItem.builder().build();
        Snapshot<InventoryItem> expectedSnapshot = new Stack1Snapshot<>(expectedInstance, 1L, supplier, injectFunction);

        CreateInventoryItem command = CreateInventoryItem.create(CommandId.create(), id);

        UnitOfWork newUow = UnitOfWork.create(UnitOfWorkId.create(), command.getCommandId(), 0L, Arrays.asList(InventoryItemCreated.create(id, "item1")));

        List<UnitOfWork> expectedHistory = Lists.newArrayList(newUow);
        List<Event> expectedEvents = new ArrayList<>(newUow.getEvents());

        when(supplier.get()).thenReturn(expectedInstance);
        when(dao.getFull(id)).thenReturn(expectedHistory);
        when(applyEventsFunction.apply(expectedInstance, expectedEvents)).thenReturn(expectedInstance);
        when(snapshotFactory.create(expectedInstance, 1L)).thenReturn(expectedSnapshot);

        Stack1Reader<UUID, InventoryItem> reader = new Stack1Reader<>(supplier, dao, cache, applyEventsFunction, snapshotFactory);

        assertThat(reader.getSnapshot(id), is(expectedSnapshot));

        verify(supplier).get();
        verify(dao).getFull(id);
        verify(applyEventsFunction).apply(expectedInstance, expectedEvents);
        verify(snapshotFactory).create(expectedInstance, 1L);

    }

    @Test
    public void hasCache() {

        UUID itemId = UUID.randomUUID();
        InventoryItem expectedInstance = InventoryItem.builder().build();
        CreateInventoryItem command = CreateInventoryItem.create(CommandId.create(), itemId);
        Snapshot<InventoryItem> expectedSnapshot = new Stack1Snapshot<>(expectedInstance, 1L, supplier, injectFunction);

        cache.put(itemId, expectedSnapshot);

        Stack1Reader<UUID, InventoryItem> reader = new Stack1Reader<>(supplier, dao, cache, applyEventsFunction, snapshotFactory);
        assertThat(reader.getSnapshot(itemId), is(expectedSnapshot));

    }

    @Test
    public void lastSnapshotNotNullButNotUpToDate() {

        UUID id = UUID.randomUUID();

        Long currentVersion = 1L;
        String expectedDescription = "item1";

        InventoryItem currentInstance =  InventoryItem.builder().id(id).description(expectedDescription).available(0).build();
        Snapshot<InventoryItem> currentSnapshot = new Stack1Snapshot<>(currentInstance, currentVersion, supplier, injectFunction);
        cache.put(id, currentSnapshot);

        IncreaseInventory command = IncreaseInventory.create(CommandId.create(), id, 2);
        UnitOfWork partialUow = UnitOfWork.create(UnitOfWorkId.create(), command.getCommandId(), currentVersion, Arrays.asList(InventoryIncreased.create(2)));
        List<UnitOfWork> remainingHistory = Lists.newArrayList(partialUow);
        List<Event> remainingEvents = new ArrayList<>(partialUow.getEvents());

        Long expectedVersion = 2L;
        InventoryItem expectedInstance =  InventoryItem.builder().id(id).description(expectedDescription).available(2).build();

        Snapshot<InventoryItem> expectedSnapshot = new Stack1Snapshot<>(expectedInstance, expectedVersion, supplier, injectFunction);

        when(supplier.get()).thenReturn(InventoryItem.builder().build());
        when(dao.getPartial(id, currentVersion)).thenReturn(remainingHistory);
        when(applyEventsFunction.apply(currentInstance, remainingEvents)).thenReturn(expectedInstance);
        when(snapshotFactory.create(expectedInstance, 2L)).thenReturn(expectedSnapshot);

        Stack1Reader<UUID, InventoryItem> reader = new Stack1Reader<>(supplier, dao, cache, applyEventsFunction, snapshotFactory);

        assertThat(reader.getSnapshot(id), is(expectedSnapshot));

        verify(supplier).get();
        verify(dao, times(1)).getPartial(id, currentVersion);
        verify(applyEventsFunction).apply(currentInstance, remainingEvents);
        verify(snapshotFactory).create(expectedInstance, 2L);
        verifyNoMoreInteractions(supplier, dao, applyEventsFunction, snapshotFactory);

    }

}

