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

package org.jboss.logging.processor.generator.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.jboss.logging.annotations.MessageBundle;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.logging.processor.model.MessageInterface;
import org.jboss.logging.processor.util.ElementHelper;

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
        return sdf.format(new Date());
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
        if (ElementHelper.isAnnotatedWith(messageInterface, MessageBundle.class)) {
            result.append("_$bundle");
        } else if (ElementHelper.isAnnotatedWith(messageInterface, MessageLogger.class)) {
            result.append("_$logger");
        } else {
            throw new IllegalArgumentException(String.format("Message interface %s is not a message bundle or message logger.", messageInterface));
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
    public static String implementationClassName(final MessageInterface messageInterface, final String translationSuffix) throws IllegalArgumentException {
        return implementationClassName(messageInterface) + translationSuffix;
    }
}
