package org.myeslib.stack1.infra.commandbus;

import org.myeslib.data.Command;
import org.myeslib.infra.commandbus.CommandBus;
import org.myeslib.infra.commandbus.CommandSubscriber;
import org.myeslib.infra.commandbus.failure.CommandErrorMessage;
import org.myeslib.infra.commandbus.failure.CommandErrorMessageId;
import org.myeslib.infra.commandbus.failure.ConcurrencyErrorMessage;
import org.myeslib.infra.commandbus.failure.UnknowErrorMessage;
import org.myeslib.infra.exceptions.ConcurrencyException;
import org.myeslib.stack1.infra.helpers.MultiMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.UUID;
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
                msg =  new ConcurrencyErrorMessage(new CommandErrorMessageId(UUID.randomUUID()), command);
            } else {
                msg = new UnknowErrorMessage(new CommandErrorMessageId(UUID.randomUUID()), command, getStackTrace(t));
            }
            logger.error("Detected an error [{}] for command [{}]. It will be notified", msg.getId(), command);
            exceptionsConsumer.accept(msg);
        }
    }

    private String getStackTrace(Throwable t) {
        if (t == null) {
            return "Exception not available.";
        } else {
            final StringWriter stackTraceHolder = new StringWriter();
            t.printStackTrace(new PrintWriter(stackTraceHolder));
            return stackTraceHolder.toString();
        }
    }
}
