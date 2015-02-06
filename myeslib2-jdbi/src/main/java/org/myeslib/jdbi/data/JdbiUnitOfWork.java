package org.myeslib.jdbi.data;

import net.jcip.annotations.Immutable;
import org.myeslib.core.Event;
import org.myeslib.data.UnitOfWork;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

@SuppressWarnings("serial")
@Immutable
public class JdbiUnitOfWork implements UnitOfWork {

    private final UUID id;
    private final UUID commandId;
    private final Long version;
    private final List<? extends Event> events;

    JdbiUnitOfWork(UUID id, UUID commandId, Long version, List<? extends Event> events) {
        this.id = id;
        this.commandId = commandId;
        this.version = version;
        this.events = events;
    }

    private static void versionIsZeroOrPositive(Long version) {
        if (!(version >= 0L)) {
            throw new IllegalArgumentException("version must be >= 0");
        }
    }

    public static UnitOfWork create(UUID id, UUID commandId, Long snapshotVersion, List<? extends Event> newEvents) {
        requireNonNull(id, "id cannot be null");
        requireNonNull(commandId, "commandId cannot be null");
        versionIsZeroOrPositive(snapshotVersion);
        requireNonNull(newEvents, "events cannot be null");
        for (Event e : newEvents) {
            requireNonNull(e, "event within events list cannot be null");
        }
        return new JdbiUnitOfWork(id, commandId, snapshotVersion + 1, newEvents);
    }

    @Override
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

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public UUID getCommandId() {
        return commandId;
    }

    @Override
    public Long getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JdbiUnitOfWork that = (JdbiUnitOfWork) o;

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
