package org.myeslib.stack1.infra.commandbus;

import org.myeslib.core.EventSourced;
import org.myeslib.data.Command;
import org.myeslib.data.CommandId;
import org.myeslib.infra.Consumers;
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
import java.util.Map;
import java.util.UUID;

import static com.google.common.base.Preconditions.checkNotNull;

public class IdempotentCommandBus<E extends EventSourced> implements CommandBus<E> {

    static final Logger logger = LoggerFactory.getLogger(IdempotentCommandBus.class);

    private final Map<CommandId, Boolean> idempotentMap;
    private final CommandSubscriber<E> commandSubscriber;
    private final Consumers<E> consumers;
    private final MultiMethod mm ;

    @Inject
    public IdempotentCommandBus(Map<CommandId, Boolean> idempotentMap, CommandSubscriber<E> commandSubscriber, Consumers<E> consumers) {
        this.idempotentMap = idempotentMap;
        this.commandSubscriber = commandSubscriber;
        this.consumers = consumers;
        this.mm =  MultiMethod.getMultiMethod(commandSubscriber.getClass(), "on");
    }

    @Override
    public void post(Command command) {
        if (idempotentMap.containsKey(command.getCommandId())) {
            logger.warn("Command {} ignored since it was already processed", command.getCommandId());
            return;
        }
        try {
            checkNotNull(command);
            mm.invoke(commandSubscriber, command);
            idempotentMap.put(command.getCommandId(), true);
        } catch (Throwable t) {
            final CommandErrorMessage msg ;
            if (t instanceof ConcurrencyException) {
                msg =  new ConcurrencyErrorMessage(new CommandErrorMessageId(UUID.randomUUID()), command);
            } else {
                msg = new UnknowErrorMessage(new CommandErrorMessageId(UUID.randomUUID()), command, getStackTrace(t));
            }
            logger.error("Detected an error [{}] for command [{}]. It will be notified", msg.getId(), command);
            consumers.consumeError(msg);
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
