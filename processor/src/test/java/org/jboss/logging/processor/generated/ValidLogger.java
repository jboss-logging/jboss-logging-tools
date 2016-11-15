/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016, Red Hat, Inc., and individual contributors
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

package org.jboss.logging.processor.generated;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.Message.Format;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.logging.annotations.ValidIdRange;
import org.jboss.logging.annotations.ValidIdRanges;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@MessageLogger(projectCode = AbstractLoggerTest.PROJECT_CODE)
@ValidIdRanges({
        @ValidIdRange(min = 200, max = 202),
        @ValidIdRange(min = 203, max = 204)
})
public interface ValidLogger {

    final ValidLogger LOGGER = Logger.getMessageLogger(ValidLogger.class, AbstractLoggerTest.CATEGORY);

    @LogMessage(level = Level.INFO, loggingClass = ValidLogger.class)
    @Message(id = 200, value = "This is a generated message.")
    void testInfoMessage();

    @LogMessage(level = Level.INFO)
    @Message(id = 201, value = "Test message format message. Test value: {0}", format = Format.MESSAGE_FORMAT)
    void testMessageFormat(Object value);

    /**
     * Logs a default informational greeting.
     *
     * @param name the name.
     */
    @LogMessage
    @Message(id = 202, value = "Greetings %s")
    void greeting(String name);

    /**
     * Logs an error message indicating a processing error.
     */
    @LogMessage(level = Level.ERROR)
    @Message(id = 203, value = "Processing error")
    void processingError();

    /**
     * Logs an error message indicating a processing error.
     *
     * @param cause the cause of the error.
     */
    @LogMessage(level = Level.ERROR)
    void processingError(@Cause Throwable cause);

    /**
     * Logs an error message indicating there was a processing error.
     *
     * @param cause      the cause of the error.
     * @param moduleName the module that caused the error.
     */
    @LogMessage(level = Level.ERROR)
    @Message(id = Message.INHERIT, value = "Processing error in module '%s'")
    void processingError(@Cause Throwable cause, String moduleName);

    /**
     * Logs an error message indicating a processing error.
     *
     * @param on      the object the error occurred on
     * @param message the error message
     */
    @LogMessage(level = Level.ERROR)
    @Message(id = 203, value = "Processing error on '%s' with error '%s'")
    void processingError(Object on, String message);

    @Message(id = 204, value = "Bundle message inside a logger")
    String bundleMessage();
}
