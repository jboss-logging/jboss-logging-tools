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
import com.sun.codemodel.internal.JFieldVar;
import com.sun.codemodel.internal.JMethod;
import com.sun.codemodel.internal.JMod;
import com.sun.codemodel.internal.JVar;

/**
 * @author James R. Perkins Jr. (jrp)
 * 
 */
public final class MessageLoggerCodeModel extends CodeModel {
    private JFieldVar log;
    private MethodDescriptor methodDescriptor;

    /**
     * Creates a new message logger code model.
     * 
     * @param interfaceName
     *            the interface name.
     * @param projectCode
     *            the project code from the annotation.
     * @throws JClassAlreadyExistsException
     *             should never happen, but could be thrown if the class has
     *             already been defined
     */
    public MessageLoggerCodeModel(final String interfaceName,
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
        return Implementation.LOGGER;
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
            // Create the method
            final JMethod jMethod = definedClass().method(
                    JMod.PUBLIC | JMod.FINAL, codeModel().VOID, methodName);
            jMethod.annotate(Override.class);
            // Find the annotations
            final Message message = methodDesc.message();
            final LogMessage logMessage = methodDesc.logMessage();
            // Set a default log level
            Logger.Level logLevel = Logger.Level.INFO;
            if (logMessage != null) {
                logLevel = logMessage.level();
            }
            // Create the body text for the method.
            final StringBuilder bodyText = new StringBuilder();
            // Add the message method.
            final JMethod msgMethod = addMessageMethod(methodName,
                    message.value(), message.id());

            bodyText.append(log.name());
            bodyText.append(".");
            bodyText.append(logLevel.name().toLowerCase());

            switch (methodDesc.message().format()) {
                case MESSAGE_FORMAT:
                    bodyText.append("v");
                    break;
                case PRINTF:
                    bodyText.append("f");
                    break;
            }
            bodyText.append("(");
            // The clause must be first if there is one.
            if (methodDesc.hasClause()) {
                bodyText.append(methodDesc.cause().getSimpleName().toString());
                bodyText.append(", ");
            }
            // The next parameter is the message. Should be accessed via the
            // message retrieval method.
            bodyText.append(msgMethod.name());
            bodyText.append("()");
            // Create the parameters
            for (VariableElement param : methodDesc.parameters()) {
                final JClass paramType = codeModel().ref(
                        param.asType().toString());
                final JVar var = jMethod.param(JMod.FINAL, paramType, param
                        .getSimpleName().toString());
                bodyText.append(", ");
                bodyText.append(var.name());
            }
            bodyText.append(");");
            // Create the body of the method and add the text
            final JBlock body = jMethod.body();
            body.directStatement(bodyText.toString());
        }
        super.writeClass(fileObject);
    }

    /**
     * Initializes global variables for the class to be created.
     */
    private void init() {
        log = definedClass().field(JMod.PROTECTED | JMod.FINAL, Logger.class,
                "log");
        // Add default constructor
        final JMethod constructor = definedClass().constructor(JMod.PROTECTED);
        constructor.param(JMod.FINAL, Logger.class, "log");
        final JBlock body = constructor.body();
        body.directStatement("this.log = log;");
    }

}
