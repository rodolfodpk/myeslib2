package org.myeslib.jdbi.storage;

import lombok.extern.slf4j.Slf4j;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.myeslib.data.UnitOfWork;
import org.myeslib.data.UnitOfWorkHistory;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.DecreaseInventory;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.CreateInventoryItem;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.IncreaseInventory;
import org.myeslib.sampledomain.aggregates.inventoryitem.events.InventoryDecreased;
import org.myeslib.sampledomain.aggregates.inventoryitem.events.InventoryIncreased;
import org.myeslib.sampledomain.aggregates.inventoryitem.events.InventoryItemCreated;
import org.myeslib.jdbi.storage.dao.UnitOfWorkDao;

import java.util.Arrays;
import java.util.UUID;

import static org.mockito.Mockito.verify;

@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class JdbiJournalTest {

    @Mock
    UnitOfWorkDao<UUID> dao;

    @Before
    public void init() throws Exception {
    }

    @Test
    public void oneTransaction() {

        UUID id = UUID.randomUUID();

        UnitOfWorkHistory toSave = new UnitOfWorkHistory();
        UnitOfWork newUow = UnitOfWork.create(UUID.randomUUID(), new CreateInventoryItem(UUID.randomUUID(), id), Arrays.asList(InventoryItemCreated.create(id, "item1")));
        toSave.add(newUow);

        JdbiJournal store = new JdbiJournal(dao);

        store.append(id, newUow);

        verify(dao).append(id, newUow);

    }

    @Test
    public void twoTransactions() {

        UUID id = UUID.randomUUID();

        UnitOfWork existingUow = UnitOfWork.create(UUID.randomUUID(), new IncreaseInventory(UUID.randomUUID(), id, 1, 0L), Arrays.asList(InventoryIncreased.create((1))));
        UnitOfWorkHistory existing = new UnitOfWorkHistory();
        existing.add(existingUow);

        UnitOfWork newUow = UnitOfWork.create(UUID.randomUUID(), new DecreaseInventory(UUID.randomUUID(), id, 1, 1L), Arrays.asList(InventoryDecreased.create((1))));

        JdbiJournal store = new JdbiJournal(dao);

        store.append(id, existingUow);

        store.append(id, newUow);

        verify(dao).append(id, existingUow);

        verify(dao).append(id, newUow);

    }

}
