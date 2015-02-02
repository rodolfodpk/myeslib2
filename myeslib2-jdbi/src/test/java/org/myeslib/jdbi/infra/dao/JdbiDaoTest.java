package org.myeslib.jdbi.infra.dao;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.myeslib.core.Command;
import org.myeslib.data.UnitOfWork;
import org.myeslib.jdbi.infra.dao.config.CmdSerialization;
import org.myeslib.jdbi.infra.dao.config.DbMetadata;
import org.myeslib.jdbi.infra.dao.config.UowSerialization;
import org.myeslib.jdbi.infra.helpers.DbAwareBaseTestClass;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.CommandsGsonFactory;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.DecreaseInventory;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.IncreaseInventory;
import org.myeslib.sampledomain.aggregates.inventoryitem.events.EventsGsonFactory;
import org.myeslib.sampledomain.aggregates.inventoryitem.events.InventoryDecreased;
import org.myeslib.sampledomain.aggregates.inventoryitem.events.InventoryIncreased;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class JdbiDaoTest extends DbAwareBaseTestClass {

    Gson uowGson;
    UowSerialization uowSer;
    Gson cmdGson;
    CmdSerialization cmdSer;
    DbMetadata dbMetadata;
    JdbiDao<UUID> dao;

    @BeforeClass
    public static void setup() throws Exception {
        initDb();
    }

    @Before
    public void init() throws Exception {
        uowGson = new EventsGsonFactory().create();
        uowSer = new UowSerialization(
                uowGson::toJson,
                (json) -> uowGson.fromJson(json, UnitOfWork.class));
        cmdGson = new CommandsGsonFactory().create();
        cmdSer = new CmdSerialization(
                (cmd) -> cmdGson.toJson(cmd, Command.class),
                (json) -> cmdGson.fromJson(json, Command.class));
        dbMetadata = new DbMetadata("inventory_item");
        dao = new JdbiDao<>(uowSer, cmdSer, dbMetadata, dbi);
    }

    @Test
    public void tesetCmdSer() {

        IncreaseInventory command = IncreaseInventory.create(UUID.randomUUID(), UUID.randomUUID(), 1);

        String asString = cmdSer.toStringFunction.apply(command);

        Command cmd = cmdSer.fromStringFunction.apply(asString);

        assertThat(command, is(cmd));

    }

    @Test
    public void tesetUowSer() {

        UUID id = UUID.randomUUID();

        IncreaseInventory command = IncreaseInventory.create(UUID.randomUUID(), id, 1);

        UnitOfWork unitOfWork = UnitOfWork.create(UUID.randomUUID(), command.commandId(), 0L, Arrays.asList(InventoryIncreased.create(1)));

        String asString = uowSer.toStringFunction.apply(unitOfWork);

        UnitOfWork fromJson = uowSer.fromStringFunction.apply(asString);

        assertThat(unitOfWork, is(fromJson));
    }

    @Test
    public void firstTransactionOnEmptyHistory() {

        UUID id = UUID.randomUUID();

        IncreaseInventory command = IncreaseInventory.create(UUID.randomUUID(), id, 1);
        
        UnitOfWork newUow = UnitOfWork.create(UUID.randomUUID(), command.commandId(), 0L, Arrays.asList(InventoryIncreased.create(1)));

        dao.append(command.targetId(), command.commandId(), command, newUow);

        List<UnitOfWork> fromDb = dao.getFull(id);

        assertThat(Lists.newArrayList(newUow), is(fromDb));

        assertThat(command, is(dao.getCommand(command.commandId())));

    }

    @Test
    public void appendNewOnPreviousVersion() {

        UUID id = UUID.randomUUID();

        IncreaseInventory command1 = IncreaseInventory.create(UUID.randomUUID(), id, 1);
        DecreaseInventory command2 = DecreaseInventory.create(UUID.randomUUID(), id, 1);

        UnitOfWork existingUow = UnitOfWork.create(UUID.randomUUID(), command1.commandId(), 0L, Arrays.asList(InventoryIncreased.create(1)));
        UnitOfWork newUow = UnitOfWork.create(UUID.randomUUID(), command2.commandId(), 1L, Arrays.asList(InventoryDecreased.create((1))));

        dao.append(command1.targetId(), command1.commandId(), command1, existingUow);
        dao.append(command2.targetId(), command2.commandId(), command2, newUow);

        List<UnitOfWork> fromDb = dao.getFull(id);

        assertThat(Lists.newArrayList(existingUow, newUow), is(fromDb));
        assertThat(command1, is(dao.getCommand(command1.commandId())));
        assertThat(command2, is(dao.getCommand(command2.commandId())));
        assertThat(fromDb.get(fromDb.size()-1).getVersion(), is(2L));


    }

    @Test(expected = Exception.class)
    public void databaseIsHandlingOptimisticLocking() {

        UUID id = UUID.randomUUID();

        IncreaseInventory command1 = IncreaseInventory.create(UUID.randomUUID(), id, 1);
        DecreaseInventory command2 = DecreaseInventory.create(UUID.randomUUID(), id, 1);

        UnitOfWork existingUow = UnitOfWork.create(UUID.randomUUID(), command1.commandId(), 0L, Arrays.asList(InventoryIncreased.create((1))));

        dao.append(command1.targetId(), command1.commandId(), command1, existingUow);

        UnitOfWork newUow = UnitOfWork.create(UUID.randomUUID(), command2.commandId(), 0L, Arrays.asList(InventoryDecreased.create((1))));

        dao.append(command2.targetId(), command2.commandId(), command2, newUow);

    }

}
