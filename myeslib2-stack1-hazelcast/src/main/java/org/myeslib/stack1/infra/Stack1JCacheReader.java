package org.myeslib.stack1.infra;

import org.myeslib.core.EventSourced;
import org.myeslib.data.Event;
import org.myeslib.data.UnitOfWork;
import org.myeslib.infra.Snapshot;
import org.myeslib.infra.SnapshotReader;
import org.myeslib.infra.WriteModelDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.Cache;
import javax.inject.Inject;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

public class Stack1JCacheReader<K, A extends EventSourced> implements SnapshotReader<K, A> {

    private static final Logger logger = LoggerFactory.getLogger(Stack1JCacheReader.class);

    private final Supplier<A> supplier;
    private final WriteModelDao<K> dao;
    private final Cache<K, Snapshot<A>> cache;
    private final BiFunction<A, List<Event>, A> applyEventsFunction;
    private final SnapshotFactory<A> snapshotFactory;

    // http://stackoverflow.com/questions/27703342/exception-while-trying-to-make-hazelcast-cluster-work-with-jcache-compliant-clie

    // TODO use CacheLoader and CacheWriter
    @Inject
    public Stack1JCacheReader(Supplier<A> supplier,
                              WriteModelDao<K> dao,
                              Cache<K, Snapshot<A>> cache,
                              BiFunction<A, List<Event>, A> ApplyEventsFunction,
                              SnapshotFactory<A> snapshotFactory) {

        this.supplier = supplier;
        this.dao = dao;
        this.cache = cache;
        this.applyEventsFunction = ApplyEventsFunction;
        this.snapshotFactory = snapshotFactory;
    }

    /*
     * (non-Javadoc)
     * @see org.myeslib.infra.SnapshotReader#getSnapshot(java.lang.Object)
     */
    public Snapshot<A> getSnapshot(final K id) {
        checkNotNull(id);
        logger.debug("id {} cache.get(id)", id);
        if (!cache.containsKey(id)) {
            logger.debug("id {} cache.get(id) does not contain anything for this id. Will have to search on dao", id);
            final List<UnitOfWork> unitOfWorkList = dao.getFull(id);
            final A currentSnapshot = applyEventsFunction.apply(supplier.get(), flatMap(unitOfWorkList));
            final Snapshot<A> result = snapshotFactory.create(currentSnapshot, lastVersion(unitOfWorkList));
            cache.put(id, result);
            return result;
        }
        final Snapshot<A> lastSnapshot = cache.get(id);
        logger.debug("id {} lastSnapshot has version {}. will check if there any version beyond it", id, lastSnapshot.getVersion());
        final List<UnitOfWork> partialTransactionHistory = dao.getPartial(id, lastSnapshot.getVersion());
        if (partialTransactionHistory.isEmpty()) {
            return lastSnapshot;
        }
        logger.debug("id {} found {} pending transactions. Last version is {}", id, partialTransactionHistory.size(), lastVersion(partialTransactionHistory));
        final A ar = applyEventsFunction.apply(lastSnapshot.getAggregateInstance(), flatMap(partialTransactionHistory));
        final Snapshot<A> latestSnapshot = snapshotFactory.create(ar, lastVersion(partialTransactionHistory));
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
