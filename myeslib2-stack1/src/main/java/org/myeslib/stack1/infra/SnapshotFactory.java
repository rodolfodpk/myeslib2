package org.myeslib.stack1.infra;

import org.myeslib.core.EventSourced;
import org.myeslib.infra.Snapshot;

public interface SnapshotFactory<A extends EventSourced> {

    Snapshot<A> create(A eventSourced, Long version);

}
