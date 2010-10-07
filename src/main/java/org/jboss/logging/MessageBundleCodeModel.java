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

import java.io.IOException;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.tools.JavaFileObject;

import com.sun.codemodel.internal.JBlock;
import com.sun.codemodel.internal.JClass;
import com.sun.codemodel.internal.JClassAlreadyExistsException;
import com.sun.codemodel.internal.JExpr;
import com.sun.codemodel.internal.JFieldVar;
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
     * @see org.jboss.logging.CodeModel#writeClass(javax.tools.JavaFileObject)
     */
    @Override
    public void writeClass(final JavaFileObject fileObject) throws IOException {
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
            // Create the parameters
            for (VariableElement param : methodDesc.parameters()) {
                final JClass paramType = codeModel().ref(
                        param.asType().toString());
                jMethod.param(JMod.FINAL, paramType, param.getSimpleName()
                        .toString());
            }

            final JBlock body = jMethod.body();
            final JClass returnField = codeModel().ref(returnType.fullName());
            JVar var = body.decl(returnField, "result");
            var.init(JExpr.invoke(msgMethod));
            body._return(var);
        }
        super.writeClass(fileObject);
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
