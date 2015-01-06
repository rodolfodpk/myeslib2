package org.myeslib.jdbi.storage;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.gson.Gson;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.myeslib.core.data.UnitOfWorkHistory;
import org.myeslib.core.data.Snapshot;
import org.myeslib.core.data.UnitOfWork;
import org.myeslib.jdbi.helpers.BaseTestClass;
import org.myeslib.jdbi.storage.config.AggregateRootFunctions;
import org.myeslib.jdbi.storage.dao.UnitOfWorkDao;
import org.myeslib.jdbi.helpers.SampleDomainGsonFactory;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;
import static org.myeslib.jdbi.helpers.SampleDomain.*;

@RunWith(MockitoJUnitRunner.class)
public class JdbiSnapshotReaderTest extends BaseTestClass {

    @Mock
    UnitOfWorkDao<UUID> dao;

    Gson gson ;
    AggregateRootFunctions<InventoryItemAggregateRoot> config ;
    Cache<UUID, Snapshot<InventoryItemAggregateRoot>> cache ;

    @BeforeClass
    public static void setup() throws Exception {
        initDb();
    }

    @Before
    public void init() throws Exception {
        gson = new SampleDomainGsonFactory().create();
        config = new AggregateRootFunctions<>(
                InventoryItemAggregateRoot::new,
                gson::toJson,
                (json) -> gson.fromJson(json, UnitOfWork.class));
        cache = CacheBuilder.newBuilder().maximumSize(1000).build();
    }

    @Test
	public void lastSnapshotNullEmptyHistory() throws ExecutionException {

		UUID id = UUID.randomUUID();
		
		JdbiSnapshotReader<UUID, InventoryItemAggregateRoot> reader = new JdbiSnapshotReader<>(config, dao, cache);

        when(dao.get(id)).thenReturn(new UnitOfWorkHistory());

        assertThat(reader.getSnapshot(id).getAggregateInstance(), is(new InventoryItemAggregateRoot()));

		verify(dao).get(id);

	}
	
	@Test
	public void lastSnapshotNullWithHistory() {

		UUID id = UUID.randomUUID();
		
		UnitOfWorkHistory transactionHistory = new UnitOfWorkHistory();

        UnitOfWork newUow = UnitOfWork.create(UUID.randomUUID(), new CreateInventoryItem(UUID.randomUUID(), id), Arrays.asList(new InventoryItemCreated(id, "item1")));

        transactionHistory.add(newUow);

        when(dao.get(id)).thenReturn(transactionHistory);

		JdbiSnapshotReader<UUID, InventoryItemAggregateRoot> st = new JdbiSnapshotReader<>(config, dao, cache);

		Snapshot<InventoryItemAggregateRoot> resultingSnapshot = st.getSnapshot(id);

		verify(dao).get(id);

		InventoryItemAggregateRoot fromSnapshot = resultingSnapshot.getAggregateInstance();

		assertThat(fromSnapshot.getAvailable(), is(0));

	}

    @Test
    public void lastSnapshotNotNullButUpToDate() {

        UUID id = UUID.randomUUID();

        Long expectedVersion = 1L;
        String expectedDescription = "item1";

        InventoryItemAggregateRoot expectedItem = new InventoryItemAggregateRoot();

        expectedItem.setId(id);
        expectedItem.setAvailable(0);
        expectedItem.setDescription(expectedDescription);

        UnitOfWorkHistory transactionHistory = new UnitOfWorkHistory();

        UnitOfWork currentUow = UnitOfWork.create(UUID.randomUUID(), new CreateInventoryItem(UUID.randomUUID(), id), Arrays.asList(new InventoryItemCreated(id, expectedDescription)));

        transactionHistory.add(currentUow);

        Snapshot<InventoryItemAggregateRoot> expectedSnapshot = new Snapshot<>(expectedItem, expectedVersion);

        cache.put(id, expectedSnapshot);

        when(dao.getPartial(id, expectedVersion)).thenReturn(transactionHistory);

        JdbiSnapshotReader<UUID, InventoryItemAggregateRoot> st = new JdbiSnapshotReader<>(config, dao, cache);

        Snapshot<InventoryItemAggregateRoot> resultingSnapshot = st.getSnapshot(id);

        verify(dao, times(1)).getPartial(id, expectedVersion);

        verify(dao, times(0)).get(id);

        assertThat(resultingSnapshot, is(expectedSnapshot));

    }

    @Test
    public void lastSnapshotNotNullButNotUpToDate() {

        UUID id = UUID.randomUUID();

        Long currentVersion = 1L;
        String expectedDescription = "item1";

        InventoryItemAggregateRoot currentItem = new InventoryItemAggregateRoot();
        currentItem.setId(id);
        currentItem.setAvailable(0);
        currentItem.setDescription(expectedDescription);

        UnitOfWorkHistory transactionHistory = new UnitOfWorkHistory();

        UnitOfWork partialUow = UnitOfWork.create(UUID.randomUUID(), new IncreaseInventory(UUID.randomUUID(), id, 2, 1L), Arrays.asList(new InventoryIncreased(id, 2)));

        transactionHistory.add(partialUow);

        Snapshot<InventoryItemAggregateRoot> currentSnapshot = new Snapshot<>(currentItem, currentVersion);

        cache.put(id, currentSnapshot);

        when(dao.getPartial(id, currentVersion)).thenReturn(transactionHistory);

        JdbiSnapshotReader<UUID, InventoryItemAggregateRoot> st = new JdbiSnapshotReader<>(config, dao, cache);

        Long expectedVersion = 2L;
        InventoryItemAggregateRoot expectedItem = new InventoryItemAggregateRoot();
        expectedItem.setId(id);
        expectedItem.setAvailable(2);
        expectedItem.setDescription(expectedDescription);

        Snapshot<InventoryItemAggregateRoot> expectedSnapshot = new Snapshot<>(expectedItem, expectedVersion);

        Snapshot<InventoryItemAggregateRoot> resultingSnapshot = st.getSnapshot(id);

        verify(dao, times(1)).getPartial(id, currentVersion);

        verify(dao, times(0)).get(id);

        assertThat(resultingSnapshot, is(expectedSnapshot));

    }

}

