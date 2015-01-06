package org.myeslib.core.storage;

import org.myeslib.core.AggregateRoot;
import org.myeslib.core.data.Snapshot;

public interface SnapshotReader<K, A extends AggregateRoot> {

    public Snapshot<A> getSnapshot(final K id);

}