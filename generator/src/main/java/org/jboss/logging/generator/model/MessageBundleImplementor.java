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

import com.sun.codemodel.internal.JBlock;
import com.sun.codemodel.internal.JClass;
import com.sun.codemodel.internal.JCodeModel;
import com.sun.codemodel.internal.JExpr;
import com.sun.codemodel.internal.JFieldVar;
import com.sun.codemodel.internal.JInvocation;
import com.sun.codemodel.internal.JMethod;
import com.sun.codemodel.internal.JMod;
import com.sun.codemodel.internal.JVar;
import org.jboss.logging.generator.MethodDescriptor;
import org.jboss.logging.generator.MethodDescriptors;
import org.jboss.logging.generator.MethodParameter;

import static org.jboss.logging.generator.model.ClassModelUtil.formatMessageId;

/**
 * Used to generate a message bundle implementation.
 * <p>
 * Creates an implementation of the interface passed in.
 * </p>
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 */
public class MessageBundleImplementor extends ImplementationClassModel {

    /**
     * Creates a new message bundle code model.
     *
     * @param interfaceName the interface name.
     * @param methodDescriptors the method descriptions
     * @param projectCode   the project code from the annotation.
     */
    public MessageBundleImplementor(final String interfaceName, final MethodDescriptors methodDescriptors, final String projectCode) {
        super(interfaceName, methodDescriptors, projectCode, ImplementationType.BUNDLE);
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
        for (MethodDescriptor methodDesc : super.getMethodDescriptors()) {
            final JClass returnType = codeModel.ref(methodDesc.returnType().getReturnTypeAsString());
            final JMethod jMethod = getDefinedClass().method(JMod.PUBLIC | JMod.FINAL, returnType, methodDesc.name());
            jMethod.annotate(Override.class);

            // Add the message method.
            final JMethod msgMethod = addMessageMethod(methodDesc);
            // Create the method body
            final JBlock body = jMethod.body();
            final JClass returnField = codeModel.ref(returnType.fullName());
            final JVar result = body.decl(returnField, "result");
            final JClass formatter = codeModel.ref(methodDesc.messageFormat().formatClass());
            final JInvocation formatterMethod = formatter.staticInvoke(methodDesc.messageFormat().staticMethod());
            if (methodDesc.parameters().isEmpty()) {
                // If the return type is an exception, initialize the exception.
                if (methodDesc.returnType().isException()) {
                    if (methodDesc.hasMessageId() && projectCodeVar != null) {
                        String formattedId = formatMessageId(methodDesc.messageId());
                        formatterMethod.arg(projectCodeVar.plus(JExpr.lit(formattedId)).plus(JExpr.invoke(msgMethod)));
                        initCause(result, returnField, body, methodDesc, formatterMethod);
                    } else {
                        initCause(result, returnField, body, methodDesc, JExpr.invoke(msgMethod));
                    }
                } else {
                    result.init(JExpr.invoke(msgMethod));
                }
            } else {
                if (methodDesc.hasMessageId() && projectCodeVar != null) {
                    String formattedId = formatMessageId(methodDesc.messageId());
                    formatterMethod.arg(projectCodeVar.plus(JExpr.lit(formattedId)).plus(JExpr.invoke(msgMethod)));
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
}
