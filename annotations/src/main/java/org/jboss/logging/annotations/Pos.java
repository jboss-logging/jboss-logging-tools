package org.jboss.logging.annotations;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 * @since 1.1.0
 */
@Retention(CLASS)
@Target(PARAMETER)
@Documented
public @interface Pos {

    /**
     * The positions the value should be used at.
     *
     * @return an array of the positions for the parameter
     */
    int[] value();

    /**
     * The transform types used on the parameter.
     *
     * @return an array of the transformer types
     *
     * @see Transform
     */
    Transform[] transform() default {};
}
