/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2017 Red Hat, Inc., and individual contributors
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

package org.jboss.logging.tools.examples;

import java.io.Closeable;

/**
 * An error with access to the failing {@link Closeable}.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@SuppressWarnings("unused")
public class CloseException extends RuntimeException {
    private final Closeable closeable;

    /**
     * Creates a new error.
     *
     * @param msg       the message for the error
     * @param closeable the failed closeable
     */
    public CloseException(final String msg, final Closeable closeable) {
        super(msg);
        this.closeable = closeable;
    }

    /**
     * Creates a new error.
     *
     * @param msg       the message for the error
     * @param cause     the cause of the error
     * @param closeable the failed closeable
     */
    public CloseException(final String msg, final Throwable cause, final Closeable closeable) {
        super(msg, cause);
        this.closeable = closeable;
    }

    /**
     * Returns the failed closeable.
     *
     * @return the failed closeable
     */
    public Closeable getCloseable() {
        return closeable;
    }
}
