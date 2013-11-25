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

package org.jboss.logging.processor.generated;

import static org.jboss.logging.processor.util.Objects.areEqual;

import org.jboss.logging.Messages;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.Field;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.Message.Format;
import org.jboss.logging.annotations.MessageBundle;
import org.jboss.logging.annotations.Param;
import org.jboss.logging.annotations.Property;
import org.jboss.logging.processor.util.Objects;
import org.jboss.logging.processor.util.Objects.HashCodeBuilder;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@MessageBundle(projectCode = "MSG")
public interface ValidMessages {
    final String TEST_MSG = "Test%n";

    final ValidMessages MESSAGES = Messages.getBundle(ValidMessages.class);

    @Message(value = TEST_MSG)
    String testWithNewLine();

    @Message(format = Format.NO_FORMAT, value = TEST_MSG)
    String noFormat();

    @Message(format = Format.NO_FORMAT, value = TEST_MSG)
    RuntimeException noFormatException(@Cause Throwable cause);

    @Message(TEST_MSG)
    CustomException fieldMessage(@Field(name = "value") int value);

    @Message(TEST_MSG)
    CustomException paramMessage(@Param int value);

    @Message(TEST_MSG)
    CustomException propertyMessage(@Property int value);

    @Message(TEST_MSG)
    LoggingException loggingException(@Cause Exception e);

    static class CustomException extends RuntimeException {
        public int value;

        public CustomException() {
        }

        public CustomException(final int value, final String msg) {
            super(msg);
            this.value = value;
        }

        public CustomException(final String msg) {
            super(msg);
        }

        public CustomException(final Throwable t) {
            super(t);
        }

        public CustomException(final String msg, final Throwable t) {
            super(msg, t);
        }

        public void setValue(final int value) {
            this.value = value;
        }
    }

    static class LoggingException extends RuntimeException {

        public LoggingException(final Exception e) {
            super(e);
        }

        public LoggingException(final Exception e, final String msg) {
            super(msg, e);
        }
    }
}
