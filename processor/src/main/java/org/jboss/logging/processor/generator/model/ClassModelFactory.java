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

package org.jboss.logging.processor.generator.model;

import static org.jboss.logging.processor.generator.model.ClassModelHelper.implementationClassName;
import static org.jboss.logging.processor.util.TranslationHelper.getEnclosingTranslationClassName;

import java.util.Map;
import javax.annotation.processing.Filer;

import org.jboss.logging.annotations.MessageBundle;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.logging.processor.model.MessageInterface;
import org.jboss.logging.processor.model.MessageMethod;
import org.jboss.logging.processor.util.ElementHelper;

/**
 * Creates a class model for the message interface.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class ClassModelFactory {

    /**
     * Private constructor for the factory.
     */
    private ClassModelFactory() {

    }

    /**
     * Creates an implementation code model from the message interface.
     *
     * @param filer            the filer used to create the source file
     * @param messageInterface the message interface to implement
     * @param useLogging31     whether or not jboss-logging 3.1 or higher is used
     * @param annotateOutput   true to have the generated class annotated with javax.annotation.Generated.
     *
     * @return the class model used to implement the interface.
     *
     * @throws IllegalArgumentException if interface is not annotated with {@link MessageBundle @MessageBundle} or {@link MessageLogger @MessageLogger}
     */
    public static ClassModel implementation(final Filer filer, final MessageInterface messageInterface, final boolean useLogging31, final boolean annotateOutput) throws IllegalArgumentException {
        if (ElementHelper.isAnnotatedWith(messageInterface, MessageBundle.class)) {
            return new MessageBundleImplementor(filer, messageInterface, annotateOutput);
        }
        if (ElementHelper.isAnnotatedWith(messageInterface, MessageLogger.class)) {
            return new MessageLoggerImplementor(filer, messageInterface, useLogging31, annotateOutput);
        }
        throw new IllegalArgumentException(String.format("Message interface %s is not a valid message logger or message bundle.", messageInterface));
    }

    /**
     * Creates a class model for created translation implementations of the message interface.
     * <p/>
     * <b>Note:</b> The implementation class must exist before the translation implementations can be created.
     *
     * @param filer             the filer used to create the source file
     * @param messageInterface  the message interface to implement.
     * @param translationSuffix the translation locale suffix.
     * @param translations      a map of the translations for the methods.
     * @param annotateOutput    true to have the generated class annotated with javax.annotation.Generated.
     *
     * @return the class model used to create translation implementations of the interface.
     *
     * @throws IllegalArgumentException if interface is not annotated with {@link MessageBundle @MessageBundle} or {@link MessageLogger @MessageLogger}
     */
    public static ClassModel translation(final Filer filer, final MessageInterface messageInterface, final String translationSuffix,
            final Map<MessageMethod, String> translations, final boolean annotateOutput) throws IllegalArgumentException {
        final String generatedClassName = implementationClassName(messageInterface, translationSuffix);
        final String superClassName = getEnclosingTranslationClassName(generatedClassName);
        if (ElementHelper.isAnnotatedWith(messageInterface, MessageBundle.class)) {
            return new MessageBundleTranslator(filer, messageInterface, generatedClassName, superClassName, translations, annotateOutput);
        }
        if (ElementHelper.isAnnotatedWith(messageInterface, MessageLogger.class)) {
            return new MessageLoggerTranslator(filer, messageInterface, generatedClassName, superClassName, translations, annotateOutput);
        }
        throw new IllegalArgumentException(String.format("Message interface %s is not a valid message logger or message bundle.", messageInterface));
    }
}
