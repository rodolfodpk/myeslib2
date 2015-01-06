package org.myeslib.experimental.pm;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.myeslib.core.CommandHandler;
import static org.myeslib.jdbi.helpers.SampleDomain.*;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.myeslib.experimental.pm.ClassPattern.inCaseOf;
import static org.myeslib.experimental.pm.OtherwisePattern.otherwise;

@RunWith(MockitoJUnitRunner.class)
public class Sample {

    InventoryItemAggregateRoot aggregateRoot = new InventoryItemAggregateRoot();

    @Mock
    ItemDescriptionGeneratorService service;

    @Test
    public void test1() {

        when(service.generate(any(UUID.class))).thenReturn("blabla ");

        PatternMatching<Optional<CommandHandler>> pm = new PatternMatching<>(
                inCaseOf(CreateInventoryItem.class, x -> new CreateCommandHandler(aggregateRoot, service)),
                inCaseOf(IncreaseInventory.class, x -> new IncreaseCommandHandler(aggregateRoot)),
                inCaseOf(DecreaseInventory.class, x -> new DecreaseCommandHandler(aggregateRoot)),
                inCaseOf(Double.class, x -> "Double: " + x),
                otherwise(x -> Optional.empty())
        );

        UUID key = UUID.randomUUID() ;

        // create

        CreateInventoryItem command1 = new CreateInventoryItem(UUID.randomUUID(), key);

        CommandHandler handler1 = pm.matchFor(command1).get();

        System.out.println(handler1.handle(command1));

        // increase

        aggregateRoot.setId(key);
        aggregateRoot.setAvailable(0);

        IncreaseInventory command2 = new IncreaseInventory(UUID.randomUUID(), key, 10, 0L);

        CommandHandler handler2 = pm.matchFor(command2).get();

        System.out.println(handler2.handle(command2));


    }

}
