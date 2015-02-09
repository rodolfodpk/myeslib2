package org.myeslib.stack1.infra.dao.config;

import org.myeslib.data.UnitOfWork;

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
