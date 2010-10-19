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

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;

import org.jboss.logging.LogMessage;
import org.jboss.logging.Logger;
import org.jboss.logging.Message;
import org.jboss.logging.model.validation.MethodParameterValidator;
import org.jboss.logging.model.validation.ValidationException;

import com.sun.codemodel.internal.JBlock;
import com.sun.codemodel.internal.JClass;
import com.sun.codemodel.internal.JClassAlreadyExistsException;
import com.sun.codemodel.internal.JExpr;
import com.sun.codemodel.internal.JFieldVar;
import com.sun.codemodel.internal.JInvocation;
import com.sun.codemodel.internal.JMethod;
import com.sun.codemodel.internal.JMod;
import com.sun.codemodel.internal.JVar;
import org.jboss.logging.ToolLogger;

/**
 * @author James R. Perkins Jr. (jrp)
 *
 */
public final class MessageLoggerImplementor extends ImplementationClassModel {
    private static final String LOG_FIELD_NAME = "log";
    private JFieldVar log;
    private MethodDescriptor methodDescriptor;

    /**
     * Creates a new message logger code model.
     *
     * @param interfaceName
     *            the interface name.
     * @param projectCode
     *            the project code from the annotation.
     */
    public MessageLoggerImplementor(final ToolLogger logger, final String interfaceName,
            final String projectCode) {
        super(logger, interfaceName, projectCode, ImplementationType.LOGGER);
        methodDescriptor = new MethodDescriptor();
    }

    /*
     * (non-Javadoc)
     *
     * @see org.jboss.logging.CodeModel#addMethod(javax.lang.model.element.
     * ExecutableElement)
     */
    @Override
    public void addMethod(final ExecutableElement method) throws ValidationException {
        methodDescriptor = methodDescriptor.add(method);
        MethodParameterValidator.create(methodDescriptor).validate();
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
            // Add the message method.
            final JMethod msgMethod = addMessageMethod(methodName,
                    message.value(), message.id());
            // Determine the log method
            final StringBuilder logMethod = new StringBuilder(logLevel.name()
                    .toLowerCase());
            switch (methodDesc.message().format()) {
                case MESSAGE_FORMAT:
                    logMethod.append("v");
                    break;
                case PRINTF:
                    logMethod.append("f");
                    break;
            }
            // Create the body of the method and add the text
            final JBlock body = jMethod.body();
            final JInvocation logInv = body.invoke(log, logMethod.toString());
            // The clause must be first if there is one.
            if (methodDesc.hasClause()) {
                logInv.arg(JExpr.direct(methodDesc.causeVarName()));
            }
            // The next parameter is the message. Should be accessed via the
            // message retrieval method.
            logInv.arg(JExpr.invoke(msgMethod));
            // Create the parameters
            for (VariableElement param : methodDesc.parameters()) {
                final JClass paramType = codeModel().ref(
                        param.asType().toString());
                final JVar var = jMethod.param(JMod.FINAL, paramType, param
                        .getSimpleName().toString());
                if (!param.equals(methodDesc.cause())) {
                    logInv.arg(var);
                }
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see org.jboss.logging.model.CodeModel#initModel()
     */
    @Override
    public void initModel() throws JClassAlreadyExistsException {
        super.initModel();
        log = definedClass().field(JMod.PROTECTED | JMod.FINAL, Logger.class,
                LOG_FIELD_NAME);
        // Add default constructor
        final JMethod constructor = definedClass().constructor(JMod.PROTECTED);
        final JVar param = constructor.param(JMod.FINAL, Logger.class, LOG_FIELD_NAME);
        final JBlock body = constructor.body();
        body.directStatement("this." + log.name() + " = " + param.name() + ";");
    }

}
