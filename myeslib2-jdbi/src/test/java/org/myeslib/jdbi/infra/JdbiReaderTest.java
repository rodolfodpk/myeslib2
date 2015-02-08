package org.myeslib.jdbi.infra;

import com.esotericsoftware.kryo.Kryo;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.myeslib.core.CommandId;
import org.myeslib.core.Event;
import org.myeslib.data.Snapshot;
import org.myeslib.data.UnitOfWork;
import org.myeslib.infra.ApplyEventsFunction;
import org.myeslib.jdbi.data.JdbiKryoSnapshot;
import org.myeslib.jdbi.data.JdbiUnitOfWork;
import org.myeslib.jdbi.infra.dao.UnitOfWorkDao;
import org.myeslib.sampledomain.aggregates.inventoryitem.InventoryItem;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.CreateInventoryItem;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.IncreaseInventory;
import org.myeslib.sampledomain.aggregates.inventoryitem.events.InventoryIncreased;
import org.myeslib.sampledomain.aggregates.inventoryitem.events.InventoryItemCreated;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class JdbiReaderTest {

    @Mock
    Supplier<InventoryItem> supplier;

    @Mock
    UnitOfWorkDao<UUID> dao;

    @Mock
    ApplyEventsFunction<InventoryItem> applyEventsFunction;

    Cache<UUID, Snapshot<InventoryItem>> cache;

    Kryo kryo ;

    @Before
    public void init() throws Exception {
        kryo = new Kryo();
        kryo.register(InventoryItem.class);
        cache = CacheBuilder.newBuilder().maximumSize(1000).build();
    }

    @Test
    public void lastSnapshotNullEmptyHistory() throws ExecutionException {

        UUID id = UUID.randomUUID();

        JdbiReader<UUID, InventoryItem> reader = new JdbiReader<>(supplier, dao, cache, applyEventsFunction);

        InventoryItem instance = InventoryItem.builder().build();
        Snapshot<InventoryItem> expectedSnapshot = new JdbiKryoSnapshot<>(instance, 0L, kryo);
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

        InventoryItem expectedInstance = InventoryItem.builder().id(id).description("item1").available(0).build();
        Snapshot<InventoryItem> expectedSnapshot = new JdbiKryoSnapshot<>(expectedInstance, 1L, kryo);

        CreateInventoryItem command = CreateInventoryItem.create(new CommandId(UUID.randomUUID()), id);
        
        UnitOfWork newUow = JdbiUnitOfWork.create(UUID.randomUUID(), command.commandId(), 0L, Arrays.asList(InventoryItemCreated.create(id, "item1")));

        List<UnitOfWork> expectedHistory = Lists.newArrayList(newUow);
        List<Event> expectedEvents = new ArrayList<>(newUow.getEvents());

        when(supplier.get()).thenReturn(expectedInstance);
        when(dao.getFull(id)).thenReturn(expectedHistory);
        when(applyEventsFunction.apply(expectedInstance, expectedEvents)).thenReturn(expectedSnapshot.getAggregateInstance());

        JdbiReader<UUID, InventoryItem> reader = new JdbiReader<>(supplier, dao, cache, applyEventsFunction);

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

        InventoryItem expectedInstance = InventoryItem.builder().id(id).description(expectedDescription).available(0).build();

        CreateInventoryItem command = CreateInventoryItem.create(new CommandId(UUID.randomUUID()), id);
        
        UnitOfWork currentUow = JdbiUnitOfWork.create(UUID.randomUUID(), command.commandId(), 0L, Arrays.asList(InventoryItemCreated.create(id, expectedDescription)));

        List<UnitOfWork> expectedHistory = Lists.newArrayList(currentUow);
        List<Event> expectedEvents = new ArrayList<>(currentUow.getEvents());

        Snapshot<InventoryItem> expectedSnapshot = new JdbiKryoSnapshot<>(expectedInstance, expectedVersion, kryo);

        cache.put(id, expectedSnapshot);

        when(dao.getPartial(id, expectedVersion)).thenReturn(expectedHistory);
        when(applyEventsFunction.apply(expectedInstance, expectedEvents)).thenReturn(expectedSnapshot.getAggregateInstance());

        JdbiReader<UUID, InventoryItem> reader = new JdbiReader<UUID, InventoryItem>(supplier, dao, cache, applyEventsFunction);

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

        InventoryItem currentInstance = InventoryItem.builder().id(id).description(expectedDescription).available(0).build();

        Snapshot<InventoryItem> currentSnapshot = new JdbiKryoSnapshot<>(currentInstance, currentVersion, kryo);

        cache.put(id, currentSnapshot);

        IncreaseInventory command = IncreaseInventory.create(new CommandId(UUID.randomUUID()), id, 2);

        UnitOfWork partialUow = JdbiUnitOfWork.create(UUID.randomUUID(), command.commandId(), currentVersion, Arrays.asList(InventoryIncreased.create(2)));

        List<UnitOfWork> remainingHistory = Lists.newArrayList(partialUow);
        List<Event> expectedEvents = new ArrayList<>(partialUow.getEvents());

        Long expectedVersion = 2L;
        InventoryItem expectedInstance = InventoryItem.builder().id(id).description(expectedDescription).available(2).build();

        Snapshot<InventoryItem> expectedSnapshot = new JdbiKryoSnapshot<>(expectedInstance, expectedVersion, kryo);

        when(dao.getPartial(id, currentVersion)).thenReturn(remainingHistory);
        when(applyEventsFunction.apply(currentInstance, expectedEvents)).thenReturn(expectedSnapshot.getAggregateInstance());

        JdbiReader<UUID, InventoryItem> reader = new JdbiReader<>(supplier, dao, cache, applyEventsFunction);

        assertThat(reader.getSnapshot(id), is(expectedSnapshot));

        verify(supplier, times(0)).get();
        verify(dao, times(1)).getPartial(id, currentVersion);
        verify(dao, times(0)).getFull(id);
        verify(applyEventsFunction).apply(currentInstance, expectedEvents);

    }

}

