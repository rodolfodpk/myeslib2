package org.myeslib.data;

import java.io.Serializable;

public interface Command extends Serializable {

    CommandId getCommandId();

}
