package org.myeslib.stack1.infra.failure;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.myeslib.data.Command;
import org.myeslib.infra.exceptions.ConcurrencyException;
import org.myeslib.infra.failure.CommandErrorMessage;
import org.myeslib.infra.failure.CommandErrorMessageId;
import org.myeslib.infra.failure.ConcurrencyErrorMessage;
import org.myeslib.infra.failure.UnknowErrorMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.UUID;
import java.util.function.Consumer;

public class CommandErrorHandler implements MethodInterceptor {

    static final Logger logger = LoggerFactory.getLogger(CommandErrorHandler.class);

    private final Consumer<CommandErrorMessage> exceptionsConsumer;

    @Inject
    public CommandErrorHandler(Consumer<CommandErrorMessage> exceptionsConsumer) {
        this.exceptionsConsumer = exceptionsConsumer;
    }

    @Override
    public Object invoke(MethodInvocation methodInvocation) throws Throwable {
        try {
            methodInvocation.proceed();
        } catch (Throwable t) {
            final Command command = (Command) methodInvocation.getArguments()[0];
            final CommandErrorMessage msg ;
            if (t instanceof ConcurrencyException) {
                msg =  new ConcurrencyErrorMessage(new CommandErrorMessageId(UUID.randomUUID()), command);
            } else {
                msg = new UnknowErrorMessage(new CommandErrorMessageId(UUID.randomUUID()), command, getStackTrace(t));
            }
            logger.error("Detected an error [{}] for command [{}]. It will be notified", msg.getId(), command);
            exceptionsConsumer.accept(msg);
        }
        return null;
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
