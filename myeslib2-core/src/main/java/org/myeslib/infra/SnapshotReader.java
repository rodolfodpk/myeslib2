package org.myeslib.infra;

import org.myeslib.core.EventSourced;
import org.myeslib.data.Event;
import org.myeslib.data.UnitOfWork;

import java.util.List;
import java.util.stream.Collectors;

public interface SnapshotReader<K, A extends EventSourced> {

    public Snapshot<A> getSnapshot(final K id);

    default List<Event> flatMap(final List<UnitOfWork> unitOfWorks) {
        return unitOfWorks.stream().flatMap((unitOfWork) -> unitOfWork.getEvents().stream()).collect(Collectors.toList());
    }

    default Long lastVersion(List<UnitOfWork> unitOfWorks) {
        return unitOfWorks.isEmpty() ? 0L : unitOfWorks.get(unitOfWorks.size()-1).getVersion();
    }

}