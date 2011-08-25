/*
 * JBoss, Home of Professional Open Source Copyright 2010, Red Hat, Inc., and
 * individual contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.jboss.logging.generator.model;

import com.sun.codemodel.internal.JCodeModel;
import com.sun.codemodel.internal.JDefinedClass;
import com.sun.codemodel.internal.JMethod;
import com.sun.codemodel.internal.JMod;
import org.jboss.logging.generator.intf.model.MessageInterface;
import org.jboss.logging.generator.intf.model.Method;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The java message bundle class model.
 *
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 */
class MessageBundleTranslator extends ClassModel {

    /**
     * The translation map.
     */
    private final Map<Method, String> translations;

    /**
     * Create a MessageBundle with super class and interface.
     *
     * @param messageInterface the message interface to implement.
     * @param className        the implementation class name.
     * @param superClassName   the super class name
     * @param translations     the translation map.
     */
    public MessageBundleTranslator(final MessageInterface messageInterface, final String className, final String superClassName, final Map<Method, String> translations) {
        super(messageInterface, className, superClassName);

        if (translations != null) {
            this.translations = translations;
        } else {
            this.translations = Collections.emptyMap();
        }
    }

    @Override
    public JCodeModel generateModel() throws IllegalStateException {
        JCodeModel model = super.generateModel();
        JDefinedClass definedClass = getDefinedClass();

        JMethod constructor = definedClass.constructor(JMod.PROTECTED);
        constructor.body().invoke("super");

        JMethod readResolve = ClassModelHelper.createReadResolveMethod(definedClass);
        readResolve.annotate(Override.class);

        final Set<Map.Entry<Method, String>> entries = translations.entrySet();
        final Set<String> methodNames = new HashSet<String>();
        for (Map.Entry<Method, String> entry : entries) {
            JMethod method = addMessageMethod(entry.getKey(), entry.getValue());
            if (methodNames.add(method.name())) {
                method.annotate(Override.class);
            }
        }

        return model;
    }
}
