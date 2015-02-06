package org.myeslib.data;

import org.myeslib.core.AggregateRoot;

import java.io.Serializable;

public interface Snapshot<A extends AggregateRoot> extends Serializable {

    A getAggregateInstance();

    Long getVersion();
}
