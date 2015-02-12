package org.myeslib.data;

import net.jcip.annotations.Immutable;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static java.util.Objects.requireNonNull;

@Immutable
public class UnitOfWork implements Serializable, Comparable<UnitOfWork> {

    private final UnitOfWorkId id;
    private final CommandId commandId;
    private final Long version;
    private final List<? extends Event> events;

    UnitOfWork(UnitOfWorkId id, CommandId commandId, Long version, List<? extends Event> events) {
        this.id = id;
        this.commandId = commandId;
        this.version = version;
        this.events = events;
    }

    public static UnitOfWork create(UnitOfWorkId id, CommandId commandId, Long snapshotVersion, List<? extends Event> newEvents) {
        requireNonNull(id, "id cannot be null");
        requireNonNull(commandId, "commandId cannot be null");
        versionIsZeroOrPositive(snapshotVersion);
        requireNonNull(newEvents, "events cannot be null");
        for (Event e : newEvents) {
            requireNonNull(e, "event within events list cannot be null");
        }
        return new UnitOfWork(id, commandId, snapshotVersion + 1, newEvents);
    }

    private static void versionIsZeroOrPositive(Long version) {
        if (!(version >= 0L)) {
            throw new IllegalArgumentException("version must be >= 0");
        }
    }

    public List<Event> getEvents() {
        return Collections.unmodifiableList(new LinkedList<>(events));
    }

    public int compareTo(UnitOfWork other) {
        if (version < other.getVersion()) {
            return -1;
        } else if (version > other.getVersion()) {
            return 1;
        }
        return 0;
    }

    public UnitOfWorkId getId() {
        return id;
    }

    public CommandId getCommandId() {
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
