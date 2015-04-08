package org.myeslib.stack1.infra.commandbus;

import org.myeslib.core.EventSourced;
import org.myeslib.data.Command;
import org.myeslib.data.CommandId;
import org.myeslib.infra.commandbus.CommandSubscriber;
import org.myeslib.infra.commandbus.failure.CommandErrorMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Map;
import java.util.function.Consumer;

public class IdempotentCommandBus<E extends EventSourced> extends Stack1CommandBus<E> {

    static final Logger logger = LoggerFactory.getLogger(IdempotentCommandBus.class);

    private final Map<CommandId, Boolean> idempotentMap;

    @Inject
    public IdempotentCommandBus(CommandSubscriber commandSubscriber, Map<CommandId, Boolean> idempotentMap, Consumer<CommandErrorMessage> exceptionsConsumer) {
        super(commandSubscriber, exceptionsConsumer);
        this.idempotentMap = idempotentMap;
    }

    @Override
    public void post(Command command) {
        if (idempotentMap.containsKey(command.getCommandId())) {
            logger.warn("Command {} ignored since it was already processed", command.getCommandId());
            return;
        }
        super.post(command);
        idempotentMap.put(command.getCommandId(), true);
    }

}
