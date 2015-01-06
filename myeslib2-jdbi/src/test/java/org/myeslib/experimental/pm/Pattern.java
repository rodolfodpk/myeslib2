package org.myeslib.experimental.pm;

public interface Pattern<T> {
    boolean matches(Object value);
    T apply(Object value);
}