package org.myeslib.sampledomain

import com.google.common.eventbus.EventBus
import com.google.inject.*
import com.google.inject.name.Named
import com.google.inject.util.Modules
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.myeslib.data.CommandId
import org.myeslib.data.Event
import org.myeslib.data.UnitOfWork
import org.myeslib.data.UnitOfWorkId
import org.myeslib.infra.SnapshotReader
import org.myeslib.infra.UnitOfWorkJournal
import org.myeslib.sampledomain.aggregates.inventoryitem.InventoryItem
import org.myeslib.sampledomain.aggregates.inventoryitem.InventoryItemModule
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.CreateInventoryItem
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.IncreaseInventory
import org.myeslib.sampledomain.aggregates.inventoryitem.events.InventoryIncreased
import org.myeslib.sampledomain.aggregates.inventoryitem.events.InventoryItemCreated
import org.myeslib.stack1.infra.dao.UnitOfWorkDao
import org.myeslib.stack1.infra.helpers.DatabaseHelper
import org.myeslib.stack1.infra.helpers.factories.InventoryItemSnapshotFactory

import static org.mockito.Mockito.times
import static org.mockito.Mockito.verify

public class LowLevelSpec extends spock.lang.Specification {

    static Injector injector;

    def setupSpec() {
        injector = Guice.createInjector(Modules.override(new InventoryItemModule()).with(new InventoryItemModule4Test()));
        injector.getInstance(DatabaseHelper.class).initDb();
    }

    def setup() {
        injector.injectMembers(this);
    }

    @Inject
    UnitOfWorkJournal<UUID> journal;
    @Inject
    @Named("inventory-item-cmd-bus")
    EventBus commandBus;
    @Inject
    SnapshotReader<UUID, InventoryItem> snapshotReader ;
    @Inject
    UnitOfWorkDao<UUID> unitOfWorkDao;
    @Inject
    InventoryItemSnapshotFactory snapshotFactory;
    @Inject
    EventBus[] eventsSubscribers;

    def "not really an user story 1 but..."() {
        given: "a previously processed create inventory item command"
            def pastCmd = CreateInventoryItem.create(CommandId.create(), UUID.randomUUID())
        and: "its respective unitOfWork appended to journal"
            def pastUow = UnitOfWork.create(UnitOfWorkId.create(), pastCmd.getCommandId(), 0L, [InventoryItemCreated.create(pastCmd.targetId(), "item1")])
            journal.append(pastCmd.targetId(), pastCmd.getCommandId(), pastCmd, pastUow)
        and: "a new increaseInventory command to increase 10 units"
            def newCmd = IncreaseInventory.create(CommandId.create(), pastCmd.targetId(), 10)
        when: "I send the command to the bus"
            commandBus.post(newCmd)
        then: "I expect a new snapshot with version = 2 and that item with 10 available"
            def expectedSnapshot = snapshotFactory.create(InventoryItem.builder().id(pastCmd.targetId()).description("item1").available(10).build(), 2L)
        and: "the snapshotReader returns the expected snapshot"
            snapshotReader.getSnapshot(pastCmd.targetId()).equals(expectedSnapshot)
        and: "I expect a new UnitOfWork"
            def expectedUow = UnitOfWork.create(UnitOfWorkId.create(), newCmd.getCommandId(), 1L, [InventoryIncreased.create(10)])
        and: "the expected UnitOfWork is now appended to List[UnitOfWork] from dao"
            unitOfWorkDao.getFull(pastCmd.targetId()) == [pastUow, expectedUow] as List<Event> // should not pass !!
            println(unitOfWorkDao.getFull(pastCmd.targetId()).get(1))
            println(expectedUow)
        and: "eventBuses are notified"
            for (EventBus eventBus : eventsSubscribers) {
                ArgumentCaptor<UnitOfWork> captor = ArgumentCaptor.forClass(UnitOfWork.class);
                verify(eventBus, times(2)).post(captor.capture())
                UnitOfWork captured = captor.value
                captured.commandId == expectedUow.commandId
                captured.version == expectedUow.version
                captured.events == expectedUow.events
            }
    }

    static class InventoryItemModule4Test extends PrivateModule {
        @Provides
        @Exposed
        @Singleton
        public EventBus[] eventSubscribers() {
            return [Mockito.mock(EventBus.class), Mockito.mock(EventBus.class)] as EventBus[]
        }
        @Override
        protected void configure() {
        }
    }
}

