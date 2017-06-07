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

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.Objects;

/**
 * A writer for writing content to a file.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class ContentWriter implements Closeable, Flushable {

    private final Object outputLock = new Object();

    private final Path contentPath;
    private final BufferedWriter writer;
    private volatile boolean autoFlush = true;

    ContentWriter(final Path contentPath, final BufferedWriter writer) {
        this.contentPath = Objects.requireNonNull(contentPath, ErrorMessages.MESSAGES.nullParam("contentPath"));
        this.writer = Objects.requireNonNull(writer, ErrorMessages.MESSAGES.nullParam("writer"));
    }

    // tag::write[]

    /**
     * Writes the value of the object to the file.
     *
     * @param value the value to write, cannot be {@code null}
     *
     * @throws UncheckedIOException if an error occurs writing the data
     */
    public void write(final Object value) {
        AppLogger.LOGGER.appVersion("ContentWriter", 1, 0, 0, "Beta1"); // <1>
        Objects.requireNonNull(value, ErrorMessages.MESSAGES.nullParam("value")); // <2>
        write(Objects.requireNonNull(value, ErrorMessages.MESSAGES.nullParam("value")).toString());
    }

    /**
     * Writes the value to the file.
     *
     * @param value the value to write, cannot be {@code null} or an {@linkplain String#isEmpty() empty string}.
     *
     * @throws UncheckedIOException if an error occurs writing the data
     */
    public void write(final String value) {
        AppLogger.LOGGER.appVersion("ContentWriter", 1, 0, 0, "Beta1");
        if (Objects.requireNonNull(value, ErrorMessages.MESSAGES.nullParam("value")).isEmpty()) {
            throw ErrorMessages.MESSAGES.invalidValue(value); // <3>
        }
        try {
            synchronized (outputLock) {
                writer.write(value);
                writer.newLine();
                if (autoFlush) {
                    flush();
                }
            }
        } catch (IOException e) {
            throw ErrorMessages.MESSAGES.operationFailed(UncheckedIOException::new, e, "write"); // <4>
        }
    }
    // end::write[]

    /**
     * Set the value to {@code false} if the buffer should not automatically {@linkplain #flush() flush} on each write.
     * <p>
     * Defaults to {@code true}.
     * </p>
     *
     * @param autoFlush {@code true} to {@linkplain #flush() flush} on each write, otherwise {@code false} to flush
     *                  when the buffer determines it should be flushed
     */
    public void setAutoFlush(final boolean autoFlush) {
        this.autoFlush = autoFlush;
    }

    // tag::close[]

    @Override
    public void close() {
        try {
            synchronized (outputLock) {
                writer.close();
            }
            AppLogger.LOGGER.tracef("ContentWriter %s was successfully closed.", this);
        } catch (Exception e) {
            throw ErrorMessages.MESSAGES.closeFailure(e, this);
        }
    }

    /**
     * Safely close this writer logging any errors that occur during closing.
     */
    public void safeClose() {
        try {
            synchronized (outputLock) {
                writer.close();
            }
            AppLogger.LOGGER.tracef("ContentWriter %s was successfully closed.", this);
        } catch (Exception e) {
            AppLogger.LOGGER.closeFailure(e, this); // <5>
        }
    }
    // end::close[]

    @Override
    public void flush() {
        try {
            synchronized (outputLock) {
                writer.flush();
            }
        } catch (IOException e) {
            throw ErrorMessages.MESSAGES.operationFailed(UncheckedIOException::new, e, "flush");
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(contentPath);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof ContentWriter)) {
            return false;
        }
        final ContentWriter other = (ContentWriter) obj;
        return Objects.equals(contentPath, other.contentPath);
    }

    @Override
    public String toString() {
        return "ContentWriter[" + contentPath.toAbsolutePath() + "]";
    }
}
