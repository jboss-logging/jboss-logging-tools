/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2015 Red Hat, Inc., and individual contributors
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

import org.jboss.logging.Logger;

/**
 * A typed logger method. Indicates that this method will log the associated {@link Message} to the logger system, as
 * opposed to being a simple message lookup.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@Retention(CLASS)
@Target(METHOD)
@Documented
public @interface LogMessage {

    /**
     * The log level at which this message should be logged.  Defaults to {@code INFO}.
     *
     * @return the log level
     */
    Logger.Level level() default Logger.Level.INFO;

    /**
     * The logging class name to use for this message, if any.
     *
     * @return the logging class name
     */
    Class<?> loggingClass() default Void.class;

    /**
     * Indicates before the message is logged the {@linkplain Thread#currentThread() current threads}
     * {@linkplain Thread#setContextClassLoader(ClassLoader) context class loader} is set to the the class loader from
     * this type.
     * <p>
     * Note that special permissions may be required if running under a {@linkplain SecurityManager security manager}.
     * </p>
     * <p>
     * It is suggested this not be used on methods which are invoked frequently as there is overhead to this.
     * </p>
     *
     * @return {@code true} if the current threads context loader should be used for the log call
     *
     * @see Thread#getContextClassLoader()
     * @see Thread#setContextClassLoader(ClassLoader)
     * @since 2.3.0
     */
    boolean useThreadContext() default false;
}
