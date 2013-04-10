package org.jboss.logging.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@Target(TYPE)
@Retention(CLASS)
@Documented
public @interface ValidIdRanges {

    /**
     * An array of valid id ranges.
     *
     * @return an array of valid id ranges
     */
    ValidIdRange[] value();
}
