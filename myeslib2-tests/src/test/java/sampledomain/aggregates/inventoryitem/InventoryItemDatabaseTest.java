package sampledomain.aggregates.inventoryitem;

import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.myeslib.data.CommandId;
import org.myeslib.infra.WriteModelDao;
import org.myeslib.infra.commandbus.CommandBus;
import org.myeslib.stack1.infra.Stack1TestSupport;
import org.myeslib.stack1.infra.helpers.DatabaseHelper;
import sampledomain.aggregates.inventoryitem.commands.CreateInventoryItem;
import sampledomain.aggregates.inventoryitem.commands.DecreaseInventory;
import sampledomain.aggregates.inventoryitem.commands.IncreaseInventory;
import sampledomain.aggregates.inventoryitem.events.InventoryDecreased;
import sampledomain.aggregates.inventoryitem.events.InventoryIncreased;
import sampledomain.aggregates.inventoryitem.events.InventoryItemCreated;

import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(MockitoJUnitRunner.class)
public class InventoryItemDatabaseTest extends Stack1TestSupport<UUID, InventoryItem> {

    Injector injector;

    @Inject
    CommandBus<InventoryItem> commandBus;

    @Inject
    WriteModelDao<UUID, InventoryItem> writeModelDao;

    @Before
    public void setup() {
        injector = Guice.createInjector(new InventoryItemStack1Module(), new InventoryItemDbModule(), new InventoryGsonModule());
        injector.injectMembers(this);
        injector.getInstance(DatabaseHelper.class).initDb();
    }

    final UUID itemId = UUID.randomUUID();
    final String itemDescription = itemId.toString();
    final InventoryItemCreated createdEvent = InventoryItemCreated.create(itemId, itemDescription);
    final InventoryIncreased increasedEvent = InventoryIncreased.create(10);
    final InventoryDecreased decreasedEvent = InventoryDecreased.create(7);

    @Test
    public void shouldCreate() {
        command(CreateInventoryItem.create(CommandId.create(), itemId));
        assertThat(lastCmdEvents(itemId), is(Lists.newArrayList(createdEvent)));
    }

    @Test
    public void shouldIncrease() {
        command(CreateInventoryItem.create(CommandId.create(), itemId));
        command(IncreaseInventory.create(CommandId.create(), itemId, 10));
        assertThat(lastCmdEvents(itemId), is(Lists.newArrayList(increasedEvent)));
    }

    @Test
    public void shouldDecrease() {
        command(CreateInventoryItem.create(CommandId.create(), itemId));
        command(IncreaseInventory.create(CommandId.create(), itemId, 10));
        command(DecreaseInventory.create(CommandId.create(), itemId, 7));
        assertThat(lastCmdEvents(itemId), is(Lists.newArrayList(decreasedEvent)));
        assertThat(allEvents(itemId), is(Lists.newArrayList(createdEvent, increasedEvent, decreasedEvent)));
    }

    @Test
    public void shouldIgnoreSinceAnError() {
        command(CreateInventoryItem.create(CommandId.create(), itemId));
        command(IncreaseInventory.create(CommandId.create(), itemId, 10));
        command(DecreaseInventory.create(CommandId.create(), itemId, 11));
        assertThat(lastCmdEvents(itemId), is(Lists.newArrayList(increasedEvent)));
    }

    @Override
    protected CommandBus<InventoryItem> getCommandBus() {
        return commandBus;
    }

    @Override
    protected WriteModelDao<UUID, InventoryItem> getWriteModelDao() {
        return writeModelDao;
    }
}

