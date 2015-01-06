package org.myeslib.jdbi.storage.config;

import org.myeslib.core.AggregateRoot;
import org.myeslib.core.data.UnitOfWork;

import java.util.function.Function;
import java.util.function.Supplier;

public class AggregateRootFunctions<A extends AggregateRoot> {

    public final Supplier<A> supplier;
    public final Function<UnitOfWork, String> toStringFunction;
    public final Function<String, UnitOfWork> fromStringFunction;

    public AggregateRootFunctions(Supplier<A> supplier, Function<UnitOfWork, String> toStringFunction,
                                  Function<String, UnitOfWork> fromStringFunction) {
        this.supplier = supplier;
        this.toStringFunction = toStringFunction;
        this.fromStringFunction = fromStringFunction;
    }

}
