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

import org.jboss.logging.Field;
import org.jboss.logging.Messages;
import org.jboss.logging.Cause;
import org.jboss.logging.Message;
import org.jboss.logging.Message.Format;
import org.jboss.logging.MessageBundle;
import org.jboss.logging.Param;
import org.jboss.logging.Property;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@MessageBundle(projectCode = "MSG")
public interface LegacyMessages {
    final String TEST_MSG = "Test%n";

    final LegacyMessages MESSAGES = Messages.getBundle(LegacyMessages.class);

    @Message(TEST_MSG)
    CustomException fieldMessage(@Field(name = "value") int value);

    @Message(TEST_MSG)
    CustomException paramMessage(@Param int value);

    @Message(TEST_MSG)
    CustomException propertyMessage(@Property int value);

    static class CustomException extends RuntimeException {
        public int value;

        public CustomException() {
            super();
        }

        public CustomException(final int value) {
            super();
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
}
