package org.myeslib.storage.jdbi;

import com.google.common.cache.Cache;
import org.myeslib.core.AggregateRoot;
import org.myeslib.core.data.Snapshot;
import org.myeslib.core.data.UnitOfWorkHistory;
import org.myeslib.core.storage.SnapshotReader;
import org.myeslib.storage.helpers.eventsource.SnapshotHelper;
import org.myeslib.storage.jdbi.dao.UnitOfWorkDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import static com.google.common.base.Preconditions.checkNotNull;

public class JdbiSnapshotReader<K, A extends AggregateRoot> implements SnapshotReader<K, A> {

    private static final Logger logger = LoggerFactory.getLogger(JdbiSnapshotReader.class);

    private final Supplier<A> supplier;
    private final UnitOfWorkDao<K> dao;
    private final Cache<K, Snapshot<A>> cache;
    private final SnapshotHelper<A> snapshotHelper;

    public JdbiSnapshotReader(Supplier<A> supplier, UnitOfWorkDao<K> dao,
                              Cache<K, Snapshot<A>> cache, SnapshotHelper<A> snapshotHelper) {
        checkNotNull(supplier);
        this.supplier = supplier;
        checkNotNull(dao);
        this.dao = dao;
        checkNotNull(cache);
        this.cache = cache;
        checkNotNull(snapshotHelper);
        this.snapshotHelper = snapshotHelper;
    }

    /*
     * (non-Javadoc)
     * @see org.myeslib.core.storage.SnapshotReader#getPartial(java.lang.Object)
     */
    public Snapshot<A> getSnapshot(final K id) {
        checkNotNull(id);
        final Snapshot<A> lastSnapshot;
        final AtomicBoolean wasDaoCalled = new AtomicBoolean(false);
        try {
            logger.info("id {} cache.get(id)", id);
            lastSnapshot = cache.get(id, () -> {
                wasDaoCalled.set(true);
                return snapshotHelper.applyEventsOn(supplier.get(), dao.get(id));
            });
        } catch (ExecutionException e) {
            throw new RuntimeException(e.getCause());
        }
        logger.info("id {} wasDaoCalled ? {}", id, wasDaoCalled.get());
        if (wasDaoCalled.get()) {
            return lastSnapshot;
        }
        logger.info("id {} lastSnapshot has version {}. will check if there any version beyond it", id, lastSnapshot.getVersion());
        final UnitOfWorkHistory partialTransactionHistory = dao.getPartial(id, lastSnapshot.getVersion());
        logger.info("id {} found {} pending transactions. Last version is {}", id, partialTransactionHistory.getAllEvents().size(), lastSnapshot.getVersion());
        return snapshotHelper.applyEventsOn(lastSnapshot.getAggregateInstance(), partialTransactionHistory);
    }

}
