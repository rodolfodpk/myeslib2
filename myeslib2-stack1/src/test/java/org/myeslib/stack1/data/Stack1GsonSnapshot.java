package org.myeslib.stack1.data;

import com.google.gson.Gson;
import net.jcip.annotations.Immutable;
import org.myeslib.core.AggregateRoot;
import org.myeslib.data.Snapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkNotNull;

@SuppressWarnings("serial")
@Immutable
@Deprecated
public class Stack1GsonSnapshot<A extends AggregateRoot> implements Snapshot<A> {

    static final Logger logger = LoggerFactory.getLogger(Stack1GsonSnapshot.class);

    final Class<A> clazz;
    final A aggregateInstance;
    final Long version;
    final Gson gson ;

    public Stack1GsonSnapshot(Class<A> clazz, A aggregateInstance, Long version, Gson gson) {
        checkNotNull(clazz);
        this.clazz = clazz;
        checkNotNull(aggregateInstance);
        this.aggregateInstance = aggregateInstance;
        checkNotNull(version);
        this.version = version;
        checkNotNull(gson);
        this.gson = gson;
    }

    @Override
    public A getAggregateInstance() {
        return gson.fromJson(gson.toJson(aggregateInstance, clazz), clazz);
    }

    @Override
    public Long getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Stack1GsonSnapshot snapshot = (Stack1GsonSnapshot) o;

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
