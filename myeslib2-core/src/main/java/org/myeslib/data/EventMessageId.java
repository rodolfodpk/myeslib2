package org.myeslib.data;

import net.jcip.annotations.Immutable;

import java.io.Serializable;
import java.util.UUID;

@Immutable
public class EventMessageId  implements Serializable {

    private final UUID uuid;

    public EventMessageId(UUID uuid) {
        this.uuid =  uuid;
    }

    public UUID uuid() {
        return uuid;
    }

    public static EventMessageId create(UUID uuid) {
        return new EventMessageId(uuid);
    }

    public static EventMessageId create() {
        return new EventMessageId(UUID.randomUUID());
    }

    public String toString() {
        return uuid().toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EventMessageId commandId = (EventMessageId) o;

        if (!uuid.equals(commandId.uuid)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }
}
