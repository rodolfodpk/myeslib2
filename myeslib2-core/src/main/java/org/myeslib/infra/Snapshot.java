package org.myeslib.infra;

import net.jcip.annotations.Immutable;
import org.myeslib.core.EventSourced;

import java.io.Serializable;

@Immutable
public interface Snapshot<A extends EventSourced> extends Serializable {

    A getAggregateInstance();

    Long getVersion();
}
