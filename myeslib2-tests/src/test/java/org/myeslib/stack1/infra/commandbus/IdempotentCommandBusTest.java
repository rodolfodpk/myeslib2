package org.myeslib.stack1.infra.commandbus;

import junit.framework.TestCase;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.Is;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.myeslib.data.CommandId;
import org.myeslib.infra.Consumers;
import org.myeslib.infra.commandbus.CommandBus;
import org.myeslib.infra.commandbus.failure.CommandErrorMessage;
import sampledomain.aggregates.inventoryitem.InventoryItem;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RunWith(MockitoJUnitRunner.class)
public class IdempotentCommandBusTest extends TestCase {

    @Mock
    Consumers<InventoryItem> consumers;
    @Mock
    TestCommandSubscriber<InventoryItem> commandSubscriber;
    @Captor
    ArgumentCaptor<CommandErrorMessage> captor;

    CommandBus<InventoryItem> commandBus;
    Map<CommandId, Boolean> idempotentMap;

    @Before
    public void before() {
        idempotentMap = new HashMap<>();
        commandBus = new IdempotentCommandBus<>(idempotentMap, commandSubscriber, consumers);
    }

    @Test
    public void idempotentcy_must_works() {
        TestCommand command = new TestCommand(new CommandId(UUID.randomUUID()));
        commandBus.post(command);
        Mockito.verify(commandSubscriber).on(command);
        commandBus.post(command);
        commandBus.post(command);
        MatcherAssert.assertThat(idempotentMap.get(command.getCommandId()), Is.is(true));
        Mockito.verifyNoMoreInteractions(consumers, commandSubscriber);
    }

    @Test
    public void happyExecution() {
        TestCommand command = new TestCommand(new CommandId(UUID.randomUUID()));
        commandBus.post(command);
        Mockito.verify(commandSubscriber).on(command);
        Mockito.verifyNoMoreInteractions(consumers, commandSubscriber);

    }

    @Test
    public void errorsShouldBeNotified() {
        TestCommand command = new TestCommand(new CommandId(UUID.randomUUID()));
        Mockito.doThrow(new IllegalStateException("I got you !")).when(commandSubscriber).on(command);
        commandBus.post(command);
        Mockito.verify(consumers).consumeError(captor.capture());
        Mockito.verify(commandSubscriber).on(command);
        CommandErrorMessage message = captor.getValue();
        MatcherAssert.assertThat(message.getCommand(), Is.is(command));
        assert(message.getDescription().get().contains("I got you !"));
    }

}
