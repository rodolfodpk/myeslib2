package org.myeslib.jdbi.storage.dao;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.myeslib.core.Command;
import org.myeslib.data.CommandResults;
import org.myeslib.data.UnitOfWork;
import org.myeslib.jdbi.storage.dao.config.CommandSerialization;
import org.myeslib.jdbi.storage.dao.config.DbMetadata;
import org.myeslib.jdbi.storage.dao.config.UowSerialization;
import org.myeslib.jdbi.storage.helpers.DbAwareBaseTestClass;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.DecreaseInventory;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.IncreaseInventory;
import org.myeslib.sampledomain.aggregates.inventoryitem.events.InventoryDecreased;
import org.myeslib.sampledomain.aggregates.inventoryitem.events.InventoryIncreased;
import org.myeslib.sampledomain.aggregates.inventoryitem.events.SampleDomainGsonFactory;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class JdbiDaoTest extends DbAwareBaseTestClass {

    Gson gson;
    UowSerialization uowSer;
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
        uowSer = new UowSerialization(
                gson::toJson,
                (json) -> gson.fromJson(json, UnitOfWork.class));
        Type type = new TypeToken<Command<UUID>>(){}.getType();
        cmdSer = new CommandSerialization<UUID>(
                (cmd) -> gson.toJson(cmd, type),
                (json) -> gson.fromJson(json, type));
        dbMetadata = new DbMetadata("inventory_item");
        dao = new JdbiDao<>(uowSer, cmdSer, dbMetadata, dbi);
    }

    @Test
    public void tesetCmdSer() {

        IncreaseInventory command = new IncreaseInventory(UUID.randomUUID(), UUID.randomUUID(), 1);

        String asString = cmdSer.toStringFunction.apply(command);

        Command<UUID> cmd = cmdSer.fromStringFunction.apply(asString);

        assertThat(command, is(cmd));

    }

    @Test
    public void firstTransactionOnEmptyHistory() {

        UUID id = UUID.randomUUID();

        IncreaseInventory command = new IncreaseInventory(UUID.randomUUID(), id, 1);
        
        UnitOfWork newUow = UnitOfWork.create(UUID.randomUUID(), command.getCommandId(), 0L, Arrays.asList(InventoryIncreased.create(1)));

        dao.append(command, newUow);

        List<UnitOfWork> fromDb = dao.getFull(id);

        assertThat(Lists.newArrayList(newUow), is(fromDb));

        assertThat(command, is(dao.getCommand(command.getCommandId())));

    }

    @Test
    public void appendNewOnPreviousVersion() {

        UUID id = UUID.randomUUID();

        IncreaseInventory command1 = new IncreaseInventory(UUID.randomUUID(), id, 1);
        DecreaseInventory command2 = new DecreaseInventory(UUID.randomUUID(), id, 1);

        UnitOfWork existingUow = UnitOfWork.create(UUID.randomUUID(), command1.getCommandId(), 0L, Arrays.asList(InventoryIncreased.create(1)));

        UnitOfWork newUow = UnitOfWork.create(UUID.randomUUID(), command2.getCommandId(), 1L, Arrays.asList(InventoryDecreased.create((1))));

        dao.append(command1, existingUow);

        dao.append(command2, newUow);

        List<UnitOfWork> fromDb = dao.getFull(id);

        assertThat(Lists.newArrayList(existingUow, newUow), is(fromDb));

        assertThat(command1, is(dao.getCommand(command1.getCommandId())));
        assertThat(command2, is(dao.getCommand(command2.getCommandId())));

        assertThat(fromDb.get(fromDb.size()-1).getVersion(), is(2L));


    }

    @Test(expected = Exception.class)
    public void databaseIsHandlingOptimisticLocking() {

        UUID id = UUID.randomUUID();

        IncreaseInventory command1 = new IncreaseInventory(UUID.randomUUID(), id, 1);
        DecreaseInventory command2 = new DecreaseInventory(UUID.randomUUID(), id, 1);

        UnitOfWork existingUow = UnitOfWork.create(UUID.randomUUID(), command1.getCommandId(), 0L, Arrays.asList(InventoryIncreased.create((1))));

        dao.append(command1, existingUow);

        UnitOfWork newUow = UnitOfWork.create(UUID.randomUUID(), command2.getCommandId(), 0L, Arrays.asList(InventoryDecreased.create((1))));
        CommandResults<UUID> results2 = new CommandResults(newUow);

        dao.append(command2, newUow);

    }

}
