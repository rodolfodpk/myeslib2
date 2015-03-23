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

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class Stack1CommandBusTest extends TestCase {

    @Mock
    Consumer<CommandErrorMessage> consumer;

    @Mock
    TestCommandSubscriber commandSubscriber;

    @Captor
    ArgumentCaptor<CommandErrorMessage> captor;

    CommandBus commandBus;

    @Before
    public void before() {
        commandBus = new Stack1CommandBus(commandSubscriber, consumer);
    }

    @Test
    public void happyExecution() {
        TestCommand command = new TestCommand(new CommandId(UUID.randomUUID()));
        commandBus.post(command);
        verify(commandSubscriber).on(command);
        verifyNoMoreInteractions(consumer, commandSubscriber);
    }


    @Test
    public void errorsShouldBeNotified() {
        TestCommand command = new TestCommand(new CommandId(UUID.randomUUID()));
        doThrow(new IllegalStateException("I got you !")).when(commandSubscriber).on(command);
        commandBus.post(command);
        verify(consumer).accept(captor.capture());
        verify(commandSubscriber).on(command);
        CommandErrorMessage message = captor.getValue();
        assertThat(message.getCommand(), is(command));
        assert(message.getDescription().get().contains("I got you !"));
    }

}
