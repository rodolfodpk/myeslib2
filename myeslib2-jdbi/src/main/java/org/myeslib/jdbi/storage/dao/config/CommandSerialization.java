package org.myeslib.jdbi.storage.dao.config;

import org.myeslib.core.Command;
import org.myeslib.data.UnitOfWork;

import java.util.function.Function;

public class CommandSerialization<K> {

    public final Function<Command<K>, String> toStringFunction;
    public final Function<String, Command<K>> fromStringFunction;

    public CommandSerialization(Function<Command<K>, String> toStringFunction,
                                Function<String, Command<K>> fromStringFunction) {
        this.toStringFunction = toStringFunction;
        this.fromStringFunction = fromStringFunction;
    }

}
