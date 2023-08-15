/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2023 Red Hat, Inc., and individual contributors
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

package org.jboss.logging.processor.model;

import org.jboss.logging.Logger;
import org.jboss.logging.annotations.Transform;

/**
 * Represents a method which is annotated with {@link org.jboss.logging.annotations.LogMessage}.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public interface LoggerMessageMethod extends MessageMethod {

    /**
     * Returns the log method to use.
     *
     * @return the log method to use
     */
    String loggerMethod();

    /**
     * Returns the log level parameter associated with the method.
     *
     * @return the enum name of the {@linkplain org.jboss.logging.Logger.Level log level}
     */
    String logLevel();

    /**
     * Indicates whether or not the {@linkplain org.jboss.logging.Logger#isEnabled(Logger.Level) level} should be
     * checked before any logging, or an expensive operation, is done.
     *
     * @return {@code true} if the statement should be wrapped in an {@code if (logger.isEnabled(level))} block,
     * otherwise {@code false}
     */
    default boolean wrapInEnabledCheck() {
        return !parametersAnnotatedWith(Transform.class).isEmpty();
    }
}
