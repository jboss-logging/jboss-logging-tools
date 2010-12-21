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
package org.jboss.logging.model;

import com.sun.codemodel.internal.JBlock;
import com.sun.codemodel.internal.JClass;
import com.sun.codemodel.internal.JCodeModel;
import com.sun.codemodel.internal.JExpr;
import com.sun.codemodel.internal.JInvocation;
import com.sun.codemodel.internal.JMethod;
import com.sun.codemodel.internal.JMod;
import com.sun.codemodel.internal.JVar;
import org.jboss.logging.Message;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

/**
 * Used to generate a message bundle implementation.
 * <p>
 * Creates an implementation of the interface passed in.
 * </p>
 *
 * @author James R. Perkins Jr. (jrp)
 * @author Kevin Pollet
 *
 */
public class MessageBundleImplementor extends ImplementationClassModel {

    /**
     * Creates a new message bundle code model.
     *
     * @param interfaceName
     *            the interface name.
     * @param projectCode
     *            the project code from the annotation.
     */
    public MessageBundleImplementor(final String interfaceName,
            final String projectCode) {
        super(interfaceName, projectCode, ImplementationType.BUNDLE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addMethod(final ExecutableElement method) {
        super.addMethod(method);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected JCodeModel generateModel() throws IllegalStateException {
        final JCodeModel codeModel = super.generateModel();
        // Add default constructor
        getDefinedClass().constructor(JMod.PROTECTED);
        ClassModelUtil.createReadResolveMethod(getDefinedClass());
        // Process the method descriptors and add to the model before
        // writing.
        for (MethodDescriptor methodDesc : methodDescriptor) {
            final JClass returnType = codeModel.ref(methodDesc.returnTypeAsString());
            final JMethod jMethod = getDefinedClass().method(JMod.PUBLIC | JMod.FINAL, returnType, methodDesc.name());
            jMethod.annotate(Override.class);

            final Message message = methodDesc.message();
            // Add the message method.
            final JMethod msgMethod = addMessageMethod(methodDesc.name(),
                    message.value());
            final JVar messageIdVar = addIdVar(methodDesc.name(), message.id());
            // Create the method body
            final JBlock body = jMethod.body();
            final JClass returnField = codeModel.ref(returnType.fullName());
            final JVar result = body.decl(returnField, "result");
            JClass formatter = null;
            // Determine the format type
            switch (message.format()) {
                case MESSAGE_FORMAT:
                    formatter = codeModel.ref(java.text.MessageFormat.class);
                    break;
                case PRINTF:
                    formatter = codeModel.ref(String.class);
                    break;
            }
            final JInvocation formatterMethod = formatter.staticInvoke("format");
            if (messageIdVar == null) {
                formatterMethod.arg(JExpr.invoke(msgMethod));
            } else {
                formatterMethod.arg(messageIdVar.plus(JExpr.invoke(msgMethod)));
            }
            // Create the parameters
            for (VariableElement param : methodDesc.parameters()) {
                final JClass paramType = codeModel.ref(param.asType().toString());
                JVar paramVar = jMethod.param(JMod.FINAL, paramType, param.getSimpleName().toString());
                formatterMethod.arg(paramVar);
            }
            // Setup the return type
            if (methodDesc.hasCause() && codeModel.ref(Throwable.class).isAssignableFrom(returnField)) {
                result.init(JExpr._new(returnField));
                JInvocation inv = body.invoke(result, "initCause");
                inv.arg(JExpr.ref(methodDesc.causeVarName()));
            } else {
                result.init(formatterMethod);
            }
            body._return(result);
        }
        return codeModel;
    }
}
