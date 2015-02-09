package org.myeslib.sampledomain

import com.google.common.eventbus.EventBus
import com.google.inject.*
import com.google.inject.name.Named
import com.google.inject.util.Modules
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.myeslib.jdbi.core.JdbiCommandId
import org.myeslib.data.UnitOfWork
import org.myeslib.jdbi.data.JdbiUnitOfWorkId
import org.myeslib.infra.SnapshotReader
import org.myeslib.infra.UnitOfWorkJournal
import org.myeslib.jdbi.data.JdbiUnitOfWork
import org.myeslib.jdbi.infra.dao.UnitOfWorkDao
import org.myeslib.jdbi.infra.helpers.DatabaseHelper
import org.myeslib.sampledomain.aggregates.inventoryitem.InventoryItem
import org.myeslib.sampledomain.aggregates.inventoryitem.InventoryItemModule
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.CreateInventoryItem
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.IncreaseInventory
import org.myeslib.sampledomain.aggregates.inventoryitem.events.InventoryIncreased
import org.myeslib.sampledomain.aggregates.inventoryitem.events.InventoryItemCreated

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;


public class SampleDomainSpec extends spock.lang.Specification {

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
    EventBus[] eventsSubscribers;

    def "user story 1..."() {
        given: "a previously processed create inventory item command"
            def pastCmd = CreateInventoryItem.create(JdbiCommandId.create(), UUID.randomUUID())
        and: "its respective unitOfWork appended to journal"
            def pastUow = JdbiUnitOfWork.create(JdbiUnitOfWorkId.create(), pastCmd.commandId(), 0L, [InventoryItemCreated.create(pastCmd.targetId(), "item1")])
            journal.append(pastCmd.targetId(), pastCmd.commandId(), pastCmd, pastUow)
        and: "a new increaseInventory command to increase 10 units"
            def newCmd = IncreaseInventory.create(JdbiCommandId.create(), pastCmd.targetId(), 10)
        when: "I send the command to the bus"
            commandBus.post(newCmd)
        then: "I have that item with 10 available"
            snapshotReader.getSnapshot(pastCmd.targetId()).aggregateInstance == InventoryItem.builder().id(pastCmd.targetId()).description("item1").available(10).build()
        and: "an expected UnitOfWork"
            def expectedUow = JdbiUnitOfWork.create(JdbiUnitOfWorkId.create(), newCmd.commandId(), 1L, [InventoryIncreased.create(10)])
        and: "a new List[UnitOfWork] from dao"
            unitOfWorkDao.getFull(pastCmd.targetId()) == [pastUow, expectedUow]
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

}

class InventoryItemModule4Test extends PrivateModule {
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