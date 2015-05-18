package org.myeslib.stack1.infra;

import com.google.common.cache.Cache;
import org.myeslib.core.EventSourced;
import org.myeslib.data.Event;
import org.myeslib.data.Snapshot;
import org.myeslib.data.SnapshotData;
import org.myeslib.data.UnitOfWork;
import org.myeslib.infra.SnapshotReader;
import org.myeslib.infra.WriteModelDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkNotNull;

public class Stack1SnapshotReader<K, E extends EventSourced> implements SnapshotReader<K, E> {

    private static final Logger logger = LoggerFactory.getLogger(Stack1SnapshotReader.class);

    private final Supplier<E> supplier;
    private final WriteModelDao<K, E> dao;
    private final Cache<K, SnapshotData<E>> cache;
    private final BiFunction<E, List<Event>, E> applyEventsFunction;
    private final Function<E, E> injector;

    @Inject
    public Stack1SnapshotReader(Supplier<E> supplier,
                                WriteModelDao<K, E> dao,
                                Cache<K, SnapshotData<E>> cache,
                                BiFunction<E, List<Event>, E> applyEventsFunction,
                                Function<E, E> injector) {
        this.supplier = supplier;
        this.dao = dao;
        this.cache = cache;
        this.applyEventsFunction = applyEventsFunction;
        this.injector = injector;
    }

    /*
     * (non-Javadoc)
     * @see org.myeslib.infra.SnapshotReader#getSnapshot(java.lang.Object)
     */
    public Snapshot<E> getSnapshot(final K id) {
        checkNotNull(id);

        logger.debug("id {} cache.get(id)", id);
        final SnapshotData<E> lastSnapshot;
        final AtomicBoolean wasDaoCalled = new AtomicBoolean(false);
        try {
            logger.info("id {} cache.get(id)", id);
            lastSnapshot = cache.get(id, () -> {
                logger.info("id {} cache.get(id) does not contain anything for this id. Will have to search on dao", id);
                wasDaoCalled.set(true);
                return new SnapshotData<>(dao.getFull(id));
            });
        } catch (ExecutionException e) {
            throw new RuntimeException(e.getCause());
        }

        logger.info("id {} wasDaoCalled ? {}", id, wasDaoCalled.get());
        if (wasDaoCalled.get()) {
            final E eventSourced = applyEventsFunction.apply(supplier.get(), flatMap(lastSnapshot.unitOfWorkList));
            return new Snapshot<>(injector.apply(eventSourced), lastVersion(lastSnapshot.unitOfWorkList));
        }
        final Long lastVersion = lastVersion(lastSnapshot.unitOfWorkList);

        logger.info("id {} cached lastSnapshotData has version {}. will check if there any version beyond it", id, lastVersion);
        final List<UnitOfWork> partialTransactionHistory = dao.getPartial(id, lastVersion);
        if (partialTransactionHistory.isEmpty()) {
            final E eventSourced = applyEventsFunction.apply(supplier.get(), flatMap(lastSnapshot.unitOfWorkList));
            return new Snapshot<>(injector.apply(eventSourced), lastVersion);
        }

        logger.info("id {} found {} pending transactions. Last version is now {}", id, partialTransactionHistory.size(), lastVersion);
        final List<UnitOfWork> newHistory = Stream.of(lastSnapshot.unitOfWorkList, partialTransactionHistory).flatMap(x -> x.stream()).collect(Collectors.toList());
        final Long newLastVersion = lastVersion(newHistory);
        cache.put(id, new SnapshotData<>(newHistory));
        final E eventSourced = applyEventsFunction.apply(supplier.get(), flatMap(newHistory));
        return new Snapshot<>(injector.apply(eventSourced), newLastVersion);
    }

}
