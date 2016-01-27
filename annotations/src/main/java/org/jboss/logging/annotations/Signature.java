/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2016 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.logging.annotations;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Specifies the exact signature to use when creating a {@link Throwable} return type.
 *
 * <p>
 * Given the following exception and message bundle interface method the {@code InvalidIntValueException(final RuntimeException cause, final String msg, final int value)}
 * constructor would be used.
 *
 * <code>
 * <pre>
 * public class InvalidIntValueException extends RuntimeException {
 *     private final RuntimeException causeAsRuntime;
 *     private final int value;
 *     public InvalidIntValueException(final Throwable cause, final String msg, final int value) {
 *         super(msg, cause);
 *         causeAsRuntime = new RuntimeException(cause);
 *         this.value = value;
 *     }
 *
 *     public InvalidIntValueException(final RuntimeException cause, final String msg, final int value) {
 *         super(msg, cause);
 *         causeAsRuntime = cause;
 *         this.value = value;
 *     }
 *     public InvalidIntValueException(final RuntimeException cause, final String msg, final Integer value) {
 *         super(msg, cause);
 *         causeAsRuntime = cause;
 *         this.value = value;
 *     }
 * }
 * </pre>
 * </code>
 *
 * <code>
 * <pre>
 * &#64;Message(&quot;Invalid value %d&quot;)
 * &#64;Signature(causeIndex = 0, messageIndex = 1, value = {RuntimeException.class, String.class, int.class}
 * InvalidIntValueException invalidValue(@Cause RuntimeException cause, @Param int value);
 * </pre>
 * </code>
 * </p>
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@Retention(CLASS)
@Target(METHOD)
@Documented
public @interface Signature {

    /**
     * An array of types matching the exact signature to use for the exception being created.
     *
     * @return an array of types used to find the signature
     */
    Class<?>[] value();

    /**
     * The index for the {@linkplain Cause cause} of the exception being created. A value of less than zero assumes
     * there is no cause parameter in the constructor. A {@link Cause} annotation can still be used and the
     * {@link Throwable#initCause(Throwable)} will be used to initialize the cause of the exception.
     *
     * @return the index for the cause parameter
     */
    int causeIndex() default -1;

    /**
     * The index for the message. This is the formatted messaged from the {@link Message#value()}. This is a required
     * value defaulting to 0 which would be the first parameter.
     *
     * @return the index for the message parameter
     */
    int messageIndex() default 0;
}
