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

import org.jboss.logging.processor.util.Objects;

@SuppressWarnings("unused")
public class SignatureMessagesInvalidTextException extends RuntimeException {
    final String value;
    final int position;

    public SignatureMessagesInvalidTextException(final String value) {
        this.value = value;
        position = -1;
    }

    public SignatureMessagesInvalidTextException(final String msg, final String value) {
        super(msg);
        this.value = value;
        position = -1;
    }

    public SignatureMessagesInvalidTextException(final Throwable cause) {
        throw new IllegalStateException("Should never be chosen");
    }

    public SignatureMessagesInvalidTextException(final String message, final Throwable cause) {
        throw new IllegalStateException("Should never be chosen");
    }

    public SignatureMessagesInvalidTextException(final String msg, final Throwable cause, final String value) {
        super(msg, cause);
        this.value = value;
        position = -1;
    }

    public SignatureMessagesInvalidTextException(final int position, final Throwable cause, final String value,
            final String msg) {
        super(msg, cause);
        this.value = value;
        this.position = position;
    }

    public SignatureMessagesInvalidTextException(final Integer position, final Throwable cause, final String value,
            final String msg) {
        throw new IllegalStateException("Should never be chosen");
    }

    @Override
    public int hashCode() {
        return Objects.HashCodeBuilder.builder()
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
        if (!(obj instanceof SignatureMessagesInvalidTextException)) {
            return false;
        }
        final SignatureMessagesInvalidTextException other = (SignatureMessagesInvalidTextException) obj;
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
