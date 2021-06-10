/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2021, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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
