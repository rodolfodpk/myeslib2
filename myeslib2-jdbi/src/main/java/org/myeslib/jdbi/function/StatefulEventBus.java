package org.myeslib.jdbi.function;

import com.google.common.eventbus.EventBus;
import org.myeslib.core.AggregateRoot;
import org.myeslib.core.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class StatefulEventBus {

    private static final Logger logger = LoggerFactory.getLogger(StatefulEventBus.class);
    private final AggregateRoot aggregateRoot;
    private final EventBus bus;

    public List<Event> getEvents() {
        return events;
    }

    private final List<Event> events;

    public StatefulEventBus(AggregateRoot aggregateRoot, EventBus bus, List<Event> events) {
        this.aggregateRoot = aggregateRoot;
        this.bus = bus;
        this.events = events;
        bus.register(aggregateRoot);
    }

    public StatefulEventBus(AggregateRoot aggregateRoot, EventBus bus) {
        this.aggregateRoot = aggregateRoot;
        this.bus = bus;
        this.events = new ArrayList<>();
        bus.register(aggregateRoot);
    }

    public void post(Event event) {
        logger.info("applying event {} on {}", event, aggregateRoot);
        bus.post(event);
        logger.info("after event {}", aggregateRoot);
        events.add(event);

    }

}
