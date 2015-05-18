package org.myeslib.data;

import net.jcip.annotations.Immutable;

import java.io.Serializable;

@Immutable
public interface Command extends Serializable {

    CommandId getCommandId();

}
