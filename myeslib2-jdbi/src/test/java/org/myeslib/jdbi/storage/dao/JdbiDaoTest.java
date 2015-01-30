package org.myeslib.jdbi.storage.dao;

import com.google.gson.Gson;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.myeslib.core.Command;
import org.myeslib.data.CommandResults;
import org.myeslib.data.UnitOfWork;
import org.myeslib.data.UnitOfWorkHistory;
import org.myeslib.jdbi.storage.dao.config.CommandSerialization;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.DecreaseInventory;
import org.myeslib.sampledomain.aggregates.inventoryitem.events.InventoryDecreased;
import org.myeslib.sampledomain.aggregates.inventoryitem.events.InventoryIncreased;
import org.myeslib.sampledomain.aggregates.inventoryitem.events.SampleDomainGsonFactory;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.IncreaseInventory;
import org.myeslib.jdbi.storage.helpers.DbAwareBaseTestClass;
import org.myeslib.jdbi.storage.dao.config.DbMetadata;
import org.myeslib.jdbi.storage.dao.config.UowSerialization;

import java.util.Arrays;
import java.util.UUID;

import static junit.framework.TestCase.assertEquals;

public class JdbiDaoTest extends DbAwareBaseTestClass {

    Gson gson;
    UowSerialization functions;
    CommandSerialization<UUID> cmdSer;
    DbMetadata dbMetadata;
    JdbiDao<UUID> dao;

    @BeforeClass
    public static void setup() throws Exception {
        initDb();
    }

    @Before
    public void init() throws Exception {
        gson = new SampleDomainGsonFactory().create();
        functions = new UowSerialization(
                gson::toJson,
                (json) -> gson.fromJson(json, UnitOfWork.class));
        cmdSer = new CommandSerialization<UUID>(
                gson::toJson,
                (json) -> gson.fromJson(json, Command.class));
        dbMetadata = new DbMetadata("inventory_item");
        dao = new JdbiDao<>(functions, cmdSer, dbMetadata, dbi);
    }

    @Test
    public void firstTransactionOnEmptyHistory() {

        UUID id = UUID.randomUUID();

        IncreaseInventory command = new IncreaseInventory(UUID.randomUUID(), id, 1);
        
        UnitOfWorkHistory toSave = new UnitOfWorkHistory();
        UnitOfWork newUow = UnitOfWork.create(UUID.randomUUID(), command.getCommandId(), 0L, Arrays.asList(InventoryIncreased.create(1)));
        toSave.add(newUow);

        CommandResults<UUID> results = new CommandResults(command, newUow);

        dao.append(results);

        UnitOfWorkHistory fromDb = dao.getFull(id);

        assertEquals(toSave, fromDb);

    }

    @Test
    public void appendNewOnPreviousVersion() {

        UUID id = UUID.randomUUID();

        IncreaseInventory command1 = new IncreaseInventory(UUID.randomUUID(), id, 1);
        DecreaseInventory command2 = new DecreaseInventory(UUID.randomUUID(), id, 1);

        UnitOfWork existingUow = UnitOfWork.create(UUID.randomUUID(), command1.getCommandId(), 0L, Arrays.asList(InventoryIncreased.create(1)));
        UnitOfWorkHistory existing = new UnitOfWorkHistory();
        existing.add(existingUow);

        CommandResults<UUID> results1 = new CommandResults(command1, existingUow);

        UnitOfWork newUow = UnitOfWork.create(UUID.randomUUID(), command2.getCommandId(), 1L, Arrays.asList(InventoryDecreased.create((1))));

        CommandResults<UUID> results2 = new CommandResults(command2, newUow);

        dao.append(results1);

        dao.append(results2);

        UnitOfWorkHistory fromDb = dao.getFull(id);

        assertEquals(fromDb.getLastVersion().intValue(), 2);


    }

    @Test(expected = Exception.class)
    public void databaseIsHandlingOptimisticLocking() {

        UUID id = UUID.randomUUID();

        IncreaseInventory command1 = new IncreaseInventory(UUID.randomUUID(), id, 1);
        DecreaseInventory command2 = new DecreaseInventory(UUID.randomUUID(), id, 1);

        UnitOfWork existingUow = UnitOfWork.create(UUID.randomUUID(), command1.getCommandId(), 0L, Arrays.asList(InventoryIncreased.create((1))));
        UnitOfWorkHistory existing = new UnitOfWorkHistory();
        existing.add(existingUow);
        CommandResults<UUID> results1 = new CommandResults(command1, existingUow);

        dao.append(results1);

        UnitOfWork newUow = UnitOfWork.create(UUID.randomUUID(), command2.getCommandId(), 0L, Arrays.asList(InventoryDecreased.create((1))));
        CommandResults<UUID> results2 = new CommandResults(command2, newUow);

        dao.append(results2);

    }

}
