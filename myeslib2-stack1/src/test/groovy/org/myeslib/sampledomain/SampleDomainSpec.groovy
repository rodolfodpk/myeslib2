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
import org.myeslib.stack1.infra.dao.UnitOfWorkDao
import org.myeslib.stack1.infra.helpers.DatabaseHelper

import java.util.function.Supplier

import static org.mockito.Mockito.when

public class SampleDomainSpec extends Stack1BaseSpec {

    static Injector injector;

    def setupSpec() {
        injector = Guice.createInjector(Modules.override(new InventoryItemModule()).with(new InventoryItemModule4Test()));
        injector.getInstance(DatabaseHelper.class).initDb();
    }

    @Inject
    @Named("inventory-item-cmd-bus")
    EventBus commandBus;
    @Inject
    UnitOfWorkDao<UUID> unitOfWorkDao;
    @Inject
    Supplier<UnitOfWorkId> uowIdSupplier;

    def setup() {
        injector.injectMembers(this);
        when(uowIdSupplier.get()).thenReturn(UnitOfWorkId.create(), expUowId)
    }

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
