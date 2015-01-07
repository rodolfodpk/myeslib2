package org.myeslib.experimental.pm;

import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.myeslib.storage.helpers.SampleDomain.*;

@RunWith(MockitoJUnitRunner.class)
public class Sample {

    InventoryItemAggregateRoot aggregateRoot = new InventoryItemAggregateRoot();

    @Mock
    SampleDomainService service;

//    @Test
//    public void testOneCommand() {
//
//        when(service.generate(any(UUID.class))).thenReturn("blabla ");
//
//        PatternMatching<Optional<CommandHandler>> pm = new PatternMatching<>(
//                inCaseOf(CreateInventoryItem.class, x -> new CreateCommandHandler(aggregateRoot, service)),
//                inCaseOf(IncreaseInventory.class, x -> new IncreaseCommandHandler(aggregateRoot)),
//                inCaseOf(DecreaseInventory.class, x -> new DecreaseCommandHandler(aggregateRoot)),
//                inCaseOf(Double.class, x -> "Double: " + x),
//                otherwise(x -> Optional.empty())
//        );
//
//        UUID key = UUID.randomUUID();
//
//        // create
//
//        CreateInventoryItem command1 = new CreateInventoryItem(UUID.randomUUID(), key);
//
//        CommandHandler handler1 = pm.matchFor(command1).getFull();
//
//        System.out.println(handler1.handle(command1));
//
//        // increase
//
//        aggregateRoot.setId(key);
//        aggregateRoot.setAvailable(0);
//
//        IncreaseInventory command2 = new IncreaseInventory(UUID.randomUUID(), key, 10, 0L);
//
//        CommandHandler handler2 = pm.matchFor(command2).getFull();
//
//        System.out.println(handler2.handle(command2));
//
//
//    }

}
