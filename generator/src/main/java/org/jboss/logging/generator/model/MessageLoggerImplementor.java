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
import org.jboss.logging.generator.LoggingTools;
import org.jboss.logging.generator.MethodDescriptor;
import org.jboss.logging.generator.MethodDescriptors;
import org.jboss.logging.generator.MethodParameter;

import java.lang.reflect.Method;

import static org.jboss.logging.generator.model.ClassModelUtil.formatMessageId;

/**
 * Used to generate a message logger implementation.
 * <p>
 * Creates an implementation of the interface passed in.
 * </p>
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public final class MessageLoggerImplementor extends ImplementationClassModel {

    private static final String LOG_FIELD_NAME = "log";
    private static final String FQCN_FIELD_NAME = "FQCN";
    private final boolean extendsBasicLogger;
    private JFieldVar log;

    /**
     * Creates a new message logger code model.
     *
     * @param interfaceName      the interface name.
     * @param methodDescriptors  the method descriptions
     * @param projectCode        the project code from the annotation.
     * @param extendsBasicLogger {@code true} if extending the basic logger, otherwise {@code false}.
     */
    public MessageLoggerImplementor(final String interfaceName, final MethodDescriptors methodDescriptors, final String projectCode, final boolean extendsBasicLogger) {
        super(interfaceName, methodDescriptors, projectCode, ImplementationType.LOGGER);
        this.extendsBasicLogger = extendsBasicLogger;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected JCodeModel generateModel() throws IllegalStateException {
        final JCodeModel codeModel = super.generateModel();
        log = getDefinedClass().field(JMod.PROTECTED | JMod.FINAL, LoggingTools.loggers().loggerClass(), LOG_FIELD_NAME);
        //Add a project code constant
        JFieldVar projectCodeVar = null;
        if (!getProjectCode().isEmpty()) {
            projectCodeVar = getDefinedClass().field(JMod.PRIVATE | JMod.STATIC | JMod.FINAL, String.class, "projectCode");
            projectCodeVar.init(JExpr.lit(getProjectCode()));
        }

        // Add FQCN
        final JFieldVar fqcn = getDefinedClass().field(JMod.PRIVATE | JMod.STATIC | JMod.FINAL, String.class, FQCN_FIELD_NAME);
        fqcn.init(getDefinedClass().dotclass().invoke("getName"));

        // Add default constructor
        final JMethod constructor = getDefinedClass().constructor(JMod.PUBLIC);
        final JVar constructorParam = constructor.param(JMod.FINAL, LoggingTools.loggers().loggerClass(), LOG_FIELD_NAME);
        final JBlock body = constructor.body();
        body.directStatement("this." + log.name() + " = " + constructorParam.name() + ";");

        // Process the method descriptors and add to the model before
        // writing.
        for (MethodDescriptor methodDesc : super.getMethodDescriptors()) {
            final JClass returnType = codeModel.ref(methodDesc.returnType().getReturnTypeAsString());
            // Create the method
            final JMethod jMethod = getDefinedClass().method(JMod.PUBLIC | JMod.FINAL, returnType, methodDesc.name());
            jMethod.annotate(Override.class);
            // Add the message method.
            final JMethod msgMethod = addMessageMethod(methodDesc);

            // Create the method body
            if (methodDesc.isLoggerMethod()) {
                createLoggerMethod(methodDesc, jMethod, msgMethod, projectCodeVar);
            } else {
                createBundleMethod(methodDesc, jMethod, msgMethod, projectCodeVar);
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
     * @param methodDesc     the method descriptor.
     * @param method         the method to create the body for.
     * @param msgMethod      the message method for retrieving the message.
     * @param projectCodeVar the project code variable
     */
    private void createLoggerMethod(final MethodDescriptor methodDesc, final JMethod method, final JMethod msgMethod, final JVar projectCodeVar) {
        // Create the body of the method and add the text
        final JBlock body = method.body();

        // Determine the log method
        final StringBuilder logMethod = new StringBuilder(methodDesc.loggerMethod());
        final JInvocation logInv = body.invoke(log, logMethod.toString());
        logInv.arg(JExpr.ref(FQCN_FIELD_NAME));
        logInv.arg(JExpr.direct(methodDesc.logLevelParameter()));
        // The clause must be first if there is one.
        if (methodDesc.hasCause()) {
            logInv.arg(JExpr.direct(methodDesc.cause().name()));
        } else {
            logInv.arg(JExpr._null());
        }
        // The next parameter is the message. Should be accessed via the
        // message retrieval method.
        if (methodDesc.hasMessageId() && projectCodeVar != null) {
            String formattedId = formatMessageId(methodDesc.messageId());
            logInv.arg(projectCodeVar.plus(JExpr.lit(formattedId)).plus(JExpr.invoke(msgMethod)));
        } else {
            logInv.arg(JExpr.invoke(msgMethod));
        }
        // Create the parameters
        for (MethodParameter param : methodDesc.parameters()) {
            final JClass paramType = getCodeModel().ref(param.fullType());
            final JVar var = method.param(JMod.FINAL, paramType, param.name());
            if (!param.equals(methodDesc.cause())) {
                logInv.arg(var);
            }
        }
    }

    /**
     * Create the bundle method body.
     *
     * @param methodDesc     the method descriptor.
     * @param method         the method to create the body for.
     * @param msgMethod      the message method for retrieving the message.
     * @param projectCodeVar the project code variable
     */
    private void createBundleMethod(final MethodDescriptor methodDesc, final JMethod method, final JMethod msgMethod, final JVar projectCodeVar) {
        // Create the body of the method and add the text
        final JBlock body = method.body();
        final JClass returnField = getCodeModel().ref(method.type().fullName());
        final JVar result = body.decl(returnField, "result");
        final JClass formatter = getCodeModel().ref(methodDesc.messageFormat().formatClass());
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
                final JClass paramType = getCodeModel().ref(param.fullType());
                JVar paramVar = method.param(JMod.FINAL, paramType, param.name());
                if (!param.isCause()) {
                    final String formatterClass = param.getFormatterClass();
                    if (formatterClass == null) {
                        formatterMethod.arg(paramVar);
                    } else {
                        formatterMethod.arg(JExpr._new(JClass.parse(getCodeModel(), formatterClass)).arg(paramVar));
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

    /**
     * Implements the basic logger methods.
     *
     * @param codeModel the code model to implement to.
     */
    private void implementBasicLogger(final JCodeModel codeModel) {
        for (Method m : LoggingTools.loggers().basicLoggerMethods()) {
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
                // Add the parameters to the body
                blBody.append("arg").append(argCount);
                String paramName = "arg" + argCount;
                argCount++;
                if (argCount < paramSize) {
                    // Create the method parameters
                    blMethod.param(JMod.FINAL, param, paramName);
                    blBody.append(", ");
                } else if (m.isVarArgs()) {
                    blMethod.varParam(param.getComponentType(), paramName);
                } else {
                    blMethod.param(JMod.FINAL, param, paramName);
                }
            }
            blBody.append(");");
            blMethod.body().directStatement(blBody.toString());
        }
    }
}
