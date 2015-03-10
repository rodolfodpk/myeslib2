package org.myeslib.sampledomain

import com.google.common.eventbus.EventBus
import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Inject
import com.google.inject.PrivateModule
import com.google.inject.Provides
import com.google.inject.Singleton
import com.google.inject.name.Named
import com.google.inject.util.Modules
import org.mockito.Mockito
import org.myeslib.data.CommandId
import org.myeslib.data.Event
import org.myeslib.sampledomain.aggregates.inventoryitem.InventoryItemModule
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.CreateInventoryItem
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.DecreaseInventory
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.IncreaseInventory
import org.myeslib.sampledomain.aggregates.inventoryitem.events.InventoryDecreased
import org.myeslib.sampledomain.aggregates.inventoryitem.events.InventoryIncreased
import org.myeslib.sampledomain.aggregates.inventoryitem.events.InventoryItemCreated
import org.myeslib.sampledomain.services.SampleDomainService
import org.myeslib.stack1.infra.dao.UnitOfWorkDao
import org.myeslib.stack1.infra.helpers.DatabaseHelper
import org.skife.jdbi.v2.DBI

import static org.mockito.Mockito.any
import static org.mockito.Mockito.when

public class SampleDomainSpec extends Stack1BaseSpec<UUID> {

    def setupSpec() {
        injector = Guice.createInjector(Modules.override(new InventoryItemModule()).with(new MockedDomainServicesModule()));
    }

    def setup() {
        injector.injectMembers(this);
        dbHelper.initDb()
        when(sampleDomainService.generateItemDescription(any(UUID.class))).thenReturn(itemDescription)
    }

    @Inject
    DatabaseHelper dbHelper;

    @Inject
    @Named("inventory-item-cmd-bus")
    EventBus commandBus

    @Inject
    SampleDomainService sampleDomainService

    @Inject
    UnitOfWorkDao<UUID> unitOfWorkDao;

    @Override
    protected EventBus commandBus() {
        return commandBus
    }

    @Override
    protected UnitOfWorkDao<String> unitOfWorkDao() {
        return unitOfWorkDao
    }

    final UUID itemId = UUID.randomUUID();
    final String itemDescription = "hammer"

    def "creating an inventory item"() {
        when:
             command(CreateInventoryItem.create(CommandId.create(), itemId))
        then:
            lastCmdEvents(itemId) == [InventoryItemCreated.create(itemId, itemDescription)]
    }

    def "increase"() {
        given:
            command(CreateInventoryItem.create(CommandId.create(), itemId))
        when:
            command(IncreaseInventory.create(CommandId.create(), itemId, 10))
        then:
            lastCmdEvents(itemId) == [InventoryIncreased.create(10)]
    }

    def "decrease"() {
        given:
            command(CreateInventoryItem.create(CommandId.create(), itemId))
        and:
            command(IncreaseInventory.create(CommandId.create(), itemId, 10))
        when:
            command(DecreaseInventory.create(CommandId.create(), itemId, 7))
        then:
            lastCmdEvents(itemId) == [InventoryDecreased.create(7)] as List<Event>
        and: "can check all events too"
            allEvents(itemId) == [InventoryItemCreated.create(itemId, itemDescription), InventoryIncreased.create(10), InventoryDecreased.create(7)] as List<Event>

    }

    def "decrease an unavailable item *will log an error*"() {
        given:
            command(CreateInventoryItem.create(CommandId.create(), itemId))
        and:
            command(IncreaseInventory.create(CommandId.create(), itemId, 10))
        when:
            command(DecreaseInventory.create(CommandId.create(), itemId, 11))
        then:
            lastCmdEvents(itemId) == [InventoryIncreased.create(10)]
    }

    static class MockedDomainServicesModule extends AbstractModule {

        @Override
        protected void configure() {
            bind(SampleDomainService.class).toInstance(Mockito.mock(SampleDomainService.class));
        }
    }

}
