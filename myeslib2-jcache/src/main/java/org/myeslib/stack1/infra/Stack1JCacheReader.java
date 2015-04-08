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

import static org.myeslib.stack1.infra.helpers.Preconditions.checkNotNull;

public class Stack1JCacheReader<K, E extends EventSourced> implements SnapshotReader<K, E> {

    private static final Logger logger = LoggerFactory.getLogger(Stack1JCacheReader.class);

    private final Supplier<E> supplier;
    private final WriteModelDao<K, E> dao;
    private final Cache<K, Snapshot<E>> cache;
    private final BiFunction<E, List<Event>, E> applyEventsFunction;
    private final SnapshotFactory<E> snapshotFactory;

    // http://stackoverflow.com/questions/27703342/exception-while-trying-to-make-hazelcast-cluster-work-with-jcache-compliant-clie

    // TODO use CacheLoader and CacheWriter
    @Inject
    public Stack1JCacheReader(Supplier<E> supplier,
                              WriteModelDao<K, E> dao,
                              Cache<K, Snapshot<E>> cache,
                              BiFunction<E, List<Event>, E> ApplyEventsFunction,
                              SnapshotFactory<E> snapshotFactory) {

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
    public Snapshot<E> getSnapshot(final K id) {
        checkNotNull(id);
        logger.debug("id {} cache.get(id)", id);
        if (!cache.containsKey(id)) {
            logger.debug("id {} cache.get(id) does not contain anything for this id. Will have to search on dao", id);
            final List<UnitOfWork> unitOfWorkList = dao.getFull(id);
            final E currentSnapshot = applyEventsFunction.apply(supplier.get(), flatMap(unitOfWorkList));
            final Snapshot<E> result = snapshotFactory.create(currentSnapshot, lastVersion(unitOfWorkList));
            cache.put(id, result);
            return result;
        }
        final Snapshot<E> lastSnapshot = cache.get(id);
        logger.debug("id {} lastSnapshot has version {}. will check if there any version beyond it", id, lastSnapshot.getVersion());
        final List<UnitOfWork> partialTransactionHistory = dao.getPartial(id, lastSnapshot.getVersion());
        if (partialTransactionHistory.isEmpty()) {
            return lastSnapshot;
        }
        logger.debug("id {} found {} pending transactions. Last version is {}", id, partialTransactionHistory.size(), lastVersion(partialTransactionHistory));
        final E ar = applyEventsFunction.apply(lastSnapshot.getAggregateInstance(), flatMap(partialTransactionHistory));
        final Snapshot<E> latestSnapshot = snapshotFactory.create(ar, lastVersion(partialTransactionHistory));
        cache.put(id, latestSnapshot); // TODO assert this on tests
        return latestSnapshot;
    }

}
