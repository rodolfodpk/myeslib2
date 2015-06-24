package sampledomain.aggregates.inventoryitem.modules;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapterFactory;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.myeslib.data.Command;
import org.myeslib.data.Event;
import org.myeslib.data.UnitOfWork;
import org.myeslib.infra.dao.config.CmdSerialization;
import org.myeslib.infra.dao.config.UowSerialization;
import org.myeslib.stack1.infra.helpers.gson.polymorphic.RuntimeTypeAdapterFactory;
import sampledomain.aggregates.inventoryitem.InventoryItem;
import sampledomain.aggregates.inventoryitem.commands.*;
import sampledomain.aggregates.inventoryitem.events.*;

import javax.inject.Singleton;
import java.lang.reflect.Modifier;
import java.util.ServiceLoader;

public class InventoryItemGsonModule extends AbstractModule {

    @Provides
    @Singleton
    public Gson gson() {

        final RuntimeTypeAdapterFactory<Command> commandAdapter =
                RuntimeTypeAdapterFactory.of(Command.class)
                        .registerSubtype(ImmutableCreateInventoryItem.class, CreateInventoryItem.class.getSimpleName())
                        .registerSubtype(ImmutableIncreaseInventory.class, IncreaseInventory.class.getSimpleName())
                        .registerSubtype(ImmutableDecreaseInventory.class, DecreaseInventory.class.getSimpleName())
                        .registerSubtype(ImmutableCreateInventoryItemThenIncreaseThenDecrease.class, CreateInventoryItemThenIncreaseThenDecrease.class.getSimpleName());

        final RuntimeTypeAdapterFactory<Event> eventAdapter =
                RuntimeTypeAdapterFactory.of(Event.class)
                        .registerSubtype(ImmutableInventoryItemCreated.class, InventoryItemCreated.class.getSimpleName())
                        .registerSubtype(ImmutableInventoryIncreased.class, InventoryIncreased.class.getSimpleName())
                        .registerSubtype(ImmutableInventoryDecreased.class, InventoryDecreased.class.getSimpleName());

        return new GsonBuilder().setPrettyPrinting()
                                .excludeFieldsWithModifiers(Modifier.TRANSIENT)
                                .registerTypeAdapterFactory(commandAdapter)
                                .registerTypeAdapterFactory(eventAdapter).create();
    }

    @Provides
    @Singleton
    public UowSerialization<InventoryItem> uowSerialization(Gson gson) {
        return new UowSerialization<>(
                (uow) -> gson.toJson(uow),
                (json) -> gson.fromJson(json, UnitOfWork.class));
    }

    @Provides
    @Singleton
    public CmdSerialization<InventoryItem> cmdSerialization(Gson gson) {
        return new CmdSerialization<>(
                (cmd) -> gson.toJson(cmd, Command.class),
                (json) -> gson.fromJson(json, Command.class));
    }

    @Override
    protected void configure() {

    }
}
