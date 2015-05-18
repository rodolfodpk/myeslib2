package org.myeslib.data;

import org.myeslib.core.EventSourced;

import java.io.Serializable;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class SnapshotData<E extends EventSourced> implements Serializable {

    public final List<UnitOfWork> unitOfWorkList;

    public SnapshotData(List<UnitOfWork> unitOfWorkList) {
        requireNonNull(unitOfWorkList, "unitOfWorkList cannot be null");
        this.unitOfWorkList = unitOfWorkList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SnapshotData<?> that = (SnapshotData<?>) o;
        return unitOfWorkList.equals(that.unitOfWorkList);
    }

    @Override
    public int hashCode() {
        return unitOfWorkList.hashCode();
    }
    @Override
    public String toString() {
        return "SnapshotData{" +
                "unitOfWorkList=" + unitOfWorkList +
                '}';
    }
}
