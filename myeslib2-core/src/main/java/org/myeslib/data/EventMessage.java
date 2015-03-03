package org.myeslib.data;

import net.jcip.annotations.Immutable;

@Immutable
public class EventMessage {

    private final EventId eventId;
    private final Event event;

    public EventMessage(EventId eventId, Event event) {
        this.eventId = eventId;
        this.event = event;
    }

    public EventId getEventId() {
        return eventId;
    }

    public Event getEvent() {
        return event;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EventMessage that = (EventMessage) o;

        if (!event.equals(that.event)) return false;
        if (!eventId.equals(that.eventId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = eventId.hashCode();
        result = 31 * result + event.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "EventMessage{" +
                "eventId=" + eventId +
                ", event=" + event +
                '}';
    }
}
