package org.myeslib.stack1.infra;

import com.google.common.cache.Cache;
import org.myeslib.core.AggregateRoot;
import org.myeslib.data.Event;
import org.myeslib.data.UnitOfWork;
import org.myeslib.data.Snapshot;
import org.myeslib.infra.ApplyEventsFunction;
import org.myeslib.infra.SnapshotReader;
import org.myeslib.infra.UnitOfWorkDao;
import org.myeslib.stack1.data.Stack1SpringBeanSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

public class Stack1Reader<K, A extends AggregateRoot> implements SnapshotReader<K, A> {

    private static final Logger logger = LoggerFactory.getLogger(Stack1Reader.class);

    private final Supplier<A> supplier;
    private final UnitOfWorkDao<K> dao;
    private final Cache<K, Snapshot<A>> cache;
    private final ApplyEventsFunction<A> applyEventsFunction;

    public Stack1Reader(Supplier<A> supplier, UnitOfWorkDao<K> dao,
                        Cache<K, Snapshot<A>> cache,
                        ApplyEventsFunction<A> ApplyEventsFunction) {
        checkNotNull(supplier);
        this.supplier = supplier;
        checkNotNull(dao);
        this.dao = dao;
        checkNotNull(cache);
        this.cache = cache;
        checkNotNull(ApplyEventsFunction);
        this.applyEventsFunction = ApplyEventsFunction;
    }

    /*
     * (non-Javadoc)
     * @see org.myeslib.infra.SnapshotReader#getSnapshot(java.lang.Object)
     */
    public Snapshot<A> getSnapshot(final K id) {
        checkNotNull(id);
        final Snapshot<A> lastSnapshot;
        final AtomicBoolean wasDaoCalled = new AtomicBoolean(false);
        try {
            logger.debug("id {} cache.get(id)", id);
            lastSnapshot = cache.get(id, () -> {
                logger.debug("id {} cache.get(id) does not contain anything for this id. Will have to search on dao", id);
                wasDaoCalled.set(true);
                final List<UnitOfWork> unitOfWorkList = dao.getFull(id);
                return new Stack1SpringBeanSnapshot<A>(applyEventsFunction.apply(supplier.get(), flatMap(unitOfWorkList)), lastVersion(unitOfWorkList));
            });
        } catch (ExecutionException e) {
            throw new RuntimeException(e.getCause());
        }
        logger.debug("id {} wasDaoCalled ? {}", id, wasDaoCalled.get());
        if (wasDaoCalled.get()) {
            return lastSnapshot;
        }
        logger.debug("id {} lastSnapshot has version {}. will check if there any version beyond it", id, lastSnapshot.getVersion());
        final List<UnitOfWork> partialTransactionHistory = dao.getPartial(id, lastSnapshot.getVersion());
        if (partialTransactionHistory.isEmpty()) {
            return lastSnapshot;
        }
        logger.debug("id {} found {} pending transactions. Last version is {}", id, partialTransactionHistory.size(), lastVersion(partialTransactionHistory));
        final A ar = applyEventsFunction.apply(lastSnapshot.getAggregateInstance(), flatMap(partialTransactionHistory));
 //       final Snapshot<A> latestSnapshot = new Stack1KryoSnapshot<>(ar, lastVersion(partialTransactionHistory), kryo);
        final Snapshot<A> latestSnapshot = new Stack1SpringBeanSnapshot<>(ar, lastVersion(partialTransactionHistory));
        cache.put(id, latestSnapshot); // TODO assert this on tests
        return latestSnapshot;
    }

    List<Event> flatMap(final List<UnitOfWork> unitOfWorks) {
        return unitOfWorks.stream().flatMap((unitOfWork) -> unitOfWork.getEvents().stream()).collect(Collectors.toList());
    }

    Long lastVersion(List<UnitOfWork> unitOfWorks) {
        return unitOfWorks.isEmpty() ? 0L : unitOfWorks.get(unitOfWorks.size()-1).getVersion();
    }
}
