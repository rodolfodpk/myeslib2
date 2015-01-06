package org.myeslib.experimental.pm;

/**
 * http://kerflyn.wordpress.com/2012/05/09/towards-pattern-matching-in-java/
 *
 * @param <T>
 */
public class PatternMatching<T> {

    private Pattern<T>[] patterns;

    public PatternMatching(Pattern... patterns) {
        this.patterns = patterns;
    }

    public T matchFor(Object value) {
        for (Pattern<T> pattern : patterns)
            if (pattern.matches(value))
                return pattern.apply(value);

        throw new IllegalArgumentException("cannot match " + value);
    }
}