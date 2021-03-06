package org.myeslib.stack1.infra;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.myeslib.data.*;
import org.myeslib.infra.Consumers;
import org.myeslib.infra.WriteModelDao;
import org.myeslib.infra.exceptions.CommandExecutionException;
import sampledomain.aggregates.inventoryitem.InventoryItem;
import sampledomain.aggregates.inventoryitem.commands.CreateInventoryItem;
import sampledomain.aggregates.inventoryitem.commands.DecreaseInventory;
import sampledomain.aggregates.inventoryitem.commands.IncreaseInventory;
import sampledomain.aggregates.inventoryitem.events.InventoryDecreased;
import sampledomain.aggregates.inventoryitem.events.InventoryIncreased;
import sampledomain.aggregates.inventoryitem.events.InventoryItemCreated;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class Stack1JournalTest {

    @Mock
    WriteModelDao<UUID, InventoryItem> dao;

    @Mock
    Consumers<InventoryItem> consumers;

    @Captor
    ArgumentCaptor<List<EventMessage>> msgCaptor;

    @Before
    public void init() throws Exception {
    }

    @Test
    public void singleCommandShouldWork() {

        Stack1Journal<UUID, InventoryItem> store = new Stack1Journal<>(dao, consumers);
        UUID id = UUID.randomUUID();
        CommandId commandId = CommandId.create();

        CreateInventoryItem command =  CreateInventoryItem.create(commandId, id);
        UnitOfWork newUow = UnitOfWork.create(UnitOfWorkId.create(), commandId, 0L, Arrays.asList(InventoryItemCreated.create(id, "item1")));

        store.append(id, command, newUow);

        verify(dao).append(id, command, newUow);

    }

    @Test
    public void twoCommandsShouldWork() {

        Stack1Journal<UUID, InventoryItem> store = new Stack1Journal<UUID, InventoryItem>(dao, consumers);

        UUID id = UUID.randomUUID();

        CommandId command1Id = CommandId.create();
        IncreaseInventory command1 = IncreaseInventory.create(command1Id, id, 1);

        UnitOfWork existingUow = UnitOfWork.create(UnitOfWorkId.create(), command1.getCommandId(), 0L, Arrays.asList(InventoryIncreased.create((1))));

        DecreaseInventory command2 = DecreaseInventory.create(CommandId.create(), id, 1);

        UnitOfWork newUow = UnitOfWork.create(UnitOfWorkId.create(), command2.getCommandId(), 1L, Arrays.asList(InventoryDecreased.create((1))));

        store.append(id, command1, existingUow);

        verify(dao).append(id, command1, existingUow);

        store.append(id, command2, newUow);

        verify(dao).append(id, command2, newUow);

        //verifyNoMoreInteractions(dao, consumers);

    }

    @Test
    public void onSuccessThenEventConsumersShouldReceiveEvents() {

        Stack1Journal<UUID, InventoryItem> store = new Stack1Journal<UUID, InventoryItem>(dao, consumers);

        UUID id = UUID.randomUUID();
        CommandId commandId = CommandId.create();
        CreateInventoryItem command =  CreateInventoryItem.create(commandId, id);
        Event event = InventoryItemCreated.create(id, "item1");

        UnitOfWork newUow = UnitOfWork.create(UnitOfWorkId.create(), commandId, 0L, Arrays.asList(event));

        store.append(id, command, newUow);

        verify(dao).append(id, command, newUow);

        verify(consumers).consumeEvents(msgCaptor.capture());

        assertThat(msgCaptor.getValue().get(0).getEvent(), is(event));

        verifyNoMoreInteractions(dao, consumers);

    }

    @Test
    public void onDaoExceptionConsumersShouldNotReceiveAnyEvent() {

        Stack1Journal<UUID, InventoryItem> store = new Stack1Journal<UUID, InventoryItem>(dao, consumers);

        IncreaseInventory command =  IncreaseInventory.create(CommandId.create(), UUID.randomUUID(), 10);
        UnitOfWork unitOfWork = UnitOfWork.create(UnitOfWorkId.create(), command.getCommandId(), 1L, Arrays.asList(InventoryIncreased.create(10)));
        doThrow(CommandExecutionException.class).when(dao).append(command.targetId(), command, unitOfWork);

        try {
            store.append(command.targetId(), command, unitOfWork);
        } catch (Exception e) {
        }

        verify(dao).append(command.targetId(), command, unitOfWork);

        verifyNoMoreInteractions(dao, consumers);

    }

}
