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

package org.jboss.logging.processor.generator.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.jboss.logging.annotations.MessageBundle;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.logging.processor.model.MessageInterface;

/**
 * Utilities for the code model.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public final class ClassModelHelper {

    private static final String STRING_ID_FORMAT2 = "%s%06d: ";

    /**
     * Constructor for singleton model.
     */
    private ClassModelHelper() {
    }

    /**
     * Returns the current date formatted in the ISO 8601 format.
     *
     * @return the current date formatted in ISO 8601.
     */
    static String generatedDateValue() {
        final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        Date d;
        d = System.getenv("SOURCE_DATE_EPOCH") == null ?
          new Date() :
          new Date(1000 * Long.parseLong(System.getenv("SOURCE_DATE_EPOCH")));
        return sdf.format(d);
    }

    /**
     * Formats message id.
     *
     * @param projectCode the project code for the message
     * @param messageId   the message id to format
     *
     * @return the formatted message id
     */
    public static String formatMessageId(final String projectCode, final int padLength, final int messageId) {
        return String.format(STRING_ID_FORMAT2, projectCode, messageId);
    }

    /**
     * Creates the implementation class name for the message interface.
     *
     * @param messageInterface the message interface to generate the implementation name for.
     *
     * @return the implementation class name
     *
     * @throws IllegalArgumentException if the message interface is not a message bundle or a message logger.
     */
    public static String implementationClassName(final MessageInterface messageInterface) throws IllegalArgumentException {
        final StringBuilder result = new StringBuilder(messageInterface.simpleName());
        if (messageInterface.isAnnotatedWith(MessageBundle.class)) {
            result.append("_$bundle");
        } else if (messageInterface.isAnnotatedWith(MessageLogger.class)) {
            result.append("_$logger");
        } else {
            throw new IllegalArgumentException(
                    String.format("Message interface %s is not a message bundle or message logger.", messageInterface));
        }
        return result.toString();
    }

    /**
     * Creates the implementation class name for the message interface.
     *
     * @param messageInterface  the message interface to generate the implementation name for.
     * @param translationSuffix the local suffix for the translation.
     *
     * @return the implementation class name
     *
     * @throws IllegalArgumentException if the message interface is not a message bundle or a message logger.
     */
    public static String implementationClassName(final MessageInterface messageInterface, final String translationSuffix)
            throws IllegalArgumentException {
        return implementationClassName(messageInterface) + translationSuffix;
    }
}
