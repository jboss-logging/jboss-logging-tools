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

import java.util.LinkedHashSet;
import java.util.Set;

import org.jboss.jdeparser.JClass;
import org.jboss.jdeparser.JDeparser;
import org.jboss.jdeparser.JMethod;
import org.jboss.jdeparser.JMod;
import org.jboss.logging.processor.model.MessageInterface;
import org.jboss.logging.processor.model.MessageInterface.AnnotatedType;
import org.jboss.logging.processor.model.MessageMethod;

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
     * @param messageInterface the message interface to implement.
     */
    public MessageBundleImplementor(final MessageInterface messageInterface) {
        super(messageInterface);
    }

    @Override
    protected JDeparser generateModel() throws IllegalStateException {
        final JDeparser codeModel = super.generateModel();
        // Add default constructor
        getDefinedClass().constructor(JMod.PROTECTED);
        createReadResolveMethod();
        final Set<MessageMethod> messageMethods = new LinkedHashSet<MessageMethod>();
        messageMethods.addAll(messageInterface().methods());
        for (MessageInterface messageInterface : messageInterface().extendedInterfaces()) {
            // Handle logger interface
            if (messageInterface.getAnnotatedType() == AnnotatedType.NONE) {
                continue;
            }
            messageMethods.addAll(messageInterface.methods());
        }
        // Process the method descriptors and add to the model before
        // writing.
        for (MessageMethod messageMethod : messageMethods) {
            final JClass returnType = codeModel.directClass(messageMethod.returnType().name());
            final JMethod jMethod = getDefinedClass().method(JMod.PUBLIC | JMod.FINAL, returnType, messageMethod.name());

            // Add the message messageMethod.
            final JMethod msgMethod = addMessageMethod(messageMethod);
            createBundleMethod(messageMethod, jMethod, msgMethod);
        }
        return codeModel;
    }
}
