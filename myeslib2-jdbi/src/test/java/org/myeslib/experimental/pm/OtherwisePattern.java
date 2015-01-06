package org.myeslib.experimental.pm;

import java.util.function.Function;

public class OtherwisePattern<T> implements Pattern {

    private final Function<Object, T> function;

    public OtherwisePattern(Function<Object, T> function) {
        this.function = function;
    }

    public static <T> Pattern otherwise(Function<Object, T> function) {
        return new OtherwisePattern(function);
    }

    @Override
    public boolean matches(Object value) {
        return true;
    }

    @Override
    public T apply(Object value) {
        return function.apply(value);
    }

}
