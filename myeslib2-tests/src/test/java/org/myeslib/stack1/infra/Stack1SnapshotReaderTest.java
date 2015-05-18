package org.myeslib.stack1.infra;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.myeslib.data.*;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class Stack1SnapshotReaderTest {

    @Mock
    WriteModelDao<UUID, InventoryItem> dao;
    @Mock
    BiFunction<InventoryItem, List<Event>, InventoryItem> applyEventsFunction;
    @Mock
    Supplier<InventoryItem> supplier ;
    @Mock
    Function<InventoryItem, InventoryItem> injector;

    Cache<UUID, SnapshotData<InventoryItem>> cache;

    @Before
    public void init() throws Exception {
        cache = CacheBuilder.newBuilder().maximumSize(1000).build();
    }

    @Test
    public void emptyCacheEmptyHistory() throws ExecutionException {

        UUID id = UUID.randomUUID();

        Stack1SnapshotReader<UUID, InventoryItem> reader = new Stack1SnapshotReader<>(supplier, dao, cache, applyEventsFunction, injector);

        InventoryItem instance = InventoryItem.builder().build();
        Snapshot<InventoryItem> expectedSnapshot = new Snapshot<>(instance, 0L);
        List<UnitOfWork> expectedHistory = new ArrayList<>();
        List<Event> expectedEvents = new ArrayList<>();

        when(dao.getFull(id)).thenReturn(expectedHistory);
        when(supplier.get()).thenReturn(instance);
        when(applyEventsFunction.apply(instance, expectedEvents)).thenReturn(instance);
        when(injector.apply(instance)).thenReturn(injected(instance));

        assertThat(reader.getSnapshot(id), is(expectedSnapshot));

        verify(dao).getFull(id);
        verify(supplier).get();
        verify(applyEventsFunction).apply(instance, expectedEvents);
        verify(injector).apply(instance);

        verifyNoMoreInteractions(dao, supplier, applyEventsFunction, injector);

    }

    @Test
    public void emptyCacheWithHistory() {

        UUID id = UUID.randomUUID();

        InventoryItem expectedInstance = InventoryItem.builder().build();
        Snapshot<InventoryItem> expectedSnapshot = new Snapshot<>(expectedInstance, 1L);

        CreateInventoryItem command = CreateInventoryItem.create(CommandId.create(), id);

        UnitOfWork newUow = UnitOfWork.create(UnitOfWorkId.create(), command.getCommandId(), 0L, Arrays.asList(InventoryItemCreated.create(id, "item1")));

        List<UnitOfWork> expectedHistory = Lists.newArrayList(newUow);
        List<Event> expectedEvents = new ArrayList<>(newUow.getEvents());

        when(supplier.get()).thenReturn(expectedInstance);
        when(dao.getFull(id)).thenReturn(expectedHistory);
        when(applyEventsFunction.apply(expectedInstance, expectedEvents)).thenReturn(expectedInstance);
        when(injector.apply(expectedInstance)).thenReturn(injected(expectedInstance));

        Stack1SnapshotReader<UUID, InventoryItem> reader = new Stack1SnapshotReader<>(supplier, dao, cache, applyEventsFunction, injector);

        assertThat(reader.getSnapshot(id), equalTo(expectedSnapshot));

        verify(supplier).get();
        verify(dao).getFull(id);
        verify(applyEventsFunction).apply(expectedInstance, expectedEvents);
        verify(injector).apply(expectedInstance);

        verifyNoMoreInteractions(dao, supplier, applyEventsFunction, injector);


    }

    @Test
    public void hasCacheAndNothingPendentOnDb() {

        UUID id = UUID.randomUUID();

        InventoryItem expectedInstance =  InventoryItem.builder().service((uuid) -> uuid.toString()).id(id).description(id.toString()).available(0).build();

        CreateInventoryItem command = CreateInventoryItem.create(CommandId.create(), id);
        List<Event> expectedEvents = Lists.newArrayList(InventoryItemCreated.create(id, id.toString()));
        UnitOfWork uow = UnitOfWork.create(UnitOfWorkId.create(), command.getCommandId(), 0L, expectedEvents);
        List<UnitOfWork> expectedHistory = Lists.newArrayList(uow);

        cache.put(id, new SnapshotData<>(expectedHistory));

        when(supplier.get()).thenReturn(expectedInstance);
        when(dao.getFull(id)).thenReturn(expectedHistory);
        when(applyEventsFunction.apply(expectedInstance, expectedEvents)).thenReturn(expectedInstance);
        when(injector.apply(expectedInstance)).thenReturn(injected(expectedInstance));

        Stack1SnapshotReader<UUID, InventoryItem> reader = new Stack1SnapshotReader<>(supplier, dao, cache, applyEventsFunction, injector);

        assertThat(reader.getSnapshot(id), equalTo(new Snapshot<>(expectedInstance, 1L)));

        verify(supplier).get();
        verify(dao).getPartial(id, 1L);
        verify(applyEventsFunction).apply(expectedInstance, expectedEvents);
        verify(injector).apply(expectedInstance);

        verifyNoMoreInteractions(dao, supplier, applyEventsFunction, injector);

    }

    @Test
    public void hasCacheAndSomePendentEntriesOnDb() {

        UUID id = UUID.randomUUID();
        String expectedDescription = id.toString();

        CreateInventoryItem command1 = CreateInventoryItem.create(CommandId.create(), id);
        UnitOfWork uow1 = UnitOfWork.create(UnitOfWorkId.create(), command1.getCommandId(), 0L, Arrays.asList(InventoryItemCreated.create(id, expectedDescription)));
        List<UnitOfWork> cachedHistory = Lists.newArrayList(uow1);

        cache.put(id, new SnapshotData<>(cachedHistory));

        Long expectedVersion = 2L;
        IncreaseInventory command = IncreaseInventory.create(CommandId.create(), id, 2);
        UnitOfWork uow2 = UnitOfWork.create(UnitOfWorkId.create(), command.getCommandId(), 1L, Arrays.asList(InventoryIncreased.create(2)));
        List<UnitOfWork> nonCachedHistory = Lists.newArrayList(uow2);

        final List<UnitOfWork> newHistory = Stream.of(cachedHistory, nonCachedHistory).flatMap(x -> x.stream()).collect(Collectors.toList());
        final InventoryItem expectedInstance =  InventoryItem.builder().id(id).description(expectedDescription).available(2).build();
        final Snapshot<InventoryItem> expectedSnapshot = new Snapshot<>(expectedInstance, expectedVersion);

        when(supplier.get()).thenReturn(InventoryItem.builder().build());
        when(dao.getPartial(id, 1L)).thenReturn(nonCachedHistory);
        when(applyEventsFunction.apply(InventoryItem.builder().build(), flatMap(newHistory))).thenReturn(expectedInstance);
        when(injector.apply(expectedInstance)).thenReturn(injected(expectedInstance));

        Stack1SnapshotReader<UUID, InventoryItem> reader = new Stack1SnapshotReader<>(supplier, dao, cache, applyEventsFunction, injector);

        assertThat(reader.getSnapshot(id), is(expectedSnapshot));

        verify(supplier).get();
        verify(dao).getPartial(id, 1L);
        verify(applyEventsFunction).apply(InventoryItem.builder().build(), flatMap(newHistory));
        verify(injector).apply(expectedInstance);

        verifyNoMoreInteractions(dao, supplier, applyEventsFunction, injector);

    }

    private InventoryItem injected(InventoryItem inventoryItem) {
        inventoryItem.setInteractionContext(new Stack1InteractionContext(inventoryItem));
        return inventoryItem;
    }

    private List<Event> flatMap(final List<UnitOfWork> unitOfWorks) {
        return unitOfWorks.stream().flatMap((unitOfWork) -> unitOfWork.getEvents().stream()).collect(Collectors.toList());
    }
}

