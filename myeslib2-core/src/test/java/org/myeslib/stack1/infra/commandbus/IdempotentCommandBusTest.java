package org.myeslib.stack1.infra.commandbus;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.myeslib.data.CommandId;
import org.myeslib.infra.commandbus.CommandBus;
import org.myeslib.infra.commandbus.failure.CommandErrorMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class IdempotentCommandBusTest extends TestCase {

    @Mock
    Consumer<CommandErrorMessage> consumer;

    @Mock
    TestCommandSubscriber commandSubscriber;

    @Captor
    ArgumentCaptor<CommandErrorMessage> captor;

    CommandBus commandBus;
    Map<CommandId, Boolean> idempotentMap;

    @Before
    public void before() {
        idempotentMap = new HashMap<>();
        commandBus = new IdempotentCommandBus(commandSubscriber, idempotentMap, consumer);
    }

    @Test
    public void idempotentcy_must_works() {
        TestCommand command = new TestCommand(CommandId.builder().build());
        commandBus.post(command);
        verify(commandSubscriber).on(command);
        commandBus.post(command);
        commandBus.post(command);
        assertThat(idempotentMap.get(command.getCommandId()), is(true));
        verifyNoMoreInteractions(consumer, commandSubscriber);
    }

    @Test
    public void happyExecution() {
        TestCommand command = new TestCommand(CommandId.builder().build());
        commandBus.post(command);
        verify(commandSubscriber).on(command);
        verifyNoMoreInteractions(consumer, commandSubscriber);

    }

    @Test
    public void errorsShouldBeNotified() {
        TestCommand command = new TestCommand(CommandId.builder().build());
        doThrow(new IllegalStateException("I got you !")).when(commandSubscriber).on(command);
        commandBus.post(command);
        verify(consumer).accept(captor.capture());
        verify(commandSubscriber).on(command);
        CommandErrorMessage message = captor.getValue();
        assertThat(message.getCommand(), is(command));
        assert(message.getDescription().get().contains("I got you !"));
    }

}