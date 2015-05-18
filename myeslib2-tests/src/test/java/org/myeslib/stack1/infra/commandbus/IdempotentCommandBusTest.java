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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

@RunWith(MockitoJUnitRunner.class)
public class IdempotentCommandBusTest extends TestCase {

    @Mock
    Consumers<InventoryItem> consumers;
    @Mock
    Consumer<CommandErrorMessage> errorConsumers;
    @Mock
    TestCommandSubscriber commandSubscriber;
    @Captor
    ArgumentCaptor<CommandErrorMessage> captor;

    CommandBus<InventoryItem> commandBus;
    Map<String, Boolean> idempotentMap;

    @Before
    public void before() {
        idempotentMap = new HashMap<>();
        Mockito.when(consumers.errorMessageConsumers()).thenReturn(Arrays.asList(errorConsumers));
        commandBus = new IdempotentCommandBus<>(idempotentMap, commandSubscriber, consumers);
    }

    @Test
    public void idempotentcy_must_works() {
        TestCommand command = new TestCommand(new CommandId(UUID.randomUUID()));
        commandBus.post(command);
        Mockito.verify(commandSubscriber).on(command);
        commandBus.post(command);
        commandBus.post(command);
        MatcherAssert.assertThat(idempotentMap.get(command.getCommandId().uuid().toString()), Is.is(true));
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
        Mockito.verify(errorConsumers).accept(captor.capture());
        Mockito.verify(commandSubscriber).on(command);
        CommandErrorMessage message = captor.getValue();
        MatcherAssert.assertThat(message.getCommand(), Is.is(command));
        assert(message.getDescription().get().contains("I got you !"));
    }

}
