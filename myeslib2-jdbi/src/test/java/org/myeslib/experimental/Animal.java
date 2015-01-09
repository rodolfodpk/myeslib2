package org.myeslib.experimental;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class Animal {

    transient Long l;

    abstract String name();
    abstract int numberOfLegs();

    static Animal create(String name, int numberOfLegs) {
         return new AutoValue_Animal(name, numberOfLegs);
    }

    public static void main(String[] args) {

        AutoValue_Animal a = new AutoValue_Animal("dog", 4);

    }

}