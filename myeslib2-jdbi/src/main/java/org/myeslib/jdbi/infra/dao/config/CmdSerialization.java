package org.myeslib.jdbi.infra.dao.config;

import org.myeslib.core.Command;

import java.util.function.Function;

public class CmdSerialization {

    public final Function<Command, String> toStringFunction;
    public final Function<String, Command> fromStringFunction;

    public CmdSerialization(Function<Command, String> toStringFunction,
                            Function<String, Command> fromStringFunction) {
        this.toStringFunction = toStringFunction;
        this.fromStringFunction = fromStringFunction;
    }

}
