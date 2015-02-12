package org.myeslib.data;

import net.jcip.annotations.Immutable;
import org.myeslib.core.AggregateRoot;

import java.io.Serializable;

@Immutable
public interface Snapshot<A extends AggregateRoot> extends Serializable {

    A getAggregateInstance();

    Long getVersion();
}
