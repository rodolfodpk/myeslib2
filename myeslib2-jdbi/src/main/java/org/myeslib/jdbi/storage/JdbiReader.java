package org.myeslib.jdbi.storage;

import com.google.common.cache.Cache;
import org.myeslib.core.AggregateRoot;
import org.myeslib.data.Snapshot;
import org.myeslib.data.UnitOfWorkHistory;
import org.myeslib.jdbi.storage.dao.UnitOfWorkDao;
import org.myeslib.storage.SnapshotReader;
import org.myeslib.function.SnapshotComputing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;


import static com.google.common.base.Preconditions.checkNotNull;

public class JdbiReader<K, A extends AggregateRoot> implements SnapshotReader<K, A> {

    private static final Logger logger = LoggerFactory.getLogger(JdbiReader.class);

    private final Supplier<A> supplier;
    private final UnitOfWorkDao<K> dao;
    private final Cache<K, Snapshot<A>> cache;
    private final SnapshotComputing<A> snapshotComputing;

    public JdbiReader(Supplier<A> supplier, UnitOfWorkDao<K> dao,
                      Cache<K, Snapshot<A>> cache, SnapshotComputing<A> SnapshotComputing) {
        checkNotNull(supplier);
        this.supplier = supplier;
        checkNotNull(dao);
        this.dao = dao;
        checkNotNull(cache);
        this.cache = cache;
        checkNotNull(SnapshotComputing);
        this.snapshotComputing = SnapshotComputing;
    }

    /*
     * (non-Javadoc)
     * @see org.myeslib.storage.SnapshotReader#getPartial(java.lang.Object)
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
                return snapshotComputing.applyEventsOn(supplier.get(), dao.getFull(id));
            });
        } catch (ExecutionException e) {
            throw new RuntimeException(e.getCause());
        }
        logger.debug("id {} wasDaoCalled ? {}", id, wasDaoCalled.get());
        if (wasDaoCalled.get()) {
            return lastSnapshot;
        }
        logger.debug("id {} lastSnapshot has version {}. will check if there any version beyond it", id, lastSnapshot.getVersion());
        final UnitOfWorkHistory partialTransactionHistory = dao.getPartial(id, lastSnapshot.getVersion());
        if (partialTransactionHistory.isEmpty()) {
            return lastSnapshot;
        }
        logger.debug("id {} found {} pending transactions. Last version is {}", id, partialTransactionHistory.getAllEvents().size(), partialTransactionHistory.getLastVersion());
        final Snapshot<A> latestSnapshot = snapshotComputing.applyEventsOn(lastSnapshot.getAggregateInstance(), partialTransactionHistory);
        cache.put(id, latestSnapshot); // TODO assert this on tests
        return latestSnapshot;
    }

}
