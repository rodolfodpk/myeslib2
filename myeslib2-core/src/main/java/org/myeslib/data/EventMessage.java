package org.myeslib.data;


import java.io.Serializable;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkNotNull;

public class EventMessage implements Comparable<EventMessage>, Serializable {

    private final EventMessageId eventMessageId;
    private final Event event;
    private final String aggregateTargetId;
    private final Long aggregateTargetVersion;

    public EventMessage(EventMessageId eventMessageId, Event event, String aggregateTargetId, Long aggregateTargetVersion) {
        checkNotNull(eventMessageId);
        this.eventMessageId = eventMessageId;
        checkNotNull(event);
        this.event = event;
        checkNotNull(aggregateTargetId);
        this.aggregateTargetId = aggregateTargetId;
        checkNotNull(aggregateTargetVersion);
        this.aggregateTargetVersion = aggregateTargetVersion;
    }

    public EventMessageId getEventMessageId() {
        return eventMessageId;
    }

    public Event getEvent() {
        return event;
    }

    public String getAggregateTargetId() {
        return aggregateTargetId;
    }

    public Long getAggregateTargetVersion() {
        return aggregateTargetVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EventMessage that = (EventMessage) o;

        if (!eventMessageId.equals(that.eventMessageId)) return false;
        if (!event.equals(that.event)) return false;
        if (!aggregateTargetId.equals(that.aggregateTargetId)) return false;
        return aggregateTargetVersion.equals(that.aggregateTargetVersion);

    }

    @Override
    public int hashCode() {
        int result = eventMessageId.hashCode();
        result = 31 * result + event.hashCode();
        result = 31 * result + aggregateTargetId.hashCode();
        result = 31 * result + aggregateTargetVersion.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "EventMessage{" +
                "eventMessageId=" + eventMessageId +
                ", event=" + event +
                ", aggregateTargetId=" + aggregateTargetId +
                ", aggregateTargetVersion=" + aggregateTargetVersion +
                '}';
    }

    @Override
    public int compareTo(EventMessage o) {
        if (Objects.equals(aggregateTargetVersion, o.aggregateTargetVersion) ||
                Objects.equals(getClass(), o.getEvent().getClass()) ||
                Objects.equals(aggregateTargetId, o.aggregateTargetId)
                ) {
            return 0;
        }
        return aggregateTargetVersion > o.aggregateTargetVersion ? 1: -1;
    }
}
