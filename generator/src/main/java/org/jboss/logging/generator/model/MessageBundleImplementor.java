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

import com.sun.codemodel.internal.JClass;
import com.sun.codemodel.internal.JCodeModel;
import com.sun.codemodel.internal.JExpr;
import com.sun.codemodel.internal.JFieldVar;
import com.sun.codemodel.internal.JMethod;
import com.sun.codemodel.internal.JMod;
import org.jboss.logging.generator.intf.model.MessageInterface;
import org.jboss.logging.generator.intf.model.Method;

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
        super(messageInterface, ImplementationType.BUNDLE);
    }

    @Override
    protected JCodeModel generateModel() throws IllegalStateException {
        final JCodeModel codeModel = super.generateModel();
        //Add a project code constant
        JFieldVar projectCodeVar = null;
        if (!messageInterface().projectCode().isEmpty()) {
            projectCodeVar = getDefinedClass().field(JMod.PRIVATE | JMod.STATIC | JMod.FINAL, String.class, "projectCode");
            projectCodeVar.init(JExpr.lit(messageInterface().projectCode()));
        }
        // Add default constructor
        getDefinedClass().constructor(JMod.PROTECTED);
        ClassModelHelper.createReadResolveMethod(getDefinedClass());
        // Process the method descriptors and add to the model before
        // writing.
        for (Method method : messageInterface().methods()) {
            final JClass returnType = codeModel.ref(method.returnType().name());
            final JMethod jMethod = getDefinedClass().method(JMod.PUBLIC | JMod.FINAL, returnType, method.name());
            jMethod.annotate(Override.class);

            // Add the message method.
            final JMethod msgMethod = addMessageMethod(method);
            createBundleMethod(method, jMethod, msgMethod, projectCodeVar);
        }
        return codeModel;
    }
}
