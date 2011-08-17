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
import com.sun.codemodel.internal.JType;
import com.sun.codemodel.internal.JVar;
import org.jboss.logging.generator.LoggingTools;
import org.jboss.logging.generator.MessageInterface;
import org.jboss.logging.generator.MessageMethod;
import org.jboss.logging.generator.MessageReturnType;
import org.jboss.logging.generator.MethodParameter;

import java.util.HashSet;
import java.util.Set;

import static org.jboss.logging.generator.model.ClassModelUtil.formatMessageId;

/**
 * Used to generate a message logger implementation.
 * <p>
 * Creates an implementation of the interface passed in.
 * </p>
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
final class MessageLoggerImplementor extends ImplementationClassModel {

    private static final String LOG_FIELD_NAME = "log";
    private static final String FQCN_FIELD_NAME = "FQCN";
    private JFieldVar log;

    /**
     * Creates a new message logger code model.
     *
     * @param messageInterface the message interface to implement.
     */
    public MessageLoggerImplementor(final MessageInterface messageInterface) {
        super(messageInterface, ImplementationType.LOGGER);
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
        if (!messageInterface().projectCode().isEmpty()) {
            projectCodeVar = getDefinedClass().field(JMod.PRIVATE | JMod.STATIC | JMod.FINAL, String.class, "projectCode");
            projectCodeVar.init(JExpr.lit(messageInterface().projectCode()));
        }

        // Add FQCN
        final JFieldVar fqcn = getDefinedClass().field(JMod.PRIVATE | JMod.STATIC | JMod.FINAL, String.class, FQCN_FIELD_NAME);
        fqcn.init(getDefinedClass().dotclass().invoke("getName"));

        // Add default constructor
        final JMethod constructor = getDefinedClass().constructor(JMod.PUBLIC);
        final JVar constructorParam = constructor.param(JMod.FINAL, LoggingTools.loggers().loggerClass(), LOG_FIELD_NAME);
        final JBlock body = constructor.body();
        body.directStatement("this." + log.name() + " = " + constructorParam.name() + ";");

        // Process the method descriptors and add to the model before writing.
        final Set<MessageMethod> methods = new HashSet<MessageMethod>();
        methods.addAll(messageInterface().methods());
        for (MessageInterface messageInterface : messageInterface().extendedInterfaces()) {
            // Handle basic logger
            if (messageInterface.isBasicLogger()) {
                implementBasicLogger(codeModel, messageInterface);
                continue;
            }
            methods.addAll(messageInterface.methods());
        }
        for (MessageMethod messageMethod : methods) {
            final JClass returnType = codeModel.ref(messageMethod.returnType().name());
            // Create the method
            final JMethod jMethod = getDefinedClass().method(JMod.PUBLIC | JMod.FINAL, returnType, messageMethod.name());
            jMethod.annotate(Override.class);
            // Add the message method.
            final JMethod msgMethod = addMessageMethod(messageMethod);

            // Create the method body
            if (messageMethod.isLoggerMethod()) {
                createLoggerMethod(messageMethod, jMethod, msgMethod, projectCodeVar);
            } else {
                createBundleMethod(messageMethod, jMethod, msgMethod, projectCodeVar);
            }
        }
        return codeModel;
    }

    /**
     * Create the logger method body.
     *
     * @param messageMethod  the message method.
     * @param method         the method to create the body for.
     * @param msgMethod      the message method for retrieving the message.
     * @param projectCodeVar the project code variable
     */
    private void createLoggerMethod(final MessageMethod messageMethod, final JMethod method, final JMethod msgMethod, final JVar projectCodeVar) {
        // Create the body of the method and add the text
        final JBlock body = method.body();

        // Determine the log method
        final StringBuilder logMethod = new StringBuilder(messageMethod.loggerMethod());
        final JInvocation logInv = body.invoke(log, logMethod.toString());
        logInv.arg(JExpr.ref(FQCN_FIELD_NAME));
        logInv.arg(JExpr.direct(messageMethod.logLevelParameter()));
        // The clause must be first if there is one.
        if (messageMethod.hasCause()) {
            logInv.arg(JExpr.direct(messageMethod.cause().name()));
        } else {
            logInv.arg(JExpr._null());
        }
        // The next parameter is the message. Should be accessed via the
        // message retrieval method.
        final MessageMethod.Message message = messageMethod.message();
        if (message.hasId() && projectCodeVar != null) {
            String formattedId = formatMessageId(message.id());
            logInv.arg(projectCodeVar.plus(JExpr.lit(formattedId)).plus(JExpr.invoke(msgMethod)));
        } else {
            logInv.arg(JExpr.invoke(msgMethod));
        }
        // Create the parameters
        for (MethodParameter param : messageMethod.allParameters()) {
            final JClass paramType = getCodeModel().ref(param.type());
            final JVar var = method.param(JMod.FINAL, paramType, param.name());
            if (!param.isCause() && !param.isParam()) {
                logInv.arg(var);
            }
        }
    }

    /**
     * Implements the basic logger methods.
     *
     * @param codeModel the code model to implement to.
     */
    private void implementBasicLogger(final JCodeModel codeModel, final MessageInterface messageInterface) {
        for (MessageMethod method : messageInterface.methods()) {
            final JType returnType;
            if (method.returnType().equals(MessageReturnType.VOID)) {
                returnType = codeModel.VOID;
            } else {
                if (method.returnType().isPrimitive()) {
                    returnType = JClass.parse(codeModel, method.returnType().name());
                } else {
                    returnType = codeModel.ref(method.returnType().name());
                }
            }
            final JMethod blMethod = getDefinedClass().method(JMod.PUBLIC | JMod.FINAL, returnType, method.name());
            blMethod.annotate(Override.class);
            final JBlock body = blMethod.body();
            final JInvocation logInv = JExpr.invoke(log, method.name());
            for (MethodParameter parameter : method.allParameters()) {
                final JType param = codeModel.ref(parameter.type());
                final JVar methodParam;
                if (parameter.isVarArgs()) {
                    methodParam = blMethod.varParam(param, parameter.name());
                } else if (parameter.isArray()) {
                    methodParam = blMethod.param(JMod.FINAL, param.array(), parameter.name());
                } else {
                    methodParam = blMethod.param(JMod.FINAL, param, parameter.name());
                }
                logInv.arg(methodParam);
            }
            if (MessageReturnType.VOID.equals(method.returnType())) {
                body.add(logInv);
            } else {
                body._return(logInv);
            }
        }
    }
}
