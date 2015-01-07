package org.myeslib.storage.jdbi.dao.config;

import org.myeslib.core.data.UnitOfWork;

import java.util.function.Function;

public class UowSerializationFunctions {

    public final Function<UnitOfWork, String> toStringFunction;
    public final Function<String, UnitOfWork> fromStringFunction;

    public UowSerializationFunctions(Function<UnitOfWork, String> toStringFunction,
                                     Function<String, UnitOfWork> fromStringFunction) {
        this.toStringFunction = toStringFunction;
        this.fromStringFunction = fromStringFunction;
    }

}
