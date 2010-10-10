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
package org.jboss.logging;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

import com.sun.codemodel.internal.JBlock;
import com.sun.codemodel.internal.JClass;
import com.sun.codemodel.internal.JClassAlreadyExistsException;
import com.sun.codemodel.internal.JExpr;
import com.sun.codemodel.internal.JFieldVar;
import com.sun.codemodel.internal.JInvocation;
import com.sun.codemodel.internal.JMethod;
import com.sun.codemodel.internal.JMod;
import com.sun.codemodel.internal.JVar;

/**
 * @author James R. Perkins Jr. (jrp)
 * 
 */
public class MessageBundleCodeModel extends CodeModel {
    private MethodDescriptor methodDescriptor;

    /**
     * Creates a new message bundle code model.
     * 
     * @param interfaceName
     *            the interface name.
     * @param projectCode
     *            the project code from the annotation.
     * @throws JClassAlreadyExistsException
     *             should never happen, but could be thrown if the class has
     *             already been defined
     */
    public MessageBundleCodeModel(final String interfaceName,
            final String projectCode) throws JClassAlreadyExistsException {
        super(interfaceName, projectCode);
        methodDescriptor = new MethodDescriptor();
        init();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jboss.logging.CodeModel#type()
     */
    @Override
    public Implementation type() {
        return Implementation.BUNDLE;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jboss.logging.CodeModel#addMethod(javax.lang.model.element.
     * ExecutableElement)
     */
    @Override
    public void addMethod(final ExecutableElement method) {
        methodDescriptor = methodDescriptor.add(method);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.jboss.logging.CodeModel#beforeWrite()
     */
    @Override
    public void beforeWrite() {
        // Process the method descriptors and add to the model before
        // writing.
        for (MethodDescriptor methodDesc : methodDescriptor) {
            final String methodName = methodDesc.name();
            final JClass returnType = codeModel().ref(
                    methodDesc.returnTypeAsString());
            final JMethod jMethod = definedClass().method(
                    JMod.PUBLIC | JMod.FINAL, returnType, methodName);
            jMethod.annotate(Override.class);
            final Message message = methodDesc.message();

            final JMethod msgMethod = addMessageMethod(methodName,
                    message.value(), message.id());

            final JBlock body = jMethod.body();
            final JClass returnField = codeModel().ref(returnType.fullName());
            final JVar result = body.decl(returnField, "result");
            JClass formatter = null;
            // Determine the format type
            switch (message.format()) {
                case MESSAGE_FORMAT:
                    formatter = codeModel().ref(java.text.MessageFormat.class);
                    break;
                case PRINTF:
                    formatter = codeModel().ref(String.class);
                    break;
            }
            final JInvocation formatterMethod = formatter
                    .staticInvoke("format");
            formatterMethod.arg(JExpr.invoke(msgMethod));
            // Create the parameters
            for (VariableElement param : methodDesc.parameters()) {
                final JClass paramType = codeModel().ref(
                        param.asType().toString());
                JVar paramVar = jMethod.param(JMod.FINAL, paramType, param
                        .getSimpleName().toString());
                formatterMethod.arg(paramVar);
            }
            // Setup the return type
            if (methodDesc.hasClause()
                    && codeModel().ref(Throwable.class).isAssignableFrom(
                            returnField)) {
                result.init(JExpr._new(returnField));
                JInvocation inv = body.invoke(result, "initCause");
                inv.arg(JExpr.ref(methodDesc.causeVarName()));
            } else {
                result.init(formatterMethod);
            }
            body._return(result);
        }
    }

    /**
     * Initializes global variables for the class to be created.
     */
    private void init() {
        final JFieldVar instance = definedClass().field(
                JMod.PUBLIC | JMod.STATIC | JMod.FINAL, definedClass(),
                "INSTANCE");
        instance.init(JExpr._new(definedClass()));
        // Add default constructor
        definedClass().constructor(JMod.PROTECTED);
        final JMethod readResolveMethod = definedClass().method(JMod.PROTECTED,
                definedClass(), "readResolve");
        readResolveMethod.body()._return(instance);
    }

}
