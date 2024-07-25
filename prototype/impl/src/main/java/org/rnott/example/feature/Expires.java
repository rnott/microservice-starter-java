package org.rnott.example.feature;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Contains the date/time after which the response is considered
 * expired. This has no effect if <code>@CacheResponse</code> is
 * present.
 *
 * @see CacheControl
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Expires {
    /**
     * Determine how long the response may be cached.
     *
     * @return the length of time expressed as a duration.
     */
    String value();
}
