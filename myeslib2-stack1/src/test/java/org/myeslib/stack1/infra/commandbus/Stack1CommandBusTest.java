package org.myeslib.stack1.infra.commandbus;

import com.google.inject.*;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.myeslib.data.CommandId;
import org.myeslib.infra.commandbus.CommandBus;
import org.myeslib.infra.commandbus.CommandSubscriber;
import org.myeslib.infra.commandbus.failure.CommandErrorMessage;

import javax.inject.Inject;
import java.util.UUID;
import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(MockitoJUnitRunner.class)
public class Stack1CommandBusTest extends TestCase {

    @Mock
    Consumer<CommandErrorMessage> consumer;

    @Mock
    TestCommandSubscriber commandSubscriber;

    @Captor
    ArgumentCaptor<CommandErrorMessage> captor;

    @Inject
    CommandBus commandBus;

    @Test
    public void happyExecution() {
        Injector injector = Guice.createInjector(new TestModule());
        injector.injectMembers(this);

        TestCommand command = new TestCommand(new CommandId(UUID.randomUUID()));

        commandBus.post(command);

        verify(commandSubscriber).on(command);

        verifyNoMoreInteractions(consumer, commandSubscriber);

    }


    @Test
    public void errorsShouldBeNotified() {
        Injector injector = Guice.createInjector(new TestModule());
        injector.injectMembers(this);

        TestCommand command = new TestCommand(new CommandId(UUID.randomUUID()));
        doThrow(new IllegalStateException("I got you !")).when(commandSubscriber).on(command);

        commandBus.post(command);

        verify(consumer).accept(captor.capture());
        verify(commandSubscriber).on(command);

        CommandErrorMessage message = captor.getValue();
        assertThat(message.getCommand(), is(command));
        assert(message.getDescription().get().contains("I got you !"));

    }

    public class TestModule extends AbstractModule {

        @Provides
        public CommandBus bus() {
            return new Stack1CommandBus(commandSubscriber, consumer);
        }

        @Override
        protected void configure() {

            bind(CommandSubscriber.class).toInstance(commandSubscriber);

            bind(new TypeLiteral<Consumer<CommandErrorMessage>>() {
            }).toInstance(consumer);

        }
    }

}
