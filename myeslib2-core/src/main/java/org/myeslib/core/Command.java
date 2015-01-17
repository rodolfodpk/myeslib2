package org.myeslib.core;

import java.io.Serializable;
import java.util.UUID;

public interface Command<T> extends Serializable {

    UUID getCommandId();

    T getTargetId();
}
