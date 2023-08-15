
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

package org.jboss.logging.tools.examples;

import java.io.Closeable;
import java.io.IOException;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jboss.logging.Messages;
import org.jboss.logging.annotations.BaseUrl;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageBundle;
import org.jboss.logging.annotations.Param;
import org.jboss.logging.annotations.Pos;
import org.jboss.logging.annotations.Producer;
import org.jboss.logging.annotations.ResolutionDoc;
import org.jboss.logging.annotations.Suppressed;

/**
 * Common error messages for the application.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@SuppressWarnings("unused")
@MessageBundle(projectCode = "CW")
@BaseUrl("errors/")
public interface ErrorMessages {
    /**
     * The static message instance.
     */
    ErrorMessages MESSAGES = Messages.getBundle(ErrorMessages.class);

    /**
     * Returns the internationalized version message.
     *
     * @param major the major version
     * @param minor the minor version
     * @param macro the macro version
     * @param rel   the release suffix, e.g. {@code Beta1}, {@code Final}
     *
     * @return the formatted version
     */
    @Message("Version %d.%d.%d.%s")
    String version(int major, int minor, int macro, String rel);

    /**
     * Creates an exception indicating the value is invalid.
     *
     * @param value the invalid value
     *
     * @return an {@link IllegalStateException} for the error
     */
    @Message(id = 1, value = "Value '%s' is invalid")
    IllegalArgumentException invalidValue(Object value);

    /**
     * Creates an exception indicating a failure to close the {@link Closeable}
     *
     * @param cause the cause of the error
     * @param c     the closeable that failed
     *
     * @return a {@link CloseException} for the error
     */
    @Message(id = 2, value = "Failure closing %s")
    CloseException closeFailure(@Cause Throwable cause, @Param @Pos(1) Closeable c);

    /**
     * Creates an exception indicating a failure to close the {@link Closeable}
     *
     * @param cause  the cause of the error
     * @param c      the closeable that failed
     * @param errors errors that should be added as {@linkplain Throwable#getSuppressed() suppressed exceptions}.
     *
     * @return a {@link CloseException} for the error
     */
    CloseException closeFailure(@Cause Throwable cause, @Param @Pos(1) Closeable c, @Suppressed Throwable... errors);

    /**
     * Creates a message indicating the parameter is {@code null}.
     * <p>
     * This can be used to lazily format the message for {@code null} checks like
     * {@link java.util.Objects#requireNonNull(Object, Supplier)}.
     * </p>
     *
     * @param name the name of the parameter
     *
     * @return a supplier for the message
     */
    @Message(id = 3, value = "Parameter %s cannot be null")
    @ResolutionDoc(suffix = ".html")
    Supplier<String> nullParam(String name);

    /**
     * Uses the producer function to create the returned exception indicating the operation has failed.
     * <p>
     * The formatted value of the message will be the parameter for the functions
     * {@linkplain Function#apply(Object) apply} method.
     * </p>
     *
     * @param fn   the function to produce the returned exception
     * @param name the name of the operation that failed
     * @param <T>  the type of the exception to return, must be assignable to a {@link RuntimeException}
     *
     * @return the produced exception for the error
     */
    @Message(id = 4, value = "Operation %s failed.")
    <T extends RuntimeException> T operationFailed(@Producer Function<String, T> fn, String name);

    /**
     * Uses the producer function to create the returned exception indicating the operation has failed.
     * <p>
     * The formatted value of the message will be the first parameter for the functions
     * {@linkplain BiFunction#apply(Object, Object)} apply} method. The second parameter will be the cause.
     * </p>
     *
     * @param fn    the function to produce the returned exception
     * @param cause the cause of the exception to pass to the function
     * @param name  the name of the operation that failed
     * @param <T>   the type of the exception to return, must be assignable to a {@link RuntimeException}
     *
     * @return the produced exception for the error
     */
    <T extends RuntimeException> T operationFailed(@Producer BiFunction<String, IOException, T> fn, @Cause IOException cause,
            String name);
}
