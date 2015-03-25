package org.myeslib.stack1.infra.commandbus;

import org.myeslib.data.Command;
import org.myeslib.infra.commandbus.CommandBus;
import org.myeslib.infra.commandbus.CommandSubscriber;
import org.myeslib.infra.commandbus.failure.ApplyEventsErrorMessage;
import org.myeslib.infra.commandbus.failure.CommandErrorMessage;
import org.myeslib.infra.commandbus.failure.ConcurrencyErrorMessage;
import org.myeslib.infra.commandbus.failure.UnknownErrorMessage;
import org.myeslib.infra.exceptions.ApplyEventsException;
import org.myeslib.infra.exceptions.ConcurrencyException;
import org.myeslib.stack1.infra.helpers.MultiMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import java.util.function.Consumer;

public class Stack1CommandBus implements CommandBus {

    static final Logger logger = LoggerFactory.getLogger(Stack1CommandBus.class);

    private final CommandSubscriber commandSubscriber;
    private final Consumer<CommandErrorMessage> exceptionsConsumer;
    private final MultiMethod mm ;

    @Inject
    public Stack1CommandBus(CommandSubscriber commandSubscriber, Consumer<CommandErrorMessage> exceptionsConsumer) {
        this.commandSubscriber = commandSubscriber;
        this.exceptionsConsumer = exceptionsConsumer;
        this.mm =  MultiMethod.getMultiMethod(commandSubscriber.getClass(), "on");
    }

    @Override
    public void post(Command command) {
        try {
            mm.invoke(commandSubscriber, command);
        } catch (Throwable t) {
            final CommandErrorMessage msg ;
            if (t instanceof ConcurrencyException) {
                final ConcurrencyException ce = (ConcurrencyException) t;
                msg = ConcurrencyErrorMessage.builder().command(command).newVersion(ce.getNewVersion()).currentVersion(ce.getCurrentVersion()).build();
            } else if (t instanceof ApplyEventsException) {
                final ApplyEventsException eap = (ApplyEventsException) t;
                msg = ApplyEventsErrorMessage.builder().command(command).description(getStackTrace(t)).build();
            } else {
                msg = UnknownErrorMessage.builder().command(command).description(getStackTrace(t)).build();
            }
            logger.error("Detected an error [{}] for command [{}]. It will be notified", msg.getId(), command);
            exceptionsConsumer.accept(msg);
        }
    }

    private Optional<String> getStackTrace(Throwable t) {
        if (t == null) {
            return Optional.of("Exception not available.");
        } else {
            final StringWriter stackTraceHolder = new StringWriter();
            t.printStackTrace(new PrintWriter(stackTraceHolder));
            return Optional.of(stackTraceHolder.toString());
        }
    }
}
