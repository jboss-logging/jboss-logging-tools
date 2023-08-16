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

import org.jboss.logging.BasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.FormatWith;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.Message.Format;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.logging.annotations.ValidIdRange;

/**
 * @author James R. Perkins Jr. (jrp)
 */
@SuppressWarnings("unused")
@MessageLogger(projectCode = TestConstants.PROJECT_CODE)
@ValidIdRange(min = 100, max = 150)
public interface DefaultLogger extends BasicLogger {

    String TEST_MSG = "No format%n";

    /**
     * The default logger.
     */
    DefaultLogger LOGGER = Logger.getMessageLogger(DefaultLogger.class, TestConstants.CATEGORY);

    // Used to test the ambiguous log field in the DelegatingBasicLogger
    DefaultLogger log = Logger.getMessageLogger(DefaultLogger.class, TestConstants.CATEGORY);

    static DefaultLogger get(final String category) {
        return Logger.getMessageLogger(DefaultLogger.class, category);
    }

    default void initialized() {
        LOGGER.info("Initialized");
    }

    @LogMessage(level = Level.INFO)
    @Message(id = 100, value = "Hello %s.")
    void hello(String name);

    @LogMessage(level = Level.INFO)
    @Message(id = 101, value = "How are you %s?")
    void howAreYou(String name);

    @LogMessage(level = Level.INFO)
    @Message(id = 102, format = Format.NO_FORMAT, value = TEST_MSG)
    void noFormat();

    @LogMessage(level = Level.INFO, useThreadContext = true)
    @Message(id = 103, format = Format.NO_FORMAT, value = TEST_MSG)
    void noFormatWithCause(@Cause Throwable cause);

    @LogMessage(level = Level.INFO, useThreadContext = true)
    @Message(id = 104, value = "Test Message: %s")
    void formatWith(@FormatWith(CustomFormatter.class) String msg);

    @LogMessage(level = Level.ERROR)
    @Message(id = 105, value = "Valid values are; %s")
    void invalidSelection(String... validValues);

    @LogMessage(level = Level.ERROR)
    @Message(value = "Invalid value '%s'. Valid values are; %s")
    void invalidSelection(String selected, String[] validValues);

    class CustomFormatter {

        private final String msg;

        public CustomFormatter(final String msg) {
            this.msg = msg;
        }

        @Override
        public String toString() {
            return String.format("Message: %s", msg);
        }
    }
}
