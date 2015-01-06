package org.myeslib.core.data;

import org.myeslib.core.AggregateRoot;

import java.io.Serializable;

import static java.util.Objects.requireNonNull;

@SuppressWarnings("serial")
public class Snapshot<A extends AggregateRoot> implements Serializable {

    private final A aggregateInstance;
    private final Long version;

    public Snapshot(A aggregateInstance, Long version) {
        requireNonNull(aggregateInstance);
        this.aggregateInstance = aggregateInstance;
        requireNonNull(version);
        this.version = version;
    }

    public A getAggregateInstance() {
        return aggregateInstance;
    }

    public Long getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Snapshot snapshot = (Snapshot) o;

        if (!aggregateInstance.equals(snapshot.aggregateInstance)) return false;
        if (!version.equals(snapshot.version)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = aggregateInstance.hashCode();
        result = 31 * result + version.hashCode();
        return result;
    }

    @Override public String toString() {
        return "Snapshot{" +
                "aggregateInstance=" + aggregateInstance +
                ", version=" + version +
                '}';
    }

}
