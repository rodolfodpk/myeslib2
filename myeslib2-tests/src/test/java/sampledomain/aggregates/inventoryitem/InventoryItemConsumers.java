package sampledomain.aggregates.inventoryitem;

import org.myeslib.data.Command;
import org.myeslib.data.EventMessage;
import org.myeslib.infra.Consumers;
import org.myeslib.infra.commandbus.failure.CommandErrorMessage;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class InventoryItemConsumers implements Consumers<InventoryItem> {

    @Override
    public List<Consumer<List<EventMessage>>> eventMessageConsumers() {
        return Arrays.asList((eventList) -> System.out.println("received " + eventList));
    }

    @Override
    public List<Consumer<List<Command>>> commandsConsumers() {
        return Arrays.asList((commandList) -> System.out.println("received " + commandList));
    }

    @Override
    public List<Consumer<CommandErrorMessage>> errorMessageConsumers() {
        return Arrays.asList((error) -> System.out.println("received " + error));
    }
}
