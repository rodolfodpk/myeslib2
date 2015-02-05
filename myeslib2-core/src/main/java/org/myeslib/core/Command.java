package org.myeslib.core;

import net.jcip.annotations.Immutable;

import java.io.Serializable;
import java.util.UUID;

@Immutable
public interface Command extends Serializable {

    UUID commandId();

}
