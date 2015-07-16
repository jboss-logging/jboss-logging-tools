/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

/**
 *
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
@MessageLogger(projectCode = AbstractLoggerTest.PROJECT_CODE)
@ValidIdRange(min = 100, max = 150)
interface DefaultLogger extends BasicLogger {

    final String TEST_MSG = "No format%n";

    /**
     * The default logger.
     */
    DefaultLogger LOGGER = Logger.getMessageLogger(DefaultLogger.class, AbstractLoggerTest.CATEGORY);

    // Used to test the ambiguous log field in the DelegatingBasicLogger
    DefaultLogger log = Logger.getMessageLogger(DefaultLogger.class, AbstractLoggerTest.CATEGORY);

    @LogMessage(level = Level.INFO)
    @Message(id = 100, value = "Hello %s.")
    void hello(String name);

    @LogMessage(level = Level.INFO)
    @Message(id = 101, value = "How are you %s?")
    void howAreYou(String name);

    @LogMessage(level = Level.INFO)
    @Message(id = 102, format = Format.NO_FORMAT, value = TEST_MSG)
    void noFormat();

    @LogMessage(level = Level.INFO)
    @Message(id = 103, format = Format.NO_FORMAT, value = TEST_MSG)
    void noFormatWithCause(@Cause Throwable cause);

    @LogMessage(level = Level.INFO)
    @Message(id = 104, value = "Test Message: %s")
    void formatWith(@FormatWith(CustomFormatter.class) String msg);

    @LogMessage(level = Level.ERROR)
    @Message(id = 105, value = "Valid values are; %s")
    void invalidSelection(String... validValues);

    @LogMessage(level = Level.ERROR)
    @Message(id = Message.INHERIT, value = "Invalid value '%s'. Valid values are; %s")
    void invalidSelection(String selected, String[] validValues);

    static class CustomFormatter {

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
