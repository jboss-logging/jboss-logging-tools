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
import com.sun.codemodel.internal.JExpression;
import com.sun.codemodel.internal.JFieldVar;
import com.sun.codemodel.internal.JInvocation;
import com.sun.codemodel.internal.JMethod;
import com.sun.codemodel.internal.JMod;
import com.sun.codemodel.internal.JVar;
import org.jboss.logging.generator.intf.model.MessageInterface;
import org.jboss.logging.generator.intf.model.MessageMethod;
import org.jboss.logging.generator.intf.model.Parameter;

import java.util.HashSet;
import java.util.Set;

import static org.jboss.logging.generator.Tools.loggers;
import static org.jboss.logging.generator.model.ClassModelHelper.formatMessageId;

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

    /**
     * Creates a new message logger code model.
     *
     * @param messageInterface the message interface to implement.
     */
    public MessageLoggerImplementor(final MessageInterface messageInterface) {
        super(messageInterface);
    }

    @Override
    protected JCodeModel generateModel() throws IllegalStateException {
        final JCodeModel codeModel = super.generateModel();
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
        final JVar constructorParam = constructor.param(JMod.FINAL, loggers().loggerClass(), LOG_FIELD_NAME);
        final JBlock constructorBody = constructor.body();
        final JExpression logger;
        if (messageInterface().extendsLoggerInterface()) {
            getDefinedClass()._extends(loggers().delegatingLogger());
            constructorBody.add(JExpr.invoke("super").arg(constructorParam));
            logger = JExpr._super().ref("log");
        } else {
            JFieldVar logVar = getDefinedClass().field(JMod.PROTECTED | JMod.FINAL, loggers().loggerClass(), LOG_FIELD_NAME);
            constructorBody.assign(JExpr._this().ref(logVar), constructorParam);
            logger = logVar;
        }

        // Process the method descriptors and add to the model before writing.
        final Set<MessageMethod> messageMethods = new HashSet<MessageMethod>();
        messageMethods.addAll(messageInterface().methods());
        for (MessageInterface messageInterface : messageInterface().extendedInterfaces()) {
            // Handle logger interface
            if (messageInterface.isLoggerInterface()) {
                continue;
            }
            messageMethods.addAll(messageInterface.methods());
        }
        for (MessageMethod messageMethod : messageMethods) {
            final JClass returnType = codeModel.ref(messageMethod.returnType().name());
            // Create the messageMethod
            final JMethod jMethod = getDefinedClass().method(JMod.PUBLIC | JMod.FINAL, returnType, messageMethod.name());
            jMethod.annotate(Override.class);
            // Add the message messageMethod.
            final JMethod msgMethod = addMessageMethod(messageMethod);

            // Create the messageMethod body
            if (messageMethod.isLoggerMethod()) {
                createLoggerMethod(messageMethod, jMethod, msgMethod, projectCodeVar, logger);
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
     * @param logger         the logger to use.
     */
    private void createLoggerMethod(final MessageMethod messageMethod, final JMethod method, final JMethod msgMethod, final JVar projectCodeVar, final JExpression logger) {
        addThrownTypes(messageMethod, method);
        // Create the body of the method and add the text
        final JBlock body = method.body();

        // Determine which logger method to invoke
        final JInvocation logInv = body.invoke(logger, messageMethod.loggerMethod());
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
        for (Parameter param : messageMethod.allParameters()) {
            final JClass paramType = getCodeModel().ref(param.type());
            final JVar var = method.param(JMod.FINAL, paramType, param.name());
            final String formatterClass = param.formatterClass();
            switch (param.parameterType()) {
                case FORMAT:
                    if (formatterClass == null) {
                        logInv.arg(var);
                    } else {
                        logInv.arg(JExpr._new(getCodeModel().ref(formatterClass)).arg(var));
                    }
                    break;
            }
        }
    }
}
