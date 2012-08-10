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

import java.io.FileNotFoundException;
import java.io.OutputStream;
import java.util.Formattable;
import java.util.Formatter;

import org.jboss.logging.Cause;
import org.jboss.logging.FormatWith;
import org.jboss.logging.LogMessage;
import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;
import org.jboss.logging.Message;
import org.jboss.logging.Message.Format;
import org.jboss.logging.MessageLogger;

/**
 * @author James R. Perkins Jr. (jrp)
 */
@MessageLogger(projectCode = AbstractLoggerTest.PROJECT_CODE)
interface LegacyLogger {

    final String TEST_MSG = "No format%n";

    /**
     * The default logger.
     */
    LegacyLogger LOGGER = Logger.getMessageLogger(LegacyLogger.class, AbstractLoggerTest.CATEGORY);

    @LogMessage(level = Level.INFO)
    @Message(id = 900, value = "Hello %s.")
    void hello(String name);

    @LogMessage(level = Level.INFO)
    @Message(id = 901, value = "How are you %s?")
    void howAreYou(String name);

    @LogMessage(level = Level.INFO)
    @Message(id = 902, format = Format.NO_FORMAT, value = TEST_MSG)
    void noFormat();

    @LogMessage(level = Level.INFO)
    @Message(id = 903, format = Format.NO_FORMAT, value = TEST_MSG)
    void noFormatWithCause(@Cause Throwable cause);

    @LogMessage(level = Level.INFO)
    @Message(id = 904, value = "Test Message: %s")
    void formatWith(@FormatWith(CustomFormatter.class) String msg);

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
