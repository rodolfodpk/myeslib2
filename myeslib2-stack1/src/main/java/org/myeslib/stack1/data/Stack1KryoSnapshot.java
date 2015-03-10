package org.myeslib.stack1.data;

import com.esotericsoftware.kryo.Kryo;
import net.jcip.annotations.Immutable;
import org.myeslib.core.AggregateRoot;
import org.myeslib.data.Snapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

@SuppressWarnings("serial")
@Immutable
public class Stack1KryoSnapshot<A extends AggregateRoot> implements Snapshot<A> {

    static final Logger logger = LoggerFactory.getLogger(Stack1KryoSnapshot.class);

    final A aggregateInstance;
    final Long version;
    final transient Kryo kryo;

    public Stack1KryoSnapshot(A aggregateInstance, Long version, Kryo kryo) {
        checkNotNull(aggregateInstance);
        this.aggregateInstance = aggregateInstance;
        checkNotNull(version);
        this.version = version;
        checkNotNull(kryo);
        this.kryo = kryo;
    }

    @Override
    public A getAggregateInstance() {
        return kryo.copy(aggregateInstance);
    }

    @Override
    public Long getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Stack1KryoSnapshot snapshot = (Stack1KryoSnapshot) o;

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
