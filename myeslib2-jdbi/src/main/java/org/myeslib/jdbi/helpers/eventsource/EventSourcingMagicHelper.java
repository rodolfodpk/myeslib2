package org.myeslib.jdbi.helpers.eventsource;

import org.myeslib.core.AggregateRoot;
import org.myeslib.core.Command;
import org.myeslib.core.CommandHandler;
import org.myeslib.core.Event;

import java.util.List;

/**
* Initial impl based on MultiMethod class
 * TODO To replace with Command and Event buses
 */
public class EventSourcingMagicHelper {
	
	@SuppressWarnings("unchecked")
	static public List<? extends Event> applyCommandOn(Command command, CommandHandler<? extends AggregateRoot> instance)  {
	   MultiMethod mm = MultiMethod.getMultiMethod(instance.getClass(), "handle");
	   final List<? extends Event> events;
       try {
           events = (List<? extends Event>) mm.invoke(instance, command);
       } catch (Exception e) {
           throw new RuntimeException("Error when executing with reflection");
       }
        return events;
	}
	
	static public void applyEventsOn(List<? extends Event> events, AggregateRoot instance)  {
	   MultiMethod mm = MultiMethod.getMultiMethod(instance.getClass(), "on");
	   for (Event event : events) {
           try {
               mm.invoke(instance, event);
           } catch (Exception e) {
               throw new RuntimeException("Error when executing with reflection");
           }
       }
	}

}
