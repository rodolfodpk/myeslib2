package org.myeslib.sampledomain

import com.google.inject.AbstractModule
import com.google.inject.Guice
import com.google.inject.Inject
import com.google.inject.TypeLiteral
import com.google.inject.util.Modules
import org.mockito.Mockito
import org.myeslib.InventoryItemGuavaModule
import org.myeslib.data.Command
import org.myeslib.data.CommandId
import org.myeslib.data.Event
import org.myeslib.data.EventMessage
import org.myeslib.infra.Consumers
import org.myeslib.infra.WriteModelDao
import org.myeslib.infra.commandbus.CommandBus
import org.myeslib.infra.commandbus.failure.CommandErrorMessage
import org.myeslib.stack1.infra.helpers.DatabaseHelper
import sampledomain.aggregates.inventoryitem.InventoryItem
import sampledomain.aggregates.inventoryitem.InventoryItemStack1Module
import sampledomain.aggregates.inventoryitem.commands.CreateInventoryItem
import sampledomain.aggregates.inventoryitem.commands.DecreaseInventory
import sampledomain.aggregates.inventoryitem.commands.IncreaseInventory
import sampledomain.aggregates.inventoryitem.events.InventoryDecreased
import sampledomain.aggregates.inventoryitem.events.InventoryIncreased
import sampledomain.aggregates.inventoryitem.events.InventoryItemCreated
import sampledomain.services.SampleDomainService

import java.util.function.Consumer

import static org.mockito.Mockito.any
import static org.mockito.Mockito.when

public class InventoryItemTest extends Stack1BaseSpec<UUID, InventoryItem> {

    @Inject
    CommandBus<InventoryItem> commandBus

    @Inject
    WriteModelDao<UUID, InventoryItem> unitOfWorkDao;

    def setup() {

        def Consumer<List<EventMessage>> eventsConsumer = Mockito.mock(Consumer.class);
        def Consumer<List<Command>> commandsConsumer = Mockito.mock(Consumer.class);
        def Consumer<CommandErrorMessage> errorsConsumer = Mockito.mock(Consumer.class);

        def Consumers<InventoryItem> consumers = new Consumers<InventoryItem>() {
            @Override
            List<Consumer<List<EventMessage>>> eventMessageConsumers() {
                return Arrays.asList(eventsConsumer)
            }

            @Override
            List<Consumer<List<Command>>> commandsConsumers() {
                return Arrays.asList(commandsConsumer)
            }

            @Override
            List<Consumer<CommandErrorMessage>> errorMessageConsumers() {
                return Arrays.asList(errorsConsumer)
            }
        }

        def sampleDomainService = Mockito.mock(SampleDomainService.class)
        when(sampleDomainService.generateItemDescription(any(UUID.class))).thenReturn(itemDescription)
        def injector = Guice.createInjector(Modules.override(new InventoryItemStack1Module(), new InventoryItemGuavaModule()).with(new MockedDomainServicesModule(sampleDomainService: sampleDomainService, eventsConsumer: consumers)))
        injector.injectMembers(this);
        assert (commandBus!=null);

        // If you want to persist to database, comment out this
       // injector.getInstance(DatabaseHelper.class).initDb();
    }

    // fixture

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
        def SampleDomainService sampleDomainService
        def Consumers<InventoryItem> eventsConsumer
        @Override
        protected void configure() {

            bind(new TypeLiteral<Consumers<InventoryItem>>() {})
                    .toInstance(eventsConsumer)

            bind(SampleDomainService.class).toInstance(sampleDomainService);

            // If you want to persist to database, you may use Stack1JdbiDao instead of Stack1MemDao
            // bind(new TypeLiteral<WriteModelDao<UUID>>() {})
            //        .to(new TypeLiteral<Stack1JdbiDao<UUID>>() {}).asEagerSingleton();

        }
    }

}



