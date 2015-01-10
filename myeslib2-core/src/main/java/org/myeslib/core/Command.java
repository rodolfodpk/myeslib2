package org.myeslib.core;

import org.myeslib.data.Id;

import java.io.Serializable;
import java.util.UUID;

public interface Command extends Serializable {

    UUID getCommandId();

    // Id<?> getTargetId(); // experimental

    Long getTargetVersion();

}
