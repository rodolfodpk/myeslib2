package org.myeslib.jdbi.helpers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.myeslib.jdbi.helpers.gson.RuntimeTypeAdapterFactory;
import org.myeslib.core.AggregateRoot;
import org.myeslib.core.Command;
import org.myeslib.core.Event;

import java.lang.reflect.Modifier;

/*
 * Produces a Gson instance able to ser/deserialize polymorfic types. 
 * Jackson is probably faster but it requires those @JsonCreator and @JsonProperty annotations which can be a bit verbose and error prone.
 */
public class SampleDomainGsonFactory {
	
	private final Gson gson;
	
	public SampleDomainGsonFactory() {

		final RuntimeTypeAdapterFactory<AggregateRoot> aggregateRootAdapter =
				RuntimeTypeAdapterFactory.of(AggregateRoot.class)
				.registerSubtype(SampleDomain.InventoryItemAggregateRoot.class, SampleDomain.InventoryItemAggregateRoot.class.getSimpleName());

		final RuntimeTypeAdapterFactory<Command> commandAdapter =
				RuntimeTypeAdapterFactory.of(Command.class)
				.registerSubtype(SampleDomain.CreateInventoryItem.class, SampleDomain.CreateInventoryItem.class.getSimpleName())
				.registerSubtype(SampleDomain.IncreaseInventory.class, SampleDomain.IncreaseInventory.class.getSimpleName())
				.registerSubtype(SampleDomain.DecreaseInventory.class, SampleDomain.DecreaseInventory.class.getSimpleName());

		final RuntimeTypeAdapterFactory<Event> eventAdapter = 
				RuntimeTypeAdapterFactory.of(Event.class)
				.registerSubtype(SampleDomain.InventoryItemCreated.class, SampleDomain.InventoryItemCreated.class.getSimpleName())
				.registerSubtype(SampleDomain.InventoryIncreased.class, SampleDomain.InventoryIncreased.class.getSimpleName())
				.registerSubtype(SampleDomain.InventoryDecreased.class, SampleDomain.InventoryDecreased.class.getSimpleName());
		
		
		this.gson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.TRANSIENT)
			.registerTypeAdapterFactory(aggregateRootAdapter)
			.registerTypeAdapterFactory(commandAdapter)
			.registerTypeAdapterFactory(eventAdapter)
			//.setPrettyPrinting()
			.create();
	
	}

	public Gson create() {
		return gson;
	}

}
