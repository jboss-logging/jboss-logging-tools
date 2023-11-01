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

import static org.jboss.logging.processor.generator.model.ClassModelHelper.implementationClassName;
import static org.jboss.logging.processor.util.TranslationHelper.getEnclosingTranslationClassName;

import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;

import org.jboss.logging.annotations.MessageBundle;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.logging.processor.model.MessageInterface;
import org.jboss.logging.processor.model.MessageMethod;

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
     * @param processingEnv    the processing environment
     * @param messageInterface the message interface to implement
     *
     * @return the class model used to implement the interface.
     *
     * @throws IllegalArgumentException if interface is not annotated with {@link MessageBundle @MessageBundle} or
     *                                  {@link MessageLogger @MessageLogger}
     */
    public static ClassModel implementation(final ProcessingEnvironment processingEnv, final RoundEnvironment roundEnv, final MessageInterface messageInterface) throws IllegalArgumentException {
        if (messageInterface.isAnnotatedWith(MessageBundle.class)) {
            return new MessageBundleImplementor(processingEnv, roundEnv, messageInterface);
        }
        if (messageInterface.isAnnotatedWith(MessageLogger.class)) {
            return new MessageLoggerImplementor(processingEnv, roundEnv, messageInterface);
        }
        throw new IllegalArgumentException(
                String.format("Message interface %s is not a valid message logger or message bundle.", messageInterface));
    }

    /**
     * Creates a class model for created translation implementations of the message interface.
     * <p/>
     * <b>Note:</b> The implementation class must exist before the translation implementations can be created.
     *
     * @param processingEnv     the processing environment
     * @param messageInterface  the message interface to implement.
     * @param translationSuffix the translation locale suffix.
     * @param translations      a map of the translations for the methods.
     *
     * @return the class model used to create translation implementations of the interface.
     *
     * @throws IllegalArgumentException if interface is not annotated with {@link MessageBundle @MessageBundle} or
     *                                  {@link MessageLogger @MessageLogger}
     */
    public static ClassModel translation(final ProcessingEnvironment processingEnv, final RoundEnvironment roundEnv, final MessageInterface messageInterface, final String translationSuffix, final Map<MessageMethod, String> translations) throws IllegalArgumentException {
        final String generatedClassName = implementationClassName(messageInterface, translationSuffix);
        final String superClassName = getEnclosingTranslationClassName(generatedClassName);
        // The locale should be the same as the translationsSuffix minus the leading _
        final String locale = translationSuffix.substring(1);
        if (messageInterface.isAnnotatedWith(MessageBundle.class)) {
            return new MessageBundleTranslator(processingEnv, roundEnv, messageInterface, generatedClassName, superClassName, locale, translations);
        }
        if (messageInterface.isAnnotatedWith(MessageLogger.class)) {
            return new MessageLoggerTranslator(processingEnv, roundEnv, messageInterface, generatedClassName, superClassName, locale, translations);
        }
        throw new IllegalArgumentException(
                String.format("Message interface %s is not a valid message logger or message bundle.", messageInterface));
    }
}
