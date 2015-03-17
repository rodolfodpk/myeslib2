package org.myeslib.infra;

import org.myeslib.core.EventSourced;

public interface SnapshotReader<K, A extends EventSourced> {

    public Snapshot<A> getSnapshot(final K id);

}