package org.myeslib.data;

import net.jcip.annotations.Immutable;

import java.io.Serializable;

@Immutable
public class EventMessage implements Serializable {

    private final EventMessageId eventMessageId;
    private final Event event;

    public EventMessage(EventMessageId eventMessageId, Event event) {
        this.eventMessageId = eventMessageId;
        this.event = event;
    }

    public EventMessageId getEventMessageId() {
        return eventMessageId;
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
        if (!eventMessageId.equals(that.eventMessageId)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = eventMessageId.hashCode();
        result = 31 * result + event.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "EventMessage{" +
                "eventId=" + eventMessageId +
                ", event=" + event +
                '}';
    }
}
