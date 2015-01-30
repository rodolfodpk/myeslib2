package org.myeslib.core;

import java.io.Serializable;
import java.util.UUID;

public interface Command<K> extends Serializable {

    UUID getCommandId();

    K getTargetId();
}
