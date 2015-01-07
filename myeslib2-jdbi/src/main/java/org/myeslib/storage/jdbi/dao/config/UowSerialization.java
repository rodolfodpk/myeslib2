package org.myeslib.storage.jdbi.dao.config;

import org.myeslib.core.data.UnitOfWork;

import java.util.function.Function;

public class UowSerialization {

    public final Function<UnitOfWork, String> toStringFunction;
    public final Function<String, UnitOfWork> fromStringFunction;

    public UowSerialization(Function<UnitOfWork, String> toStringFunction,
                            Function<String, UnitOfWork> fromStringFunction) {
        this.toStringFunction = toStringFunction;
        this.fromStringFunction = fromStringFunction;
    }

}
