package org.myeslib.jdbi.infra;

import com.google.common.eventbus.EventBus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.myeslib.data.UnitOfWork;
import org.myeslib.jdbi.infra.dao.UnitOfWorkDao;
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

    @Mock
    EventBus queryModel1Bus;

    @Mock
    EventBus saga1Bus;

    @Before
    public void init() throws Exception {
    }

    @Test
    public void oneTransaction() {

        UUID id = UUID.randomUUID();
        UUID commandId = UUID.randomUUID();

        CreateInventoryItem command =  CreateInventoryItem.create(commandId, id);
        UnitOfWork newUow = UnitOfWork.create(UUID.randomUUID(), commandId, 0L, Arrays.asList(InventoryItemCreated.create(id, "item1")));
        JdbiJournal store = new JdbiJournal(dao);

        store.append(id, commandId, command, newUow);

        verify(dao).append(id, commandId, command, newUow);

    }

    @Test
    public void twoTransactions() {

        UUID id = UUID.randomUUID();

        UUID command1Id = UUID.randomUUID();
        IncreaseInventory command1 = IncreaseInventory.create(command1Id, id, 1);

        UnitOfWork existingUow = UnitOfWork.create(UUID.randomUUID(), command1.commandId(), 0L, Arrays.asList(InventoryIncreased.create((1))));

        DecreaseInventory command2 = DecreaseInventory.create(UUID.randomUUID(), id, 1);

        UnitOfWork newUow = UnitOfWork.create(UUID.randomUUID(), command2.commandId(), 1L, Arrays.asList(InventoryDecreased.create((1))));

        JdbiJournal store = new JdbiJournal(dao);

        store.append(id, command1.commandId(), command1, existingUow);

        store.append(id, command2.commandId(), command2, newUow);

        verify(dao).append(id, command1.commandId(), command1, existingUow);

        verify(dao).append(id, command2.commandId(), command2, newUow);

    }

    @Test
    public void queryModelEventBuses() {

        UUID id = UUID.randomUUID();
        UUID commandId = UUID.randomUUID();

        CreateInventoryItem command =  CreateInventoryItem.create(commandId, id);

        UnitOfWork newUow = UnitOfWork.create(UUID.randomUUID(), commandId, 0L, Arrays.asList(InventoryItemCreated.create(id, "item1")));

        JdbiJournal store = new JdbiJournal(dao, queryModel1Bus, saga1Bus);

        store.append(id, commandId, command, newUow);

        verify(dao).append(id, commandId, command, newUow);

        verify(queryModel1Bus).post(newUow);

        verify(saga1Bus).post(newUow);

    }

}
