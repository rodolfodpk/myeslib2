package sampledomain.aggregates.inventoryitem;

import autovalue.shaded.com.google.common.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.myeslib.data.EventMessage;
import org.myeslib.data.EventMessageId;
import org.myeslib.stack1.infra.helpers.jdbi.DatabaseHelper;
import sampledomain.aggregates.inventoryitem.events.InventoryItemCreated;
import sampledomain.aggregates.inventoryitem.modules.InventoryItemGsonModule;
import sampledomain.aggregates.inventoryitem.modules.InventoryItemJdbiModule;
import sampledomain.aggregates.inventoryitem.modules.InventoryItemModule;

import javax.inject.Named;
import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

@RunWith(MockitoJUnitRunner.class)
public class InventoryItemGsonTest {

    Injector injector;

    @Inject
    @Named("events-json")
    Gson gson;

    @Before
    public void setup() {
        injector = Guice.createInjector(new InventoryItemModule(), new InventoryItemJdbiModule(), new InventoryItemGsonModule());
        injector.injectMembers(this);
        injector.getInstance(DatabaseHelper.class).initDb();
    }

    @Test
    public void eventMessage() {

        InventoryItemCreated event = InventoryItemCreated.create(UUID.randomUUID(), "test");

        List<EventMessage> events = Lists.newArrayList(new EventMessage(EventMessageId.create(), event, event.id().toString(), 0L));

        Type type = new TypeToken<List<EventMessage>>() {}.getType();

        String asJson = gson.toJson(events, type);

        //System.out.println(asJson);

        List<EventMessage> eventsFromJson = gson.fromJson(asJson, type);

        assertThat(events, is(eventsFromJson));

    }
}
