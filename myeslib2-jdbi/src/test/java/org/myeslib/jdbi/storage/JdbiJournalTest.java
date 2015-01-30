package org.myeslib.jdbi.storage;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.myeslib.data.CommandResults;
import org.myeslib.data.UnitOfWork;
import org.myeslib.data.UnitOfWorkHistory;
import org.myeslib.jdbi.storage.dao.UnitOfWorkDao;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.CreateInventoryItem;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.DecreaseInventory;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.IncreaseInventory;
import org.myeslib.sampledomain.aggregates.inventoryitem.events.InventoryDecreased;
import org.myeslib.sampledomain.aggregates.inventoryitem.events.InventoryIncreased;
import org.myeslib.sampledomain.aggregates.inventoryitem.events.InventoryItemCreated;

import java.util.Arrays;
import java.util.UUID;

import static org.mockito.Mockito.verify;

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

        CreateInventoryItem command = new CreateInventoryItem(UUID.randomUUID(), id);

        UnitOfWorkHistory toSave = new UnitOfWorkHistory();
        UnitOfWork newUow = UnitOfWork.create(UUID.randomUUID(), command.getCommandId(), 0L, Arrays.asList(InventoryItemCreated.create(id, "item1")));
        toSave.add(newUow);

        CommandResults<UUID> results = new CommandResults(command, newUow);

        JdbiJournal store = new JdbiJournal(dao);

        store.append(results);

        verify(dao).append(results);

    }

    @Test
    public void twoTransactions() {

        UUID id = UUID.randomUUID();

        IncreaseInventory command1 = new IncreaseInventory(UUID.randomUUID(), id, 1);

        UnitOfWork existingUow = UnitOfWork.create(UUID.randomUUID(), command1.getCommandId(), 0L, Arrays.asList(InventoryIncreased.create((1))));
        UnitOfWorkHistory existing = new UnitOfWorkHistory();
        existing.add(existingUow);

        CommandResults<UUID> results1 = new CommandResults(command1, existingUow);

        DecreaseInventory command2 = new DecreaseInventory(UUID.randomUUID(), id, 1);

        UnitOfWork newUow = UnitOfWork.create(UUID.randomUUID(), command2.getCommandId(), 1L, Arrays.asList(InventoryDecreased.create((1))));

        CommandResults<UUID> results2 = new CommandResults(command2, newUow);

        JdbiJournal store = new JdbiJournal(dao);

        store.append(results1);

        store.append(results2);

        verify(dao).append(results1);

        verify(dao).append(results2);

    }

}
