package org.myeslib.infra;

import org.myeslib.core.AggregateRoot;

public interface SnapshotReader<K, A extends AggregateRoot> {

    public Snapshot<A> getSnapshot(final K id);

}