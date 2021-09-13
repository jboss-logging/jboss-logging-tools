/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2021 Red Hat, Inc., and individual contributors
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
 * Transforms the parameter into a new exception appending the {@linkplain Throwable#getLocalizedMessage() message} from
 * the parameter to the {@linkplain Message message} from the method. This annotation is only allowed on parameters that
 * are a super type of the return type.
 * <p>
 * Note that the {@linkplain Message message} must include a {@code %s} for the message from the parameter.
 * </p>
 *
 * <p>
 * <pre>{@code
 * @Message("Binding to %s failed: %s")
 * IOException bindFailed(SocketAddress address, @TransformException({BindException.class, SocketException.class}) IOException toCopy);
 * }</pre>
 *
 * In the above example an exception is created based on the {@code toCopy} parameter. If the {@code toCopy} parameter
 * is a {@link java.net.BindException} then a {@link java.net.BindException} is created and the stack trace from the
 * {@code toCopy} parameter is copied to the newly created exception. This will happen for each type listed as a value,
 * finally falling back to an {@link java.io.IOException} if the parameter is not an instance of the suggested types.
 * </p>
 * <p>
 * The message for the newly created exception will be; &quot;Binding to {@code address.toString()} failed:
 * {@code toCopy.getLocalizedMessage()}&quot;.
 * </p>
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@Retention(CLASS)
@Target(PARAMETER)
@Documented
public @interface TransformException {

    /**
     * Indicates if the stack trace from the parameter should be copied to the exception returned.
     * <p>
     * If {@code true}, the default, the parameters stack trace will be set as the stack trace on the newly created
     * exception that is returned.
     * </p>
     *
     * @return {@code true} if the stack trace should be copied to the newly created exception
     */
    boolean copyStackTrace() default true;

    /**
     * An array of suggested types to create. Each type must be a super type of the parameter.
     *
     * @return the suggested types to create
     */
    Class<? extends Throwable>[] value() default {};
}
