package org.myeslib.jdbi.storage;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.myeslib.core.data.UnitOfWork;
import org.myeslib.core.data.UnitOfWorkHistory;
import org.myeslib.jdbi.helpers.SampleDomain;
import org.myeslib.jdbi.helpers.SampleDomainGsonFactory;
import org.myeslib.jdbi.storage.config.AggregateRootFunctions;
import org.myeslib.jdbi.storage.dao.UnitOfWorkDao;

import java.util.Arrays;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.myeslib.jdbi.helpers.SampleDomain.*;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class JdbiUuidUnitOfWorkJournalTest {

    @Mock
    UnitOfWorkDao<UUID> dao;

    Gson gson;
    AggregateRootFunctions<InventoryItemAggregateRoot> config;

    @Before
    public void init() throws Exception {
        gson = new SampleDomainGsonFactory().create();
        config = new AggregateRootFunctions<>(
                () -> new SampleDomain.InventoryItemAggregateRoot(),
                gson::toJson,
                (json) -> gson.fromJson(json, UnitOfWork.class));

    }

    @Test
    public void oneTransaction() {

        UUID id = UUID.randomUUID();

        UnitOfWorkHistory toSave = new UnitOfWorkHistory();
        UnitOfWork newUow = UnitOfWork.create(UUID.randomUUID(), new CreateInventoryItem(UUID.randomUUID(), id), Arrays.asList(new InventoryItemCreated(id, "item1")));
        toSave.add(newUow);

        JdbiUuidUnitOfWorkJournal store = new JdbiUuidUnitOfWorkJournal(dao);

        store.append(id, newUow);

        verify(dao).append(id, newUow);

    }

    @Test
    public void twoTransactions() {

        UUID id = UUID.randomUUID();

        UnitOfWork existingUow = UnitOfWork.create(UUID.randomUUID(), new IncreaseInventory(UUID.randomUUID(), id, 1, 0L), Arrays.asList(new InventoryIncreased(id, 1)));
        UnitOfWorkHistory existing = new UnitOfWorkHistory();
        existing.add(existingUow);

        UnitOfWork newUow = UnitOfWork.create(UUID.randomUUID(), new DecreaseInventory(UUID.randomUUID(), id, 1, 1L), Arrays.asList(new InventoryDecreased(id, 1)));

        JdbiUuidUnitOfWorkJournal store = new JdbiUuidUnitOfWorkJournal(dao);

        store.append(id, existingUow);

        store.append(id, newUow);

        verify(dao).append(id, existingUow);

        verify(dao).append(id, newUow);

    }

}
