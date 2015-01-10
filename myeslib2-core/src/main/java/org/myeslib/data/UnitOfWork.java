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
    private final Command command;
    private final List<? extends Event> events;
    private final Long version;

    public UnitOfWork(UUID id, Command command, Long version, List<? extends Event> events) {
        requireNonNull(id, "id cannot be null");
        requireNonNull(command, "command cannot be null");
        targetVersionIsBiggerThanZero(command.getTargetVersion());
        versionIsBiggerThanZero(version);
        requireNonNull(events, "events cannot be null");
        for (Event e : events) {
            requireNonNull(e, "event within events list cannot be null");
        }
        this.id = id;
        this.command = command;
        this.version = version;
        this.events = events;
    }

    private static void targetVersionIsBiggerThanZero(Long targetVersion) {
        if (!(targetVersion >= 0L)) {
            throw new IllegalArgumentException("target version must be >= 0");
        }
    }

    private static void versionIsBiggerThanZero(Long version) {
        if (!(version > 0L)) {
            throw new IllegalArgumentException("version must be > 0");
        }
    }

    public static UnitOfWork create(UUID id, Command command, List<? extends Event> newEvents) {
        requireNonNull(command.getTargetVersion(), "target version cannot be null");
        targetVersionIsBiggerThanZero(command.getTargetVersion());
        return new UnitOfWork(id, command, command.getTargetVersion() + 1, newEvents);
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

    public Long getTargetVersion() {
        return command.getTargetVersion();
    }

    public UUID getId() {
        return id;
    }

    public Command getCommand() {
        return command;
    }

    public Long getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UnitOfWork that = (UnitOfWork) o;

        if (!command.equals(that.command)) return false;
        if (!events.equals(that.events)) return false;
        if (!id.equals(that.id)) return false;
        if (!version.equals(that.version)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + command.hashCode();
        result = 31 * result + events.hashCode();
        result = 31 * result + version.hashCode();
        return result;
    }

    @Override public String toString() {
        return "UnitOfWork{" +
                "id=" + id +
                ", command=" + command +
                ", events=" + events +
                ", version=" + version +
                '}';
    }
}
