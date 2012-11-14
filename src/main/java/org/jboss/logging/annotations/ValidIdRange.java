package org.jboss.logging.annotations;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Sets a range of valid id's allowed on the {@link org.jboss.logging.annotations.Message#id() message id}. Both {@link
 * Message#INHERIT} and {@link Message#NONE} are ignored when validating.
 * <p/>
 * <code>
 * <pre>
 *          &#64;MessageLogger(projectCode = "EXAMPLE")
 *          &#64;ValidIdRange(min = 100, max = 200)
 *          public interface ExampleLogger {
 *
 *              &#64;LogMessage
 *              &#64;Message(id = 100, value = "Example message")
 *              void example();
 *          }
 * </pre>
 * </code>
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@Target(TYPE)
@Retention(CLASS)
@Documented
public @interface ValidIdRange {

    /**
     * The minimum id allowed in the {@link org.jboss.logging.annotations.Message#id() message id}. Both {@link
     * Message#INHERIT} and {@link Message#NONE} are ignored when validating.
     *
     * @return the minimum id allowed
     */
    int min() default 1;

    /**
     * The maximum id allowed in the {@link org.jboss.logging.annotations.Message#id() message id}. Both {@link
     * Message#INHERIT} and {@link Message#NONE} are ignored when validating.
     *
     * @return the maximum id allowed
     */
    int max() default 999999;
}
