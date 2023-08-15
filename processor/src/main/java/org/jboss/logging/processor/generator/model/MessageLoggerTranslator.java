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

import static org.jboss.jdeparser.JExprs.$v;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;

import org.jboss.jdeparser.JBlock;
import org.jboss.jdeparser.JClassDef;
import org.jboss.jdeparser.JMethodDef;
import org.jboss.jdeparser.JMod;
import org.jboss.logging.Logger;
import org.jboss.logging.processor.model.MessageInterface;
import org.jboss.logging.processor.model.MessageMethod;

/**
 * The java message logger translation class model.
 *
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 */
class MessageLoggerTranslator extends ClassModel {

    /**
     * The logger parameter name.
     */
    private static final String LOGGER_PARAMETER_NAME = "logger";

    /**
     * The translation map.
     */
    private final Map<MessageMethod, String> translations;
    private final String locale;

    /**
     * Create a MessageLogger with super class and interface.
     *
     * @param processingEnv    the processing environment
     * @param messageInterface the message interface to implement.
     * @param className        the implementation class name.
     * @param superClassName   the super class name
     * @param locale           the locale used to override the default locale
     * @param translations     the translation map.
     */
    public MessageLoggerTranslator(final ProcessingEnvironment processingEnv, final MessageInterface messageInterface,
            final String className, final String superClassName, final String locale,
            final Map<MessageMethod, String> translations) {
        super(processingEnv, messageInterface, className, superClassName);
        this.locale = locale;
        if (translations != null) {
            this.translations = translations;
        } else {
            this.translations = Collections.emptyMap();
        }
    }

    @Override
    public JClassDef generateModel() throws IllegalStateException {
        JClassDef classDef = super.generateModel();

        JMethodDef constructor = classDef.constructor(JMod.PUBLIC);
        constructor.param(JMod.FINAL, Logger.class, LOGGER_PARAMETER_NAME);

        JBlock constructorBody = constructor.body();
        constructorBody.callSuper().arg($v(LOGGER_PARAMETER_NAME));

        // Override the locale getter
        createLocaleGetter(locale, true);

        final Set<Map.Entry<MessageMethod, String>> entries = this.translations.entrySet();
        final Set<JMethodDef> methodNames = new LinkedHashSet<>();
        for (Map.Entry<MessageMethod, String> entry : entries) {
            JMethodDef method = addMessageMethod(entry.getKey(), entry.getValue());
            if (methodNames.add(method)) {
                method.annotate(Override.class);
            }
        }

        return classDef;
    }

}
