package org.myeslib.stack1.infra.dao;

import com.google.common.collect.Lists;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.myeslib.core.CommandId;
import org.myeslib.data.UnitOfWork;
import org.myeslib.data.UnitOfWorkId;
import org.myeslib.sampledomain.aggregates.inventoryitem.InventoryItemModule;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.DecreaseInventory;
import org.myeslib.sampledomain.aggregates.inventoryitem.commands.IncreaseInventory;
import org.myeslib.sampledomain.aggregates.inventoryitem.events.InventoryDecreased;
import org.myeslib.sampledomain.aggregates.inventoryitem.events.InventoryIncreased;
import org.myeslib.data.UnitOfWorkId;
import org.myeslib.stack1.infra.exceptions.ConcurrencyException;
import org.myeslib.stack1.infra.helpers.DatabaseHelper;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class Stack1DaoTest {

    @Inject
    UnitOfWorkDao<UUID> dao;

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

//    @Test
//    public void tesetCmdSer() {
//
//        IncreaseInventory command = IncreaseInventory.create(new CommandId(UUID.randomUUID()), new CommandId(UUID.randomUUID()), 1);
//
//        String asString = cmdSer.toStringFunction.apply(command);
//
//        Command cmd = cmdSer.fromStringFunction.apply(asString);
//
//        assertThat(command, is(cmd));
//
//    }
//
//    @Test
//    public void tesetUowSer() {
//
//        UUID id = UUID.randomUUID();
//
//        IncreaseInventory command = IncreaseInventory.create(new CommandId(UUID.randomUUID()), id, 1);
//
//        UnitOfWork unitOfWork = UnitOfWork.create(new CommandId(UUID.randomUUID()), command.getCommandId(), 0L, Arrays.asList(InventoryIncreased.create(1)));
//
//        String asString = uowSer.toStringFunction.apply(unitOfWork);
//
//        UnitOfWork fromJson = uowSer.fromStringFunction.apply(asString);
//
//        assertThat(unitOfWork, is(fromJson));
//    }

    @Test
    public void firstTransactionOnEmptyHistory() {

        UUID id = UUID.randomUUID();

        IncreaseInventory command = IncreaseInventory.create(CommandId.create(), id, 1);

        UnitOfWork newUow = UnitOfWork.create(UnitOfWorkId.create(), command.getCommandId(), 0L, Arrays.asList(InventoryIncreased.create(1)));

        dao.append(command.targetId(), command.getCommandId(), command, newUow);

        List<UnitOfWork> fromDb = dao.getFull(id);

        assertThat(Lists.newArrayList(newUow), is(fromDb));

        assertThat(command, is(dao.getCommand(command.getCommandId())));

    }

    @Test
    public void appendNewOnPreviousVersion() {

        UUID id = UUID.randomUUID();

        IncreaseInventory command1 = IncreaseInventory.create(CommandId.create(), id, 1);
        DecreaseInventory command2 = DecreaseInventory.create(CommandId.create(), id, 1);

        UnitOfWork existingUow = UnitOfWork.create(UnitOfWorkId.create(), command1.getCommandId(), 0L, Arrays.asList(InventoryIncreased.create(1)));
        UnitOfWork newUow = UnitOfWork.create(UnitOfWorkId.create(), command2.getCommandId(), 1L, Arrays.asList(InventoryDecreased.create((1))));

        dao.append(command1.targetId(), command1.getCommandId(), command1, existingUow);
        dao.append(command2.targetId(), command2.getCommandId(), command2, newUow);

        List<UnitOfWork> fromDb = dao.getFull(id);

        assertThat(Lists.newArrayList(existingUow, newUow), is(fromDb));
        assertThat(command1, is(dao.getCommand(command1.getCommandId())));
        assertThat(command2, is(dao.getCommand(command2.getCommandId())));
        assertThat(fromDb.get(fromDb.size()-1).getVersion(), is(2L));


    }

    @Test(expected = ConcurrencyException.class)
    public void databaseIsHandlingOptimisticLocking() {

        UUID id = UUID.randomUUID();

        IncreaseInventory command1 = IncreaseInventory.create(CommandId.create(), id, 1);
        DecreaseInventory command2 = DecreaseInventory.create(CommandId.create(), id, 1);

        UnitOfWork existingUow = UnitOfWork.create(UnitOfWorkId.create(), command1.getCommandId(), 0L, Arrays.asList(InventoryIncreased.create((1))));

        dao.append(id, command1.getCommandId(), command1, existingUow);

        UnitOfWork newUow = UnitOfWork.create(UnitOfWorkId.create(), command2.getCommandId(), 0L, Arrays.asList(InventoryDecreased.create((1))));

        dao.append(command2.targetId(), command2.getCommandId(), command2, newUow);

    }

}
