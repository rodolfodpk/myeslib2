package org.myeslib.jdbi.infra;

import com.google.common.eventbus.EventBus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.myeslib.jdbi.data.JdbiUnitOfWorkId;
import org.myeslib.jdbi.core.JdbiCommandId;
import org.myeslib.data.UnitOfWork;
import org.myeslib.jdbi.data.JdbiUnitOfWork;
import org.myeslib.jdbi.infra.exceptions.InfraRuntimeException;
import org.myeslib.jdbi.infra.dao.UnitOfWorkDao;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.CreateInventoryItem;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.DecreaseInventory;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.IncreaseInventory;
import org.myeslib.sampledomain.aggregates.inventoryitem.events.InventoryDecreased;
import org.myeslib.sampledomain.aggregates.inventoryitem.events.InventoryIncreased;
import org.myeslib.sampledomain.aggregates.inventoryitem.events.InventoryItemCreated;

import java.util.Arrays;
import java.util.UUID;

import static org.mockito.Mockito.*;

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
    public void singleCommandShouldWork() {

        JdbiJournal store = new JdbiJournal(dao);
        UUID id = UUID.randomUUID();
        JdbiCommandId commandId = JdbiCommandId.create();

        CreateInventoryItem command =  CreateInventoryItem.create(commandId, id);
        UnitOfWork newUow = JdbiUnitOfWork.create(JdbiUnitOfWorkId.create(), commandId, 0L, Arrays.asList(InventoryItemCreated.create(id, "item1")));

        store.append(id, commandId, command, newUow);

        verify(dao).append(id, commandId, command, newUow);

    }

    @Test
    public void twoCommandsShouldWork() {

        JdbiJournal store = new JdbiJournal(dao);

        UUID id = UUID.randomUUID();

        JdbiCommandId command1Id = JdbiCommandId.create();
        IncreaseInventory command1 = IncreaseInventory.create(command1Id, id, 1);

        UnitOfWork existingUow = JdbiUnitOfWork.create(JdbiUnitOfWorkId.create(), command1.commandId(), 0L, Arrays.asList(InventoryIncreased.create((1))));

        DecreaseInventory command2 = DecreaseInventory.create(JdbiCommandId.create(), id, 1);

        UnitOfWork newUow = JdbiUnitOfWork.create(JdbiUnitOfWorkId.create(), command2.commandId(), 1L, Arrays.asList(InventoryDecreased.create((1))));

        store.append(id, command1.commandId(), command1, existingUow);

        store.append(id, command2.commandId(), command2, newUow);

        verify(dao).append(id, command1.commandId(), command1, existingUow);

        verify(dao).append(id, command2.commandId(), command2, newUow);

    }

    @Test
    public void onSuccessThenEventBusesShouldReceiveEvents() {

        JdbiJournal store = new JdbiJournal(dao, queryModel1Bus, saga1Bus);

        UUID id = UUID.randomUUID();
        JdbiCommandId commandId = JdbiCommandId.create();
        CreateInventoryItem command =  CreateInventoryItem.create(commandId, id);

        UnitOfWork newUow = JdbiUnitOfWork.create(JdbiUnitOfWorkId.create(), commandId, 0L, Arrays.asList(InventoryItemCreated.create(id, "item1")));

        store.append(id, commandId, command, newUow);

        verify(dao).append(id, commandId, command, newUow);
        verify(queryModel1Bus).post(newUow);
        verify(saga1Bus).post(newUow);

    }

    @Test
    public void onDaoExceptionBusesShouldNotReceiveAnyEvent() {

        JdbiJournal store = new JdbiJournal(dao, queryModel1Bus, saga1Bus);

        IncreaseInventory command =  IncreaseInventory.create(JdbiCommandId.create(), UUID.randomUUID(), 10);
        UnitOfWork UnitOfWork = JdbiUnitOfWork.create(JdbiUnitOfWorkId.create(), command.commandId(), 1L, Arrays.asList(InventoryIncreased.create(10)));
        doThrow(InfraRuntimeException.class).when(dao).append(command.targetId(), command.commandId(), command, UnitOfWork);

        try {
            store.append(command.targetId(), command.commandId(), command, UnitOfWork);
        } catch (Exception e) {
        }

        verify(dao).append(command.targetId(), command.commandId(), command, UnitOfWork);
        verify(queryModel1Bus, times(0)).post(any(JdbiUnitOfWork.class));
        verify(saga1Bus, times(0)).post(any(JdbiUnitOfWork.class));

    }

}
