package org.rnott.example.feature;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Cache control configuration. This annotation can be placed at the class or method level.
 *
 * @see jakarta.ws.rs.core.CacheControl
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CacheControl {

    boolean isNoCache() default false;

    int maxAge() default -1;

    int sMaxAge() default -1;

    boolean isPrivate() default false;

    boolean isMustRevalidate() default false;

    boolean isProxyRevalidate() default false;

    boolean isNoStore() default false;

    boolean isNoTransform() default false;
}
