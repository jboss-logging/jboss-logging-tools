/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2017 Red Hat, Inc., and individual contributors
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

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Identifies a parameter has the ability to produce a {@link Throwable} or a super type of a {@code Throwable}. The
 * parameter type must be a {@link java.util.function.Function} or a {@link java.util.function.BiFunction}.
 * <p>
 * For a {@link java.util.function.Function} the input parameter must be a {@link String} which will be the message
 * associated with the method. The result type must {@link Throwable} or a super type of a {@code Throwable}.
 * </p>
 *
 * <p>
 * For a {@link java.util.function.BiFunction} one of the input parameters must be a {@link String} which will be the
 * message associated with the method. The other input parameter must be a {@link Throwable} or a super type of a
 * {@code Throwable} and must be assignable from the parameter annotated with {@link Cause}. The result type must
 * {@link Throwable} or a super type of a {@code Throwable}.
 * </p>
 *
 * <p>
 * <strong>Example</strong>
 * <code>
 * <pre>
 * &#64;Message(&quot;The operation failed due to %s&quot;)
 * <T extends RuntimeException> T operationFailed(@Producer Function<String, T> function, String op);
 *
 * &#64;Message(&quot;The operation failed due to %s&quot;)
 * <T extends RuntimeException> T operationFailed(@Producer BiFunction<String, Throwable, T> function, @Cause Throwable cause, String op);
 *
 * </pre>
 * </code>
 * </p>
 *
 * <p>
 * <strong>Example Usage</strong>
 * <code>
 * <pre>
 * throw Bundle.MESSAGES.operationFailed(IllegalArgumentException::new, &quot;start&quot;);
 *
 * throw Bundle.MESSAGES.operationFailed(IllegalStateException::new, cause, &quot;start&quot;);
 *
 * </pre>
 * </code>
 * </p>
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@Target(PARAMETER)
@Retention(CLASS)
@Documented
public @interface Producer {
}
