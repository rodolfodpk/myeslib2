package sampledomain.aggregates.inventoryitem.modules;

import com.google.gson.Gson;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.myeslib.data.Command;
import org.myeslib.data.UnitOfWork;
import org.myeslib.infra.dao.config.CmdSerialization;
import org.myeslib.infra.dao.config.UowSerialization;
import sampledomain.aggregates.inventoryitem.InventoryItem;
import sampledomain.aggregates.inventoryitem.commands.CommandsGsonFactory;
import sampledomain.aggregates.inventoryitem.events.EventsGsonFactory;

import javax.inject.Named;
import javax.inject.Singleton;

public class InventoryItemGsonModule extends AbstractModule {

    @Provides
    @Singleton
    public UowSerialization<InventoryItem> uowSerialization(@Named("events-json") Gson gson) {
        return new UowSerialization<>(
                (uow) -> gson.toJson(uow),
                (json) -> gson.fromJson(json, UnitOfWork.class));
    }

    @Provides
    @Singleton
    public CmdSerialization<InventoryItem> cmdSerialization(@Named("commands-json") Gson gson) {
        return new CmdSerialization<>(
                (cmd) -> gson.toJson(cmd, Command.class),
                (json) -> gson.fromJson(json, Command.class));
    }

    @Provides
    @Named("events-json")
    @Singleton
    public Gson gsonEvents() {
        return new EventsGsonFactory().create();
    }

    @Provides
    @Named("commands-json")
    @Singleton
    public Gson gsonCommands() {
        return new CommandsGsonFactory().create();
    }

    @Override
    protected void configure() {

    }
}
