package org.myeslib.stack1.infra;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.myeslib.data.*;
import org.myeslib.infra.UnitOfWorkDao;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.CreateInventoryItem;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.DecreaseInventory;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.IncreaseInventory;
import org.myeslib.sampledomain.aggregates.inventoryitem.events.InventoryDecreased;
import org.myeslib.sampledomain.aggregates.inventoryitem.events.InventoryIncreased;
import org.myeslib.sampledomain.aggregates.inventoryitem.events.InventoryItemCreated;
import org.myeslib.stack1.infra.exceptions.InfraRuntimeException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class Stack1JournalTest {

    @Mock
    UnitOfWorkDao<UUID> dao;

    @Mock
    Consumer<EventMessage> queryModelConsumer;

    @Mock
    Consumer<EventMessage> sagaConsumer;

    List<Consumer<EventMessage>> consumerList ;

    @Before
    public void init() throws Exception {
        consumerList = new ArrayList<>();
        consumerList.add(queryModelConsumer);
        consumerList.add(sagaConsumer);
    }

    @Test
    public void singleCommandShouldWork() {

        Stack1Journal<UUID> store = new Stack1Journal<>(dao);
        UUID id = UUID.randomUUID();
        CommandId commandId = CommandId.create();

        CreateInventoryItem command =  CreateInventoryItem.create(commandId, id);
        UnitOfWork newUow = UnitOfWork.create(UnitOfWorkId.create(), commandId, 0L, Arrays.asList(InventoryItemCreated.create(id, "item1")));

        store.append(id, commandId, command, newUow);

        verify(dao).append(id, commandId, command, newUow);

    }

    @Test
    public void twoCommandsShouldWork() {

        Stack1Journal<UUID> store = new Stack1Journal<>(dao);

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
    public void onSuccessThenEventConsumersShouldReceiveEvents() {

        Stack1Journal<UUID> store = new Stack1Journal<>(dao, consumerList);

        UUID id = UUID.randomUUID();
        CommandId commandId = CommandId.create();
        CreateInventoryItem command =  CreateInventoryItem.create(commandId, id);
        Event event = InventoryItemCreated.create(id, "item1");

        UnitOfWork newUow = UnitOfWork.create(UnitOfWorkId.create(), commandId, 0L, Arrays.asList(event));

        store.append(id, commandId, command, newUow);

        verify(dao).append(id, commandId, command, newUow);

        ArgumentCaptor<EventMessage> msgCaptor = ArgumentCaptor.forClass(EventMessage.class);

        verify(queryModelConsumer).accept(msgCaptor.capture());
        verify(sagaConsumer).accept(msgCaptor.capture());

        assertThat(msgCaptor.getValue().getEvent(), is(event));

    }

    @Test
    public void onDaoExceptionConsumersShouldNotReceiveAnyEvent() {

        Stack1Journal<UUID> store = new Stack1Journal<>(dao, consumerList);

        IncreaseInventory command =  IncreaseInventory.create(CommandId.create(), UUID.randomUUID(), 10);
        UnitOfWork unitOfWork = UnitOfWork.create(UnitOfWorkId.create(), command.getCommandId(), 1L, Arrays.asList(InventoryIncreased.create(10)));
        doThrow(InfraRuntimeException.class).when(dao).append(command.targetId(), command.getCommandId(), command, unitOfWork);

        try {
            store.append(command.targetId(), command.getCommandId(), command, unitOfWork);
        } catch (Exception e) {
        }

        verify(dao).append(command.targetId(), command.getCommandId(), command, unitOfWork);

        verify(queryModelConsumer, times(0)).accept(any());
        verify(sagaConsumer, times(0)).accept(any());

    }

}
