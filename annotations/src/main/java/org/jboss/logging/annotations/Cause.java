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

import org.jboss.logging.Logger;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.CLASS;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * Mark a parameter as being the "exception cause" parameter rather than a positional format parameter.
 *
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
@Retention(CLASS)
@Target(PARAMETER)
@Documented
public @interface Cause {
    /**
     * The maximum allowed log level at which the exception cause is actually logged.
     *
     * This level is compared against the log level allowed for the logger, i.e. it is independent of the level the
     * actual message is being logged at.
     *
     * The default value, {@link org.jboss.logging.Logger.Level#FATAL} means that the cause is attached to the message
     * every time.
     *
     * If this attribute is set for example to {@link Logger.Level#DEBUG} then the cause will be attached to the message
     * only if the allowed log level for the logger is {@link Logger.Level#DEBUG} or {@link Logger.Level#TRACE}.
     */
    Logger.Level loggedAt() default Logger.Level.FATAL;
}
