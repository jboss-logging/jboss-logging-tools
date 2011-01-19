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

import static org.jboss.logging.util.ElementHelper.*;

import com.sun.codemodel.internal.JBlock;
import com.sun.codemodel.internal.JClass;
import com.sun.codemodel.internal.JCodeModel;
import com.sun.codemodel.internal.JExpr;
import com.sun.codemodel.internal.JFieldVar;
import com.sun.codemodel.internal.JInvocation;
import com.sun.codemodel.internal.JMethod;
import com.sun.codemodel.internal.JMod;
import com.sun.codemodel.internal.JVar;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import org.jboss.logging.LogMessage;
import org.jboss.logging.Logger;
import org.jboss.logging.Message;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import org.jboss.logging.util.BasicLoggerDescriptor;

/**
 * Used to generate a message logger implementation.
 * <p>
 * Creates an implementation of the interface passed in.
 * </p>
 *
 * @author James R. Perkins Jr. (jrp)
 *
 */
public final class MessageLoggerImplementor extends ImplementationClassModel {

    private static final String LOG_FIELD_NAME = "log";

    private final boolean extendsBasicLogger;

    private JFieldVar log;

    /**
     * Creates a new message logger code model.
     *
     * @param interfaceName
     *            the interface name.
     * @param projectCode
     *            the project code from the annotation.
     */
    public MessageLoggerImplementor(final String interfaceName,
            final String projectCode, final boolean extendsBasicLogger) {
        super(interfaceName, projectCode, ImplementationType.LOGGER);
        this.extendsBasicLogger = extendsBasicLogger;
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
        log = getDefinedClass().field(JMod.PROTECTED | JMod.FINAL, Logger.class, LOG_FIELD_NAME);
        // Add default constructor
        final JMethod constructor = getDefinedClass().constructor(JMod.PROTECTED);
        final JVar constructorParam = constructor.param(JMod.FINAL, Logger.class, LOG_FIELD_NAME);
        final JBlock body = constructor.body();
        body.directStatement("this." + log.name() + " = " + constructorParam.name() + ";");

        // Process the method descriptors and add to the model before
        // writing.
        for (MethodDescriptor methodDesc : methodDescriptor) {
            final JClass returnType = codeModel.ref(methodDesc.returnTypeAsString());
            final String methodName = methodDesc.name();
            // Create the method
            final JMethod jMethod = getDefinedClass().method(JMod.PUBLIC | JMod.FINAL, returnType, methodName);
            jMethod.annotate(Override.class);
            // Find the annotations
            final Message message = methodDesc.message();
            // Add the message method.
            final JMethod msgMethod = addMessageMethod(methodName, message.value());
            final JVar messageIdVar = addIdVar(methodDesc.name(), message.id());

            // Create the method body
            if (isLoggerMethod(methodDesc.method())) {
                createLoggerMethod(methodDesc, jMethod, msgMethod, messageIdVar);
            } else {
                createBundleMethod(methodDesc, jMethod, msgMethod, messageIdVar);
            }
        }
        // If implementing the BasicLogger, defer to the global log instance.
        if (extendsBasicLogger) {
            implementBasicLogger(codeModel);
        }
        return codeModel;
    }

    /**
     * Create the logger method body.
     * 
     * @param methodDesc   the method descriptor.
     * @param method       the method to create the body for.
     * @param msgMethod    the message method for retrieving the message.
     * @param messageIdVar the message id variable.
     */
    private void createLoggerMethod(final MethodDescriptor methodDesc, final JMethod method, final JMethod msgMethod, final JVar messageIdVar) {
        final LogMessage logMessage = methodDesc.logMessage();
        // Set a default log level
        Logger.Level logLevel = Logger.Level.INFO;
        if (logMessage != null) {
            logLevel = logMessage.level();
        }
        // Create the body of the method and add the text
        final JBlock body = method.body();

        // Determine the log method
        final StringBuilder logMethod = new StringBuilder(logLevel.name().toLowerCase());
        switch (methodDesc.message().format()) {
            case MESSAGE_FORMAT:
                logMethod.append("v");
                break;
            case PRINTF:
                logMethod.append("f");
                break;
        }
        final JInvocation logInv = body.invoke(log, logMethod.toString());
        // The clause must be first if there is one.
        if (methodDesc.hasCause()) {
            logInv.arg(JExpr.direct(methodDesc.causeVarName()));
        }
        // The next parameter is the message. Should be accessed via the
        // message retrieval method.
        if (messageIdVar == null) {
            logInv.arg(JExpr.invoke(msgMethod));
        } else {
            logInv.arg(messageIdVar.plus(JExpr.invoke(msgMethod)));
        }
        // Create the parameters
        for (VariableElement param : methodDesc.parameters()) {
            final JClass paramType = getCodeModel().ref(param.asType().toString());
            final JVar var = method.param(JMod.FINAL, paramType, param.getSimpleName().toString());
            if (!param.equals(methodDesc.cause())) {
                logInv.arg(var);
            }
        }
    }

    /**
     * Create the bundle method body.
     * 
     * @param methodDesc   the method descriptor.
     * @param method       the method to create the body for.
     * @param msgMethod    the message method for retrieving the message.
     * @param messageIdVar the message id variable.
     */
    private void createBundleMethod(final MethodDescriptor methodDesc, final JMethod method, final JMethod msgMethod, final JVar messageIdVar) {
        // Create the body of the method and add the text
        final JBlock body = method.body();
        final JClass returnField = getCodeModel().ref(method.type().fullName());
        final JVar result = body.decl(returnField, "result");
        JClass formatter = null;
        // Determine the format type
        switch (methodDesc.message().format()) {
            case MESSAGE_FORMAT:
                formatter = getCodeModel().ref(java.text.MessageFormat.class);
                break;
            case PRINTF:
                formatter = getCodeModel().ref(String.class);
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
            final JClass paramType = getCodeModel().ref(param.asType().toString());
            JVar paramVar = method.param(JMod.FINAL, paramType, param.getSimpleName().toString());
            formatterMethod.arg(paramVar);
        }
        // Setup the return type
        result.init(formatterMethod);
        body._return(result);
    }

    /**
     * Implements the basic logger methods.
     * 
     * @param codeModel the code model to implement to.
     */
    private void implementBasicLogger(final JCodeModel codeModel) {
        for (Method m : BasicLoggerDescriptor.getInstance().getMethods()) {
            if (!m.getReturnType().isPrimitive()) {
                codeModel.ref(m.getReturnType());
            }
            final JMethod blMethod = getDefinedClass().method(JMod.PUBLIC | JMod.FINAL, m.getReturnType(), m.getName());
            blMethod.annotate(Override.class);
            final int paramSize = m.getParameterTypes().length;
            int argCount = 0;
            final StringBuilder blBody = new StringBuilder();
            if (!m.getReturnType().equals(Void.TYPE)) {
                blBody.append("return ");
            }
            blBody.append("this.").append(log.name()).append(".").append(m.getName()).append("(");
            for (Class<?> param : m.getParameterTypes()) {
                // Reference the parameter with the code model
                codeModel.ref(param);
                // Create the method parameters
                blMethod.param(JMod.FINAL, param, "arg" + argCount);
                // Add the parameters to the body
                blBody.append("arg").append(argCount);
                argCount++;
                if (argCount < paramSize) {
                    blBody.append(", ");
                }
            }
            blBody.append(");");
            blMethod.body().directStatement(blBody.toString());
        }
    }
}
