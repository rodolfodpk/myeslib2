package org.myeslib.core;

import net.jcip.annotations.Immutable;

import java.io.Serializable;

@Immutable
public interface Command extends Serializable {

    CommandId commandId();

}
