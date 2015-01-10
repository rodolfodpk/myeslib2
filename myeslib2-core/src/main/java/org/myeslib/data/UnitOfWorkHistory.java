package org.myeslib.data;

import org.myeslib.core.Event;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

@SuppressWarnings("serial")
public class UnitOfWorkHistory implements Serializable {

    private final List<UnitOfWork> unitsOfWork;

    public UnitOfWorkHistory() {
        this.unitsOfWork = new LinkedList<>();
    }

    public List<Event> getAllEvents() {
        return Collections.unmodifiableList(getEventsAfterUntil(0, Long.MAX_VALUE));
    }

    public List<Event> getEventsAfterUntil(long afterVersion, long untilVersion) {
        List<Event> events = new LinkedList<>();
        unitsOfWork.stream().filter(t -> t.getVersion() > afterVersion && t.getVersion() <= untilVersion).forEach(t -> {
            events.addAll(t.getEvents().stream().collect(Collectors.toList()));
        });
        return Collections.unmodifiableList(events);
    }

    public List<Event> getEventsUntil(long version) {
        List<Event> events = new LinkedList<>();
        unitsOfWork.stream().filter(t -> t.getVersion() <= version).forEach(t -> {
            events.addAll(t.getEvents().stream().collect(Collectors.toList()));
        });
        return Collections.unmodifiableList(events);
    }

    public Long getLastVersion() {
        return unitsOfWork.size() == 0 ? 0 : unitsOfWork.get(unitsOfWork.size() - 1).getVersion();
    }

    public List<UnitOfWork> getUnitsOfWork() {
        return unitsOfWork;
    }

    public void add(final UnitOfWork transaction) {
        requireNonNull(transaction);
        unitsOfWork.add(transaction);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UnitOfWorkHistory that = (UnitOfWorkHistory) o;

        if (!unitsOfWork.equals(that.unitsOfWork)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return unitsOfWork.hashCode();
    }

    @Override public String toString() {
        return "AggregateRootHistory{" +
                "unitsOfWork=" + unitsOfWork +
                '}';
    }
}
