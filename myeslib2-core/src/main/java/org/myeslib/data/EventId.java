package org.myeslib.data;

import net.jcip.annotations.Immutable;

import java.util.UUID;

@Immutable
public class EventId {

    private final UUID uuid;

    public EventId(UUID uuid) {
        this.uuid =  uuid;
    }

    public UUID uuid() {
        return uuid;
    }

    public static EventId create(UUID uuid) {
        return new EventId(uuid);
    }

    public static EventId create() {
        return new EventId(UUID.randomUUID());
    }

    public String toString() {
        return uuid().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EventId commandId = (EventId) o;

        if (!uuid.equals(commandId.uuid)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }
}
