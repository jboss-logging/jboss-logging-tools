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

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Filer;

import org.jboss.jdeparser.JClassDef;
import org.jboss.jdeparser.JMethodDef;
import org.jboss.jdeparser.JMod;
import org.jboss.logging.processor.model.MessageInterface;
import org.jboss.logging.processor.model.MessageMethod;

/**
 * The java message bundle class model.
 *
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 */
class MessageBundleTranslator extends ClassModel {

    /**
     * The translation map.
     */
    private final Map<MessageMethod, String> translations;

    /**
     * Create a MessageBundle with super class and interface.
     *
     * @param filer            the filer used to create the source file
     * @param messageInterface the message interface to implement.
     * @param className        the implementation class name.
     * @param superClassName   the super class name
     * @param translations     the translation map.
     */
    public MessageBundleTranslator(final Filer filer, final MessageInterface messageInterface, final String className, final String superClassName, final Map<MessageMethod, String> translations) {
        super(filer, messageInterface, className, superClassName);

        if (translations != null) {
            this.translations = translations;
        } else {
            this.translations = Collections.emptyMap();
        }
    }

    @Override
    public JClassDef generateModel() throws IllegalStateException {
        JClassDef classDef = super.generateModel();

        JMethodDef constructor = classDef.constructor(JMod.PROTECTED);
        constructor.body().callSuper();

        JMethodDef readResolve = createReadResolveMethod();
        readResolve.annotate(Override.class);

        final Set<Map.Entry<MessageMethod, String>> entries = translations.entrySet();
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
