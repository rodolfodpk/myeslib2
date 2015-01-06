package org.myeslib.jdbi.storage;

import com.google.common.cache.Cache;
import org.myeslib.core.AggregateRoot;
import org.myeslib.core.Event;
import org.myeslib.core.data.Snapshot;
import org.myeslib.core.data.UnitOfWorkHistory;
import org.myeslib.core.storage.SnapshotReader;
import org.myeslib.jdbi.storage.config.AggregateRootFunctions;
import org.myeslib.jdbi.storage.dao.UnitOfWorkDao;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.myeslib.jdbi.helpers.eventsource.EventSourcingMagicHelper.applyEventsOn;

public class JdbiSnapshotReader<K, A extends AggregateRoot> implements SnapshotReader<K, A> {
    
	public JdbiSnapshotReader(AggregateRootFunctions<A> functions,
                              UnitOfWorkDao<K> dao, Cache<K, Snapshot<A>> cache) {
        checkNotNull(functions);
        this.config = functions;
        checkNotNull(dao);
        this.dao = dao;
        checkNotNull(cache);
        this.cache = cache;
    }

    private final AggregateRootFunctions<A> config;
    private final UnitOfWorkDao<K> dao;
    private final Cache<K, Snapshot<A>> cache;

	/*
	 * (non-Javadoc)
	 * @see org.myeslib.core.storage.SnapshotReader#getPartial(java.lang.Object)
	 */
	public Snapshot<A> getSnapshot(final K id) {
		checkNotNull(id);
		final Snapshot<A> lastSnapshot ;
        final AtomicBoolean wasFullLoadPerformed = new AtomicBoolean(false);
        try {
            lastSnapshot = cache.get(id, () -> {
                wasFullLoadPerformed.set(true);
                return applyAllEventsOnFreshInstance(dao.get(id));
            });
        } catch (ExecutionException e) {
            throw new RuntimeException(e.getCause());
        }
        if (wasFullLoadPerformed.get()){
            return lastSnapshot;
        }
        final UnitOfWorkHistory partialTransactionHistory = dao.getPartial(id, lastSnapshot.getVersion());
	    return applyMissingEventsOnThisInstance(partialTransactionHistory, lastSnapshot);
    }

	private Snapshot<A> applyAllEventsOnFreshInstance(final UnitOfWorkHistory transactionHistory) {
        final A aggregateRootFreshInstance = config.supplier.get();
		final Long lastVersion = transactionHistory.getLastVersion();
		final List<? extends Event> eventsToApply = transactionHistory.getEventsUntil(lastVersion);
		applyEventsOn(eventsToApply, aggregateRootFreshInstance);
		return new Snapshot<>(aggregateRootFreshInstance, lastVersion);
	}

    private Snapshot<A> applyMissingEventsOnThisInstance(final UnitOfWorkHistory transactionHistory,
                                                         final Snapshot<A> lastSnapshot) {
        final Long lastVersion = transactionHistory.getLastVersion();
        applyEventsOn(transactionHistory.getAllEvents(), lastSnapshot.getAggregateInstance());
        return new Snapshot<>(lastSnapshot.getAggregateInstance(), lastVersion);
    }
}
