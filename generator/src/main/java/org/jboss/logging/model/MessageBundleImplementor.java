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

import org.jboss.logging.generator.MethodParameter;
import org.jboss.logging.generator.ReturnType;
import org.jboss.logging.generator.MethodDescriptor;
import com.sun.codemodel.internal.*;


import static org.jboss.logging.model.ClassModelUtil.STRING_ID_FORMAT;

/**
 * Used to generate a message bundle implementation.
 * <p>
 * Creates an implementation of the interface passed in.
 * </p>
 *
 * @author James R. Perkins Jr. (jrp)
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
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
    public MessageBundleImplementor(final String interfaceName, final String projectCode) {
        super(interfaceName, projectCode, ImplementationType.BUNDLE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected JCodeModel generateModel() throws IllegalStateException {
        final JCodeModel codeModel = super.generateModel();
        //Add a project code constant
        JFieldVar projectCodeVar = null;
        if (!getProjectCode().isEmpty()) {
            projectCodeVar = getDefinedClass().field(JMod.PRIVATE | JMod.STATIC | JMod.FINAL, String.class, "projectCode");
            projectCodeVar.init(JExpr.lit(getProjectCode()));
        }
        // Add default constructor
        getDefinedClass().constructor(JMod.PROTECTED);
        ClassModelUtil.createReadResolveMethod(getDefinedClass());
        // Process the method descriptors and add to the model before
        // writing.
        for (MethodDescriptor methodDesc : methodDescriptor) {
            final JClass returnType = codeModel.ref(methodDesc.returnType().getReturnTypeAsString());
            final JMethod jMethod = getDefinedClass().method(JMod.PUBLIC | JMod.FINAL, returnType, methodDesc.name());
            jMethod.annotate(Override.class);

            // Add the message method.
            final JMethod msgMethod = addMessageMethod(methodDesc.name(), methodDesc.messageValue());
            // Create the method body
            final JBlock body = jMethod.body();
            final JClass returnField = codeModel.ref(returnType.fullName());
            final JVar result = body.decl(returnField, "result");
            if (methodDesc.parameters().isEmpty()) {
                if (methodDesc.returnType().isException()) {
                    initCause(result, returnField, body, methodDesc, JExpr.invoke(msgMethod));
                } else {
                    result.init(JExpr.invoke(msgMethod));
                }
            } else {
                final JClass formatter = codeModel.ref(methodDesc.messageFormat().formatClass());
                final JInvocation formatterMethod = formatter.staticInvoke(methodDesc.messageFormat().staticMethod());
                if (methodDesc.hasMessageId() && projectCodeVar != null) {
                    String formatedId = String.format(STRING_ID_FORMAT, methodDesc.messageId());
                    formatterMethod.arg(projectCodeVar.plus(JExpr.lit(formatedId)).plus(JExpr.invoke(msgMethod)));
                } else {
                    formatterMethod.arg(JExpr.invoke(msgMethod));
                }
                // Create the parameters
                for (MethodParameter param : methodDesc.parameters()) {
                    final JClass paramType = codeModel.ref(param.fullType());
                    JVar paramVar = jMethod.param(JMod.FINAL, paramType, param.name());
                    if (!param.isCause()) {
                        final String formatterClass = param.getFormatterClass();
                        if (formatterClass == null) {
                            formatterMethod.arg(paramVar);
                        } else {
                            formatterMethod.arg(JExpr._new(JClass.parse(codeModel, formatterClass)).arg(paramVar));
                        }
                    }
                }
                // Setup the return type
                if (methodDesc.returnType().isException()) {
                    initCause(result, returnField, body, methodDesc, formatterMethod);
                } else {
                    result.init(formatterMethod);
                }
            }
            body._return(result);
        }
        return codeModel;
    }

    private void initCause(final JVar result, final JClass returnField, final JBlock body, final MethodDescriptor methodDesc, final JInvocation formatterMethod) {
        ReturnType desc = methodDesc.returnType();
        if (desc.hasStringAndThrowableConstructor() && methodDesc.hasCause()) {
            result.init(JExpr._new(returnField).arg(formatterMethod).arg(JExpr.ref(methodDesc.cause().name())));
        } else if (desc.hasThrowableAndStringConstructor() && methodDesc.hasCause()) {
            result.init(JExpr._new(returnField).arg(JExpr.ref(methodDesc.cause().name())).arg(formatterMethod));
        } else if (desc.hasStringConsturctor() && methodDesc.hasCause()) {
            result.init(JExpr._new(returnField).arg(formatterMethod));
            if (methodDesc.hasCause()) {
                JInvocation resultInv = body.invoke(result, "initCause");
                resultInv.arg(JExpr.ref(methodDesc.cause().name()));
            }
        } else if (desc.hasThrowableConstructor() && methodDesc.hasCause()) {
            result.init(JExpr._new(returnField).arg(methodDesc.cause().name()));
        } else if (methodDesc.hasCause()) {
            result.init(JExpr._new(returnField));
            JInvocation resultInv = body.invoke(result, "initCause");
            resultInv.arg(JExpr.ref(methodDesc.cause().name()));
        } else {
            result.init(JExpr._new(returnField));
        }
    }
}
