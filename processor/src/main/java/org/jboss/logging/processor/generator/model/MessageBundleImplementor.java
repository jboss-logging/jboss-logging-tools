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

import java.util.LinkedHashSet;
import java.util.Set;
import javax.annotation.processing.Filer;

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
     * @param filer            the filer used to create the source file
     * @param messageInterface the message interface to implement.
     */
    public MessageBundleImplementor(final Filer filer, final MessageInterface messageInterface) {
        super(filer, messageInterface);
    }

    @Override
    protected JClassDef generateModel() throws IllegalStateException {
        final JClassDef classDef = super.generateModel();
        // Add default constructor
        classDef.constructor(JMod.PROTECTED);
        createReadResolveMethod();
        final Set<MessageMethod> messageMethods = new LinkedHashSet<>();
        messageMethods.addAll(messageInterface().methods());
        for (MessageInterface messageInterface : messageInterface().extendedInterfaces()) {
            if (ElementHelper.isAnnotatedWith(messageInterface, MessageBundle.class) || ElementHelper.isAnnotatedWith(messageInterface, MessageLogger.class)) {
                messageMethods.addAll(messageInterface.methods());
            }
        }
        // Process the method descriptors and add to the model before
        // writing.
        for (MessageMethod messageMethod : messageMethods) {
            createBundleMethod(classDef, messageMethod);
        }
        return classDef;
    }
}
