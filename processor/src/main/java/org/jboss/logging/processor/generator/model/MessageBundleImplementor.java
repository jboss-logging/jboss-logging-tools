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

import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.processing.ProcessingEnvironment;

import org.jboss.jdeparser.JCall;
import org.jboss.jdeparser.JClassDef;
import org.jboss.jdeparser.JMod;
import org.jboss.logging.annotations.MessageBundle;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.logging.processor.model.MessageInterface;
import org.jboss.logging.processor.model.MessageMethod;
import org.jboss.logging.processor.util.ElementHelper;

/**
 * Used to generate a message bundle implementation.
 * <p>
 * Creates an implementation of the interface passed in.
 * </p>
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 */
class MessageBundleImplementor extends ImplementationClassModel {

    /**
     * Creates a new message bundle code model.
     *
     * @param processingEnv    the processing environment
     * @param messageInterface the message interface to implement.
     */
    public MessageBundleImplementor(final ProcessingEnvironment processingEnv, final MessageInterface messageInterface) {
        super(processingEnv, messageInterface);
    }

    @Override
    protected JClassDef generateModel() throws IllegalStateException {
        final JClassDef classDef = super.generateModel();
        // Add default constructor
        classDef.constructor(JMod.PROTECTED);
        createReadResolveMethod();
        final JCall localeGetter = createLocaleGetter(null, false);
        final Set<MessageMethod> messageMethods = new LinkedHashSet<>();
        messageMethods.addAll(messageInterface().methods());
        for (MessageInterface messageInterface : messageInterface().extendedInterfaces()) {
            if (messageInterface.isAnnotatedWith(MessageBundle.class) || messageInterface.isAnnotatedWith(MessageLogger.class)) {
                messageMethods.addAll(messageInterface.methods());
            }
        }
        // Process the method descriptors and add to the model before
        // writing.
        for (MessageMethod messageMethod : messageMethods) {
            createBundleMethod(classDef, localeGetter, messageMethod);
        }
        return classDef;
    }
}
