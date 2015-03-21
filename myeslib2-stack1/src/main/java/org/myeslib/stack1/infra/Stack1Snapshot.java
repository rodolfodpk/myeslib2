package org.myeslib.stack1.infra;

import net.jcip.annotations.Immutable;
import org.myeslib.core.EventSourced;
import org.myeslib.infra.Snapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;

@SuppressWarnings("serial")
@Immutable
public class Stack1Snapshot<A extends EventSourced> implements Snapshot<A> {

    static final Logger logger = LoggerFactory.getLogger(Stack1Snapshot.class);

    final A aggregateInstance;
    final Long version;
    final Supplier<A> supplier;
    final Function<A, A> injectFunction;

    public Stack1Snapshot(A aggregateInstance, Long version, Supplier<A> supplier, Function<A, A> injectFunction) {
        checkNotNull(aggregateInstance);
        this.aggregateInstance = aggregateInstance;
        checkNotNull(version);
        this.version = version;
        checkNotNull(supplier);
        this.supplier = supplier;
        checkNotNull(injectFunction);
        this.injectFunction = injectFunction;
    }

    @Override
    public A getAggregateInstance() {
        final A newInstance = supplier.get();
        try {
            final Field[] fields = aggregateInstance.getClass().getFields();
            for (Field field : fields) {
                Object value = field.get(aggregateInstance);
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                logger.debug("will set value {} to field {} ", value, field);
                field.set(newInstance, value);
            }
            return injectFunction.apply(newInstance);
        } catch (Exception e) {
            e.printStackTrace();
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

        Stack1Snapshot snapshot = (Stack1Snapshot) o;

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
