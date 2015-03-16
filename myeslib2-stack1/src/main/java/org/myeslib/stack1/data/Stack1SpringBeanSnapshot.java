package org.myeslib.stack1.data;

import net.jcip.annotations.Immutable;
import org.myeslib.core.AggregateRoot;
import org.myeslib.data.Snapshot;
import org.myeslib.stack1.infra.MultiMethodInteractionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import static com.google.common.base.Preconditions.checkNotNull;

@SuppressWarnings("serial")
@Immutable
public class Stack1SpringBeanSnapshot<A extends AggregateRoot> implements Snapshot<A> {

    static final Logger logger = LoggerFactory.getLogger(Stack1SpringBeanSnapshot.class);

    final A aggregateInstance;
    final Long version;

    public Stack1SpringBeanSnapshot(A aggregateInstance, Long version) {
        checkNotNull(aggregateInstance);
        this.aggregateInstance = aggregateInstance;
        checkNotNull(version);
        this.version = version;
    }

    @Override
    public A getAggregateInstance() {
        final A newInstance;
        try {
            newInstance = (A) aggregateInstance.getClass().newInstance();
            BeanUtils.copyProperties(aggregateInstance, newInstance);
            newInstance.setInteractionContext(new MultiMethodInteractionContext(newInstance));
        } catch (Exception e) {
            throw new RuntimeException(e.getCause());
        }
        return newInstance;
    }

    @Override
    public Long getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Stack1SpringBeanSnapshot snapshot = (Stack1SpringBeanSnapshot) o;

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
