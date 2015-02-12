package org.myeslib.stack1.infra;

import com.google.common.eventbus.EventBus;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.myeslib.data.CommandId;
import org.myeslib.data.UnitOfWork;
import org.myeslib.data.UnitOfWorkId;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.CreateInventoryItem;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.DecreaseInventory;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.IncreaseInventory;
import org.myeslib.sampledomain.aggregates.inventoryitem.events.InventoryDecreased;
import org.myeslib.sampledomain.aggregates.inventoryitem.events.InventoryIncreased;
import org.myeslib.sampledomain.aggregates.inventoryitem.events.InventoryItemCreated;
import org.myeslib.stack1.infra.dao.UnitOfWorkDao;
import org.myeslib.stack1.infra.exceptions.InfraRuntimeException;

import java.util.Arrays;
import java.util.UUID;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class Stack1JournalTest {

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

        Stack1Journal store = new Stack1Journal(dao);
        UUID id = UUID.randomUUID();
        CommandId commandId = CommandId.create();

        CreateInventoryItem command =  CreateInventoryItem.create(commandId, id);
        UnitOfWork newUow = UnitOfWork.create(UnitOfWorkId.create(), commandId, 0L, Arrays.asList(InventoryItemCreated.create(id, "item1")));

        store.append(id, commandId, command, newUow);

        verify(dao).append(id, commandId, command, newUow);

    }

    @Test
    public void twoCommandsShouldWork() {

        Stack1Journal store = new Stack1Journal(dao);

        UUID id = UUID.randomUUID();

        CommandId command1Id = CommandId.create();
        IncreaseInventory command1 = IncreaseInventory.create(command1Id, id, 1);

        UnitOfWork existingUow = UnitOfWork.create(UnitOfWorkId.create(), command1.getCommandId(), 0L, Arrays.asList(InventoryIncreased.create((1))));

        DecreaseInventory command2 = DecreaseInventory.create(CommandId.create(), id, 1);

        UnitOfWork newUow = UnitOfWork.create(UnitOfWorkId.create(), command2.getCommandId(), 1L, Arrays.asList(InventoryDecreased.create((1))));

        store.append(id, command1.getCommandId(), command1, existingUow);

        store.append(id, command2.getCommandId(), command2, newUow);

        verify(dao).append(id, command1.getCommandId(), command1, existingUow);

        verify(dao).append(id, command2.getCommandId(), command2, newUow);

    }

    @Test
    public void onSuccessThenEventBusesShouldReceiveEvents() {

        Stack1Journal store = new Stack1Journal(dao, queryModel1Bus, saga1Bus);

        UUID id = UUID.randomUUID();
        CommandId commandId = CommandId.create();
        CreateInventoryItem command =  CreateInventoryItem.create(commandId, id);

        UnitOfWork newUow = UnitOfWork.create(UnitOfWorkId.create(), commandId, 0L, Arrays.asList(InventoryItemCreated.create(id, "item1")));

        store.append(id, commandId, command, newUow);

        verify(dao).append(id, commandId, command, newUow);
        verify(queryModel1Bus).post(newUow);
        verify(saga1Bus).post(newUow);

    }

    @Test
    public void onDaoExceptionBusesShouldNotReceiveAnyEvent() {

        Stack1Journal store = new Stack1Journal(dao, queryModel1Bus, saga1Bus);

        IncreaseInventory command =  IncreaseInventory.create(CommandId.create(), UUID.randomUUID(), 10);
        UnitOfWork unitOfWork = UnitOfWork.create(UnitOfWorkId.create(), command.getCommandId(), 1L, Arrays.asList(InventoryIncreased.create(10)));
        doThrow(InfraRuntimeException.class).when(dao).append(command.targetId(), command.getCommandId(), command, unitOfWork);

        try {
            store.append(command.targetId(), command.getCommandId(), command, unitOfWork);
        } catch (Exception e) {
        }

        verify(dao).append(command.targetId(), command.getCommandId(), command, unitOfWork);
        verify(queryModel1Bus, times(0)).post(any(UnitOfWork.class));
        verify(saga1Bus, times(0)).post(any(UnitOfWork.class));

    }

}
