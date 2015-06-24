package org.myeslib.stack1.infra.commandbus;

import org.myeslib.core.EventSourced;
import org.myeslib.data.Command;
import org.myeslib.infra.commandbus.CommandBus;
import org.myeslib.infra.commandbus.CommandSubscriber;
import org.myeslib.infra.exceptions.ConcurrencyException;
import org.myeslib.stack1.infra.helpers.MultiMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

public class Stack1RetrievableCommandBus<E extends EventSourced> implements CommandBus<E> {

    static final Logger logger = LoggerFactory.getLogger(Stack1RetrievableCommandBus.class);

    private final Map<String, Boolean> idempotentMap;
    private final CommandSubscriber<E> commandSubscriber;
    private final MultiMethod mm ;

    @Inject
    public Stack1RetrievableCommandBus(Map<String, Boolean> idempotentMap, CommandSubscriber<E> commandSubscriber) {
        this.idempotentMap = idempotentMap;
        this.commandSubscriber = commandSubscriber;
        this.mm =  MultiMethod.getMultiMethod(commandSubscriber.getClass(), "on");
    }

    @Override
    public void post(Command command) {
        if (idempotentMap.containsKey(command.getCommandId().toString())) {
            logger.warn("Command {} ignored since it was already processed", command.getCommandId());
            return;
        }
        try {
            checkNotNull(command);
            mm.invoke(commandSubscriber, command);
            idempotentMap.put(command.getCommandId().toString(), true);
        } catch (Throwable t) {
            if (t instanceof ConcurrencyException) {
                throw ((ConcurrencyException) t);
            } else if (t instanceof IllegalArgumentException) {
                throw ((IllegalArgumentException) t);
            } else if (t instanceof IllegalStateException) {
                throw ((IllegalStateException) t);
            } else if (t instanceof NullPointerException) {
                throw ((NullPointerException) t);
            } else {
                throw new RuntimeException(t);
            }
        }
    }

}
