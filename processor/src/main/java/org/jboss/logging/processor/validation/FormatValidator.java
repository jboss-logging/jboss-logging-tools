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

package org.jboss.logging.processor.validation;

/**
 * Date: 14.06.2011
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public interface FormatValidator {

    /**
     * The number of arguments needed for the format.
     *
     * @return the number of arguments needed.
     */
    int argumentCount();

    /**
     * Returns the format string used for validation.
     *
     * @return the format string.
     */
    String format();

    /**
     * Returns {@code true} of the format is valid, otherwise {@code false}.
     *
     * @return {@code true} of the format is valid, otherwise {@code false}.
     */
    boolean isValid();

    /**
     * A detail message if {@link #isValid()} returns {@code false}, otherwise an empty string.
     *
     * @return a detailed message.
     */
    String detailMessage();

    /**
     * A summary message if {@link #isValid()} returns {@code false}, otherwise an empty string.
     *
     * @return a summary message.
     */
    String summaryMessage();
}
