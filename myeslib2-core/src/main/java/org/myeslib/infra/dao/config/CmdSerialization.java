package org.myeslib.infra.dao.config;

import org.myeslib.core.EventSourced;
import org.myeslib.data.Command;

import java.util.function.Function;

public class CmdSerialization<E extends EventSourced> {

    public final Function<Command, String> toStringFunction;
    public final Function<String, Command> fromStringFunction;

    public CmdSerialization(Function<Command, String> toStringFunction,
                            Function<String, Command> fromStringFunction) {
        this.toStringFunction = toStringFunction;
        this.fromStringFunction = fromStringFunction;
    }

}
