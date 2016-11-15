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

import static org.jboss.logging.processor.util.Objects.areEqual;

import org.jboss.logging.Messages;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageBundle;
import org.jboss.logging.annotations.Param;
import org.jboss.logging.processor.util.Objects;
import org.jboss.logging.processor.util.Objects.HashCodeBuilder;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@MessageBundle(projectCode = "SIG")
public interface SignatureMessages {
    final String TEST_MSG = "Test signature message";

    final SignatureMessages MESSAGES = Messages.getBundle(SignatureMessages.class);

    @Message(TEST_MSG)
    RedirectException redirect(@Param int responseCode, @Param String location);

    RedirectException redirect(@Cause Throwable cause, @Param int responseCode, @Param String location);

    @Message(TEST_MSG)
    TestException test();

    TestException test(@Cause Throwable cause);

    @Message(TEST_MSG)
    InvalidTextException invalidText(@Param String text);

    InvalidTextException invalidText(@Cause Throwable cause, @Param String text);

    @SuppressWarnings("unused")
    static class RedirectException extends RuntimeException {
        final int statusCode;
        final String location;

        public RedirectException(final String msg, final Throwable cause, final int statusCode, final String location) {
            super(msg, cause);
            this.statusCode = statusCode;
            this.location = location;
        }

        public RedirectException(final int statusCode, final String location) {
            throw new IllegalStateException("Should never be chosen");
        }

        public RedirectException(final Throwable cause, final int statusCode, final String location) {
            throw new IllegalStateException("Should never be chosen");
        }

        public RedirectException(final String msg, final int statusCode, final String location) {
            super(msg);
            this.statusCode = statusCode;
            this.location = location;
        }

        @Override
        public int hashCode() {
            return HashCodeBuilder.builder()
                    .add(statusCode)
                    .add(location)
                    .add(getMessage())
                    .add(getCause())
                    .toHashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof RedirectException)) {
                return false;
            }
            final RedirectException other = (RedirectException) obj;
            return areEqual(statusCode, other.statusCode) &&
                    areEqual(location, other.location) &&
                    areEqual(getMessage(), other.getMessage()) &&
                    areEqual(getCause(), other.getCause());
        }

        @Override
        public String toString() {
            return Objects.ToStringBuilder.of(this)
                    .add("statusCode", statusCode)
                    .add("location", location)
                    .add("message", getMessage())
                    .add("cause", getCause())
                    .toString();
        }
    }

    @SuppressWarnings("unused")
    static class TestException extends RuntimeException {

        public TestException(final String message) {
            super(message);
        }

        public TestException(final String message, final Throwable cause) {
            super(message, cause);
        }

        public TestException(final Throwable cause) {
            throw new IllegalStateException("Should never be chosen");
        }

        @Override
        public int hashCode() {
            return HashCodeBuilder.builder()
                    .add(getMessage())
                    .add(getCause())
                    .toHashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof TestException)) {
                return false;
            }
            final TestException other = (TestException) obj;
            return areEqual(getMessage(), other.getMessage()) &&
                    areEqual(getCause(), other.getCause());
        }

        @Override
        public String toString() {
            return Objects.ToStringBuilder.of(this)
                    .add("message", getMessage())
                    .add("cause", getCause())
                    .toString();
        }
    }

    @SuppressWarnings("unused")
    static class InvalidTextException extends RuntimeException {
        final String value;

        public InvalidTextException(final String value) {
            this.value = value;
        }

        public InvalidTextException(final String msg, final String value) {
            super(msg);
            this.value = value;
        }

        public InvalidTextException(final Throwable cause) {
            throw new IllegalStateException("Should never be chosen");
        }

        public InvalidTextException(final String message, final Throwable cause) {
            throw new IllegalStateException("Should never be chosen");
        }

        public InvalidTextException(final String msg, final Throwable cause, final String value) {
            super(msg, cause);
            this.value = value;
        }

        @Override
        public int hashCode() {
            return HashCodeBuilder.builder()
                    .add(value)
                    .add(getMessage())
                    .add(getCause())
                    .toHashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof InvalidTextException)) {
                return false;
            }
            final InvalidTextException other = (InvalidTextException) obj;
            return areEqual(value, other.value) &&
                    areEqual(getMessage(), other.getMessage()) &&
                    areEqual(getCause(), other.getCause());
        }

        @Override
        public String toString() {
            return Objects.ToStringBuilder.of(this)
                    .add("value", value)
                    .add("message", getMessage())
                    .add("cause", getCause())
                    .toString();
        }
    }
}
