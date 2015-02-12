package org.myeslib.sampledomain

import com.google.common.eventbus.EventBus
import com.google.inject.Guice
import com.google.inject.Inject
import com.google.inject.name.Named
import org.myeslib.core.CommandId
import org.myeslib.sampledomain.aggregates.inventoryitem.InventoryItemModule
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.CreateInventoryItem
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.IncreaseInventory
import org.myeslib.sampledomain.aggregates.inventoryitem.events.InventoryIncreased

public class SampleDomainSpec extends Stack1BaseSpec<UUID> {

    def setupSpec() {
        injector = Guice.createInjector(new InventoryItemModule());
    }

    @Inject
    @Named("inventory-item-cmd-bus")
    EventBus commandBus;

    final UUID itemId = UUID.randomUUID();

    def "a little more focused on domain..."() {
        given:
            command(CreateInventoryItem.create(CommandId.create(), itemId))
         when:
            command(IncreaseInventory.create(CommandId.create(), itemId, 10))
         then:
            lastCmdEvents(itemId) == [InventoryIncreased.create(10)]
    }


    @Override
    protected commandBus() {
        return commandBus
    }

}
