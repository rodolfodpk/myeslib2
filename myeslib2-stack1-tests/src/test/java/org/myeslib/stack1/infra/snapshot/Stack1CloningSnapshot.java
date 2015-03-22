package org.myeslib.stack1.infra.snapshot;

import com.rits.cloning.Cloner;
import net.jcip.annotations.Immutable;
import org.myeslib.core.AggregateRoot;
import org.myeslib.infra.Snapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

import static org.myeslib.stack1.infra.helpers.Preconditions.checkNotNull;

@SuppressWarnings("serial")
@Immutable
@Deprecated
public class Stack1CloningSnapshot<A extends AggregateRoot> implements Snapshot<A> {

    static final Logger logger = LoggerFactory.getLogger(Stack1CloningSnapshot.class);

    final A aggregateInstance;
    final Long version;
    final Cloner cloner;
    final Function<A, A> injectFunction;

    public Stack1CloningSnapshot(A aggregateInstance, Long version, Function<A, A> injectFunction) {
        checkNotNull(aggregateInstance);
        this.aggregateInstance = aggregateInstance;
        checkNotNull(version);
        this.version = version;
        this.cloner = new Cloner();
        this.cloner.setNullTransient(true);
        this.injectFunction = injectFunction;
    }

    @Override
    public A getAggregateInstance() {
        final A newInstance;
        try {
            newInstance = cloner.deepClone(aggregateInstance);
            return injectFunction.apply(newInstance);
        } catch (Exception e) {
            throw new RuntimeException(e.getCause());
        }
    }

    @Override
    public Long getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Stack1CloningSnapshot snapshot = (Stack1CloningSnapshot) o;

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
