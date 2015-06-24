package org.myeslib.data;

import org.myeslib.core.EventSourced;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

@SuppressWarnings("serial")
public class Snapshot<A extends EventSourced> {

    static final Logger logger = LoggerFactory.getLogger(Snapshot.class);

    final A aggregateInstance;
    final Long version;

    public Snapshot(A aggregateInstance, Long version) {
        checkNotNull(aggregateInstance);
        this.aggregateInstance = aggregateInstance;
        checkNotNull(version);
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
