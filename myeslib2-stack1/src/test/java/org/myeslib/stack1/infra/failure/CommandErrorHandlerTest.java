package org.myeslib.stack1.infra.failure;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.matcher.Matchers;
import lombok.Value;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.myeslib.data.Command;
import org.myeslib.data.CommandId;
import org.myeslib.infra.failure.CommandErrorAware;
import org.myeslib.infra.failure.CommandErrorMessage;
import org.myeslib.stack1.infra.failure.CommandErrorHandler;

import javax.inject.Inject;
import java.util.function.Consumer;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class CommandErrorHandlerTest {

    @Mock
    Consumer<CommandErrorMessage> consumer;

    @Captor
    ArgumentCaptor<CommandErrorMessage> captor;

    @Inject
    CommandSubscriber commandSubscriber;

    @Test
    public void itShouldWork() {
        Injector injector = Guice.createInjector(new TestModule());
        injector.injectMembers(this);
        EventBus commandBus = new EventBus();
        commandBus.register(commandSubscriber);
        TestCommand command = new TestCommand();
        commandSubscriber.doSomething(command);
        verify(consumer).accept(captor.capture());
        CommandErrorMessage message = captor.getValue();
        assertThat(message.getCommand(), is(command));
       // System.out.println(message);
        assert(message.getDescription().get().contains("I got you !"));

    }

    static class CommandSubscriber {
        @Subscribe
        @CommandErrorAware
        public void doSomething(Command command) {
            throw new IllegalStateException(" I got you !");
        }
    }

    @Value
    static public class TestCommand implements Command {
        @Override
        public CommandId getCommandId() {
            return CommandId.create();
        }
    }

    public class TestModule extends AbstractModule {

        @Override
        protected void configure() {
            CommandErrorHandler tracker = new CommandErrorHandler(consumer);
            bind(CommandSubscriber.class);
            bindInterceptor(Matchers.any(), Matchers.annotatedWith(CommandErrorAware.class), tracker);
        }
    }

}
