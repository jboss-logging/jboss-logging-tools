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

package org.jboss.logging.processor.generated;

import java.util.function.Supplier;

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
@SuppressWarnings("unused")
@MessageLogger(projectCode = TestConstants.PROJECT_CODE)
@ValidIdRanges({
        @ValidIdRange(min = 200, max = 202),
        @ValidIdRange(min = 203, max = 204)
})
public interface ValidLogger {

    ValidLogger LOGGER = Logger.getMessageLogger(ValidLogger.class, TestConstants.CATEGORY);

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
    @Message("Processing error in module '%s'")
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

    @LogMessage(level = Level.ERROR)
    @Message("Error: %s")
    void expensiveLog(Supplier<String> error);

    @LogMessage(level = Level.ERROR)
    @Message("Error: %s")
    void expensiveLogArray(Supplier<Object[]> error);

    @LogMessage(level = Level.WARN)
    @Message("Expected: %s")
    void expectedValues(String... expected);

    @LogMessage(level = Level.DEBUG)
    @Message("Debug: %s")
    void debugValues(Supplier<String> values);
}
