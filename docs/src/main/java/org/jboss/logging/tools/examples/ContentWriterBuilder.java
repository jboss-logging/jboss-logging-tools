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
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Objects;

/**
 * Builds a {@link ContentWriter}.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@SuppressWarnings("unused")
public class ContentWriterBuilder {
    private final Path path;
    private boolean append = true;
    private String encoding = null;

    private ContentWriterBuilder(final Path path) {
        this.path = path;
    }

    /**
     * Creates a new builder.
     *
     * @param path the path where the content should be written to, cannot be {@code null}
     *
     * @return the new content writer
     */
    public static ContentWriterBuilder of(final Path path) {
        Objects.requireNonNull(path, ErrorMessages.MESSAGES.nullParam("path"));
        return new ContentWriterBuilder(path);
    }

    /**
     * Set the encoding of for the content. If the encoding is not a valid {@link Charset} a default encoding of
     * {@link StandardCharsets#UTF_8} will be used.
     *
     * @param encoding the encoding to use
     *
     * @return this builder
     */
    public ContentWriterBuilder setEncoding(final String encoding) {
        this.encoding = encoding;
        return this;
    }

    /**
     * Indicates whether or not the content should be appended to any previous content already written. The default
     * value is {@link true}.
     *
     * @param append {@code false} if the created writer should overwrite previously written content, otherwise
     *               {@code true}
     *
     * @return this builder
     */
    public ContentWriterBuilder setAppend(final boolean append) {
        this.append = append;
        return this;
    }

    /**
     * Creates the {@link ContentWriter}.
     *
     * @return the created writer
     *
     * @throws IOException if a failure creating the writer occurs
     */
    public ContentWriter build() throws IOException {
        final OpenOption[] options;
        if (append) {
            options = new OpenOption[] { StandardOpenOption.CREATE, StandardOpenOption.APPEND };
        } else {
            options = new OpenOption[] { StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING };
        }
        Charset charset = StandardCharsets.UTF_8;
        if (encoding != null) {
            try {
                charset = Charset.forName(encoding);
            } catch (UnsupportedCharsetException e) {
                AppLogger.LOGGER.encodingNotFound(encoding, charset);
            }
        }
        final BufferedWriter writer = Files.newBufferedWriter(path, charset, options);
        return new ContentWriter(path, writer);
    }
}
