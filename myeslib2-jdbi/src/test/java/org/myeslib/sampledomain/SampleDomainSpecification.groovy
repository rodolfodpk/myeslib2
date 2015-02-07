package org.myeslib.sampledomain

import com.google.inject.Guice
import com.google.inject.Inject
import com.google.inject.Injector
import org.myeslib.core.Command
import org.myeslib.core.Event
import org.myeslib.data.UnitOfWork
import org.myeslib.infra.SnapshotReader
import org.myeslib.infra.UnitOfWorkJournal
import org.myeslib.jdbi.data.JdbiUnitOfWork
import org.myeslib.jdbi.infra.helpers.DatabaseHelper
import org.myeslib.sampledomain.aggregates.inventoryitem.InventoryItem
import org.myeslib.sampledomain.aggregates.inventoryitem.InventoryItemModule
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.CreateInventoryItem
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.IncreaseInventory
import org.myeslib.sampledomain.aggregates.inventoryitem.events.InventoryItemCreated
import org.myeslib.sampledomain.aggregates.inventoryitem.handlers.IncreaseHandler

class SampleDomainSpecification extends spock.lang.Specification {

    static Injector injector;

    def setupSpec() {
        injector = Guice.createInjector(new InventoryItemModule());
        injector.getInstance(DatabaseHelper.class).initDb();
    }

    def setup() {
        injector.injectMembers(this);
    }

    @Inject
    IncreaseHandler increaseHandler;
    @Inject
    UnitOfWorkJournal<UUID> journal;
    @Inject
    SnapshotReader<UUID, InventoryItem> snapshotReader ;

    def "user history 1..."() {
        given: "an previously created item"
            def pastCmd = CreateInventoryItem.create(UUID.randomUUID(), UUID.randomUUID())
                withEvents(pastCmd.targetId(), pastCmd, [InventoryItemCreated.create(pastCmd.targetId(), "item1")])
        and: "an increaseInventory command to increase 10 units"
            def newCmd = IncreaseInventory.create(UUID.randomUUID(), pastCmd.targetId(), 10)
        when: "I call the respective command handler"
            increaseHandler.handle(newCmd)
        then: "I haven that item with 1 specific item available"
            snapshotReader.getSnapshot(pastCmd.targetId()).aggregateInstance == new InventoryItem(pastCmd.targetId(), "item1", 10, null, null)
    }

    def protected List<Event> withEvents(UUID targetId, Command pastCmd, List<Event> events) {
        UnitOfWork pastUow = JdbiUnitOfWork.create(pastCmd.commandId(), pastCmd.commandId(), 0L, events)
        journal.append(targetId, pastCmd.commandId(), pastCmd, pastUow)
    }
}