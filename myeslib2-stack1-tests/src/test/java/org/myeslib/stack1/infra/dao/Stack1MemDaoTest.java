package org.myeslib.stack1.infra.dao;

import com.google.common.collect.Lists;
import com.google.inject.*;
import com.google.inject.util.Modules;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.myeslib.InventoryItemGuavaModule;
import org.myeslib.data.CommandId;
import org.myeslib.data.EventMessage;
import org.myeslib.data.UnitOfWork;
import org.myeslib.data.UnitOfWorkId;
import org.myeslib.infra.WriteModelDao;
import org.myeslib.infra.exceptions.ConcurrencyException;
import sampledomain.aggregates.inventoryitem.InventoryItemStack1Module;
import sampledomain.aggregates.inventoryitem.commands.DecreaseInventory;
import sampledomain.aggregates.inventoryitem.commands.IncreaseInventory;
import sampledomain.aggregates.inventoryitem.events.InventoryDecreased;
import sampledomain.aggregates.inventoryitem.events.InventoryIncreased;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class Stack1MemDaoTest {

    @Inject
    WriteModelDao<UUID> dao;

    static Injector injector;

    @BeforeClass
    public static void setup() throws Exception {
        final Consumer<EventMessage> mockConsumer = Mockito.mock(Consumer.class);
        List<Consumer<EventMessage>> consumerList = Lists.newArrayList(mockConsumer);
        injector = Guice.createInjector(Modules.override(new InventoryItemStack1Module(), new InventoryItemGuavaModule()).with(new AbstractModule() {
            @Override
            protected void configure() {
                bind(new TypeLiteral<List<Consumer<EventMessage>>>() {}).toInstance(consumerList);
                bind(new TypeLiteral<WriteModelDao<UUID>>() {}).to(new TypeLiteral<Stack1MemDao<UUID>>() {}).asEagerSingleton();
            }
        }));
    }

    @Before
    public void init() throws Exception {
        injector.injectMembers(this);
    }

    @Test
    public void firstTransactionOnEmptyHistory() {

        UUID id = UUID.randomUUID();

        IncreaseInventory command = IncreaseInventory.create(CommandId.create(), id, 1);

        UnitOfWork newUow = UnitOfWork.create(UnitOfWorkId.create(), command.getCommandId(), 0L, Arrays.asList(InventoryIncreased.create(1)));

        dao.append(command.targetId(), command, newUow);

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

        dao.append(command1.targetId(), command1, existingUow);
        dao.append(command2.targetId(), command2, newUow);

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

        dao.append(id, command1, existingUow);

        UnitOfWork newUow = UnitOfWork.create(UnitOfWorkId.create(), command2.getCommandId(), 0L, Arrays.asList(InventoryDecreased.create((1))));

        dao.append(command2.targetId(), command2, newUow);

    }

}
