package org.myeslib.stack1.infra;

import com.google.common.cache.Cache;
import org.myeslib.core.EventSourced;
import org.myeslib.data.Event;
import org.myeslib.data.UnitOfWork;
import org.myeslib.infra.Snapshot;
import org.myeslib.infra.SnapshotReader;
import org.myeslib.infra.WriteModelDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.myeslib.stack1.infra.helpers.Preconditions.checkNotNull;

public class Stack1Reader<K, E extends EventSourced> implements SnapshotReader<K, E> {

    private static final Logger logger = LoggerFactory.getLogger(Stack1Reader.class);

    private final Supplier<E> supplier;
    private final WriteModelDao<K, E> dao;
    private final Cache<K, Snapshot<E>> cache;
    private final BiFunction<E, List<Event>, E> applyEventsFunction;
    private final SnapshotFactory<E> snapshotFactory;

    @Inject
    public Stack1Reader(Supplier<E> supplier,
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
        final Snapshot<E> lastSnapshot;
        final AtomicBoolean wasDaoCalled = new AtomicBoolean(false);
        try {
            logger.debug("id {} cache.get(id)", id);
            lastSnapshot = cache.get(id, () -> {
                logger.debug("id {} cache.get(id) does not contain anything for this id. Will have to search on dao", id);
                wasDaoCalled.set(true);
                final List<UnitOfWork> unitOfWorkList = dao.getFull(id);
                final E currentSnapshot = applyEventsFunction.apply(supplier.get(), flatMap(unitOfWorkList));
                return snapshotFactory.create(currentSnapshot, lastVersion(unitOfWorkList));
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
        final E ar = applyEventsFunction.apply(lastSnapshot.getAggregateInstance(), flatMap(partialTransactionHistory));
        final Snapshot<E> latestSnapshot = snapshotFactory.create(ar, lastVersion(partialTransactionHistory));
        cache.put(id, latestSnapshot); // TODO assert this on tests
        return latestSnapshot;
    }

}
