package org.myeslib.jdbi.storage;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.myeslib.data.Snapshot;
import org.myeslib.function.SnapshotComputing;
import org.myeslib.data.UnitOfWork;
import org.myeslib.data.UnitOfWorkHistory;
import org.myeslib.sampledomain.aggregates.inventoryitem.InventoryItem;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.CreateInventoryItem;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.IncreaseInventory;
import org.myeslib.sampledomain.aggregates.inventoryitem.events.domain.InventoryIncreased;
import org.myeslib.sampledomain.aggregates.inventoryitem.events.domain.InventoryItemCreated;
import org.myeslib.jdbi.storage.dao.UnitOfWorkDao;

import java.util.Arrays;
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
    SnapshotComputing<InventoryItem> snapshotComputing;

    Cache<UUID, Snapshot<InventoryItem>> cache;

    @Before
    public void init() throws Exception {
        cache = CacheBuilder.newBuilder().maximumSize(1000).build();
    }

    @Test
    public void lastSnapshotNullEmptyHistory() throws ExecutionException {

        UUID id = UUID.randomUUID();

        JdbiReader<UUID, InventoryItem> reader = new JdbiReader<>(supplier, dao, cache, snapshotComputing);

        UnitOfWorkHistory expectedHistory = new UnitOfWorkHistory();
        InventoryItem instance = InventoryItem.builder().build();
        Snapshot<InventoryItem> expectedSnapshot = new Snapshot<>(instance, 0L);

        when(supplier.get()).thenReturn(instance);
        when(dao.getFull(id)).thenReturn(expectedHistory);
        when(snapshotComputing.applyEventsOn(instance, expectedHistory)).thenReturn(expectedSnapshot);

        assertThat(reader.getSnapshot(id), is(expectedSnapshot));

        verify(supplier).get();
        verify(dao).getFull(id);
        verify(snapshotComputing).applyEventsOn(instance, expectedHistory);

    }

    @Test
    public void lastSnapshotNullWithHistory() {

        UUID id = UUID.randomUUID();

        UnitOfWorkHistory expectedHistory = new UnitOfWorkHistory();
        InventoryItem expectedInstance = InventoryItem.builder().id(id).description("item1").available(0).build();
        Snapshot<InventoryItem> expectedSnapshot = new Snapshot<>(expectedInstance, 0L);

        UnitOfWork newUow = UnitOfWork.create(UUID.randomUUID(), new CreateInventoryItem(UUID.randomUUID(), id), Arrays.asList(InventoryItemCreated.create(id, "item1")));

        expectedHistory.add(newUow);

        when(supplier.get()).thenReturn(expectedInstance);
        when(dao.getFull(id)).thenReturn(expectedHistory);
        when(snapshotComputing.applyEventsOn(expectedInstance, expectedHistory)).thenReturn(expectedSnapshot);

        JdbiReader<UUID, InventoryItem> reader = new JdbiReader<>(supplier, dao, cache, snapshotComputing);

        assertThat(reader.getSnapshot(id), is(expectedSnapshot));

        verify(supplier).get();
        verify(dao).getFull(id);
        verify(snapshotComputing).applyEventsOn(expectedInstance, expectedHistory);

    }

    @Test
    public void lastSnapshotNotNullButUpToDate() {

        UUID id = UUID.randomUUID();

        Long expectedVersion = 1L;
        String expectedDescription = "item1";

        InventoryItem expectedInstance = InventoryItem.builder().id(id).description(expectedDescription).available(0).build();

        UnitOfWorkHistory expectedHistory = new UnitOfWorkHistory();

        UnitOfWork currentUow = UnitOfWork.create(UUID.randomUUID(), new CreateInventoryItem(UUID.randomUUID(), id), Arrays.asList(InventoryItemCreated.create(id, expectedDescription)));

        expectedHistory.add(currentUow);

        Snapshot<InventoryItem> expectedSnapshot = new Snapshot<>(expectedInstance, expectedVersion);

        cache.put(id, expectedSnapshot);

        when(dao.getPartial(id, expectedVersion)).thenReturn(expectedHistory);
        when(snapshotComputing.applyEventsOn(expectedInstance, expectedHistory)).thenReturn(expectedSnapshot);

        JdbiReader<UUID, InventoryItem> reader = new JdbiReader<UUID, InventoryItem>(supplier, dao, cache, snapshotComputing);

        assertThat(reader.getSnapshot(id), is(expectedSnapshot));

        verify(supplier, times(0)).get();
        verify(dao, times(1)).getPartial(id, expectedVersion);
        verify(dao, times(0)).getFull(id);
        verify(snapshotComputing).applyEventsOn(expectedInstance, expectedHistory);

    }

    @Test
    public void lastSnapshotNotNullButNotUpToDate() {

        UUID id = UUID.randomUUID();

        Long currentVersion = 1L;
        String expectedDescription = "item1";

        InventoryItem currentInstance = InventoryItem.builder().id(id).description(expectedDescription).available(0).build();

        Snapshot<InventoryItem> currentSnapshot = new Snapshot<>(currentInstance, currentVersion);

        cache.put(id, currentSnapshot);

        UnitOfWorkHistory remainingHistory = new UnitOfWorkHistory();
        UnitOfWork partialUow = UnitOfWork.create(UUID.randomUUID(), new IncreaseInventory(UUID.randomUUID(), id, 2, 1L), Arrays.asList(InventoryIncreased.create(2)));
        remainingHistory.add(partialUow);

        Long expectedVersion = 2L;
        InventoryItem expectedInstance = InventoryItem.builder().id(id).description(expectedDescription).available(2).build();

        Snapshot<InventoryItem> expectedSnapshot = new Snapshot<>(expectedInstance, expectedVersion);

        when(dao.getPartial(id, currentVersion)).thenReturn(remainingHistory);
        when(snapshotComputing.applyEventsOn(currentInstance, remainingHistory)).thenReturn(expectedSnapshot);

        JdbiReader<UUID, InventoryItem> reader = new JdbiReader<>(supplier, dao, cache, snapshotComputing);

        assertThat(reader.getSnapshot(id), is(expectedSnapshot));

        verify(supplier, times(0)).get();
        verify(dao, times(1)).getPartial(id, currentVersion);
        verify(dao, times(0)).getFull(id);
        verify(snapshotComputing).applyEventsOn(currentInstance, remainingHistory);

    }

}

