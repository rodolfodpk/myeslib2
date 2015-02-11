package org.myeslib.sampledomain

import com.google.common.eventbus.EventBus
import com.google.inject.*
import com.google.inject.name.Named
import com.google.inject.util.Modules
import org.mockito.Mockito
import org.myeslib.core.CommandId
import org.myeslib.data.UnitOfWorkId
import org.myeslib.sampledomain.aggregates.inventoryitem.InventoryItemModule
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.CreateInventoryItem
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.IncreaseInventory
import org.myeslib.sampledomain.aggregates.inventoryitem.events.InventoryIncreased

import java.util.function.Supplier

public class SampleDomainSpec extends Stack1BaseSpec<UUID> {

    static Injector injector;

    def setupSpec() {
        injector = Guice.createInjector(Modules.override(new InventoryItemModule()).with(new InventoryItemModule4Test()));
    }

    @Inject
    @Named("inventory-item-cmd-bus")
    EventBus commandBus;

    final UUID itemId = UUID.randomUUID();
    final CommandId newCmdId = CommandId.create();
    final UnitOfWorkId expUowId = UnitOfWorkId.create();

    def "a little more focused on domain..."() {
        given:
            command(CreateInventoryItem.create(CommandId.create(), itemId))
         when:
            command(IncreaseInventory.create(newCmdId, itemId, 10))
         then:
            lastCmdEvents(itemId) == [InventoryIncreased.create(10)]
    }

    @Override
    protected commandBus() {
        return commandBus
    }

    static class InventoryItemModule4Test extends PrivateModule {
        @Provides
        @Exposed
        @Singleton
        public Supplier<UnitOfWorkId> supplierUowId() {
            return Mockito.mock(Supplier.class)
        }
        @Override
        protected void configure() {
        }
    }
}
