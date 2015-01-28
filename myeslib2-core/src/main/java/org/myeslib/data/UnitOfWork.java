package org.myeslib.data;

import org.myeslib.core.Command;
import org.myeslib.core.Event;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

@SuppressWarnings("serial")
public class UnitOfWork implements Comparable<UnitOfWork>, Serializable {

    private final UUID id;
    private final UUID commandId;
    private final List<? extends Event> events;
    private final Long version;

    public UnitOfWork(UUID id, UUID commandId, Long version, List<? extends Event> events) {
        requireNonNull(id, "id cannot be null");
        requireNonNull(commandId, "commandId cannot be null");
        versionIsBiggerThanZero(version);
        requireNonNull(events, "events cannot be null");
        for (Event e : events) {
            requireNonNull(e, "event within events list cannot be null");
        }
        this.id = id;
        this.commandId = commandId;
        this.version = version;
        this.events = events;
    }

    private static void versionIsBiggerThanZero(Long version) {
        if (!(version > 0L)) {
            throw new IllegalArgumentException("version must be > 0");
        }
    }

    public static UnitOfWork create(UUID id, Command<?> command, Long snapshotVersion, List<? extends Event> newEvents) {
        return new UnitOfWork(id, command.getCommandId(), snapshotVersion + 1, newEvents);
    }

    public List<Event> getEvents() {
        List<Event> result = new LinkedList<>();
        for (Event e : events) {
            result.add(e);
        }
        return Collections.unmodifiableList(result);
    }

    public int compareTo(UnitOfWork other) {
        if (version < other.version) {
            return -1;
        } else if (version > other.version) {
            return 1;
        }
        return 0;
    }

    public UUID getId() {
        return id;
    }

    public UUID getCommandId() {
        return commandId;
    }

    public Long getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UnitOfWork that = (UnitOfWork) o;

        if (!commandId.equals(that.commandId)) return false;
        if (!events.equals(that.events)) return false;
        if (!id.equals(that.id)) return false;
        if (!version.equals(that.version)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + commandId.hashCode();
        result = 31 * result + events.hashCode();
        result = 31 * result + version.hashCode();
        return result;
    }

    @Override public String toString() {
        return "UnitOfWork{" +
                "id=" + id +
                ", commandId=" + commandId +
                ", events=" + events +
                ", version=" + version +
                '}';
    }
}
