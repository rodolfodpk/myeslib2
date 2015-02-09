package org.myeslib.sampledomain;

import com.esotericsoftware.kryo.Kryo;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.myeslib.jdbi.core.JdbiCommandId;
import org.myeslib.data.Snapshot;
import org.myeslib.infra.SnapshotReader;
import org.myeslib.jdbi.data.JdbiKryoSnapshot;
import org.myeslib.jdbi.infra.helpers.DatabaseHelper;
import org.myeslib.sampledomain.aggregates.inventoryitem.InventoryItem;
import org.myeslib.sampledomain.aggregates.inventoryitem.InventoryItemModule;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.CreateInventoryItem;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.CreateInventoryItemThenIncreaseThenDecrease;
import org.myeslib.sampledomain.aggregates.inventoryitem.handlers.CreateInventoryItemHandler;
import org.myeslib.sampledomain.aggregates.inventoryitem.handlers.CreateThenIncreaseThenDecreaseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class SampleDomainIntegrationTest {

    static final Logger logger = LoggerFactory.getLogger(SampleDomainIntegrationTest.class);

    static Injector injector;

    @BeforeClass
    public static void setup() throws Exception {
        injector = Guice.createInjector(new InventoryItemModule());
    }

    @Before
    public void init() throws Exception {
        injector.injectMembers(this);
        databaseHelper.initDb();
    }

    @Inject
    DatabaseHelper databaseHelper;
    @Inject
    SnapshotReader<UUID, InventoryItem> snapshotReader ;
    @Inject
    CreateInventoryItemHandler createInventoryItemHandler;
    @Inject
    CreateThenIncreaseThenDecreaseHandler createThenIncreaseThenDecreaseHandler;

    @Test
    public void testCreateInventoryItemHandler() throws InterruptedException {

        // create

        UUID itemId = UUID.randomUUID() ;
        CreateInventoryItem command =  CreateInventoryItem.create(JdbiCommandId.create(), itemId);

        createInventoryItemHandler.handle(command);

        InventoryItem expected = InventoryItem.builder().id(itemId).description(itemId.toString()).available(0).build();
        Snapshot<InventoryItem> expectedSnapshot = new JdbiKryoSnapshot<>(expected, 1L, new Kryo());

        assertThat(snapshotReader.getSnapshot(itemId), is(expectedSnapshot));

    }

    @Test
    public void testCreateThenIncreaseThenDecreaseHandler() throws InterruptedException {

        // command to create then increase and decrease

        UUID itemId = UUID.randomUUID();
        CreateInventoryItemThenIncreaseThenDecrease command = CreateInventoryItemThenIncreaseThenDecrease.create(JdbiCommandId.create(), itemId, 2, 1);

        createThenIncreaseThenDecreaseHandler.handle(command);

        InventoryItem expected = InventoryItem.builder().id(itemId).description(itemId.toString()).available(1).build();
        Snapshot<InventoryItem> expectedSnapshot = new JdbiKryoSnapshot<>(expected, 1L, new Kryo());

        assertThat(snapshotReader.getSnapshot(itemId), is(expectedSnapshot));

    }

}
