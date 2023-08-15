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

import static org.jboss.logging.processor.util.Objects.areEqual;

import org.jboss.logging.Messages;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageBundle;
import org.jboss.logging.annotations.Param;
import org.jboss.logging.annotations.Signature;
import org.jboss.logging.processor.util.Objects;
import org.jboss.logging.processor.util.Objects.HashCodeBuilder;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@MessageBundle(projectCode = "SIG")
public interface SignatureMessages {
    String TEST_MSG = "Test signature message";

    SignatureMessages MESSAGES = Messages.getBundle(SignatureMessages.class);

    @Message(TEST_MSG)
    RedirectException redirect(@Param int responseCode, @Param String location);

    RedirectException redirect(@Cause Throwable cause, @Param int responseCode, @Param String location);

    @Signature({ String.class, String.class })
    RedirectException redirect(@Cause Throwable cause, @Param String location);

    @Message(TEST_MSG)
    TestException test();

    TestException test(@Cause Throwable cause);

    @Message(TEST_MSG)
    InvalidTextException invalidText(@Param String text);

    InvalidTextException invalidText(@Cause Throwable cause, @Param String text);

    @Signature(causeIndex = 1, messageIndex = 3, value = { int.class, Throwable.class, String.class, String.class })
    InvalidTextException invalidText(@Param int position, @Cause Throwable cause, @Param String text);

    @SuppressWarnings("unused")
    class RedirectException extends RuntimeException {
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

        public RedirectException(final String msg, final String location) {
            super(msg);
            this.statusCode = 301;
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
    class TestException extends RuntimeException {

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
    class InvalidTextException extends RuntimeException {
        final String value;
        final int position;

        public InvalidTextException(final String value) {
            this.value = value;
            position = -1;
        }

        public InvalidTextException(final String msg, final String value) {
            super(msg);
            this.value = value;
            position = -1;
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
            position = -1;
        }

        public InvalidTextException(final int position, final Throwable cause, final String value, final String msg) {
            super(msg, cause);
            this.value = value;
            this.position = position;
        }

        public InvalidTextException(final Integer position, final Throwable cause, final String value, final String msg) {
            throw new IllegalStateException("Should never be chosen");
        }

        @Override
        public int hashCode() {
            return HashCodeBuilder.builder()
                    .add(value)
                    .add(position)
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
                    areEqual(position, other.position) &&
                    areEqual(getMessage(), other.getMessage()) &&
                    areEqual(getCause(), other.getCause());
        }

        @Override
        public String toString() {
            return Objects.ToStringBuilder.of(this)
                    .add("value", value)
                    .add("position", position)
                    .add("message", getMessage())
                    .add("cause", getCause())
                    .toString();
        }
    }
}
