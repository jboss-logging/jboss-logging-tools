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

import java.util.ArrayList;
import java.util.List;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;

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
public class CodeModelFactory {

    static final class MessageLoggerCodeModel extends CodeModel {
        private JFieldVar log;

        private MessageLoggerCodeModel(final String interfaceName)
                throws JClassAlreadyExistsException {
            super(interfaceName);
            init();
        }

        @Override
        public Implementation type() {
            return Implementation.LOGGER;
        }

        @Override
        public void addMethod(final ExecutableElement method) {
            final String methodName = method.getSimpleName().toString();
            // Create the method
            final JMethod jMethod = definedClass().method(
                    JMod.PUBLIC | JMod.FINAL, codeModel().VOID, methodName);
            final Message message = method.getAnnotation(Message.class);
            final LogMessage logMessage = method
                    .getAnnotation(LogMessage.class);
            final List<JVar> parameters = new ArrayList<JVar>();
            Message.Format format = Message.Format.PRINTF;
            Logger.Level logLevel = Logger.Level.INFO;
            String msg = "";
            if (logMessage != null) {
                logLevel = logMessage.level();
            }
            if (message != null) {
                format = message.format();
                final int id = message.id();
                msg = message.value();
            }
            final StringBuilder bodyText = new StringBuilder();

            final JMethod msgMethod = addMessageMethod(methodName, msg);

            bodyText.append(log.name());
            bodyText.append(".");
            bodyText.append(logLevel.name().toLowerCase());

            switch (format) {
                case MESSAGE_FORMAT:
                    bodyText.append("v");
                    break;
                case PRINTF:
                    bodyText.append("f");
                    break;
            }
            bodyText.append("(");
            // Create the parameters
            for (VariableElement param : method.getParameters()) {
                final JClass paramType = codeModel().ref(
                        param.asType().toString());
                final JVar var = jMethod.param(JMod.FINAL, paramType, param
                        .getSimpleName().toString());
                if (param.getAnnotation(Cause.class) != null) {
                    bodyText.append(var.name());
                    bodyText.append(", ");
                } else {
                    parameters.add(var);
                }
            }
            bodyText.append(msgMethod.name());
            bodyText.append("()");
            for (JVar var : parameters) {
                bodyText.append(", ");
                bodyText.append(var.name());
            }
            bodyText.append(");");
            // Create the model
            final JBlock body = jMethod.body();
            body.directStatement(bodyText.toString());
        }

        private void init() {
            log = definedClass().field(JMod.PROTECTED | JMod.FINAL,
                    Logger.class, "log");
            // Add default constructor
            final JMethod constructor = definedClass().constructor(
                    JMod.PROTECTED);
            constructor.param(JMod.FINAL, Logger.class, "log");
            final JBlock body = constructor.body();
            body.directStatement("this.log = log;");
        }
    }

    static final class MessageBundleCodeModel extends CodeModel {
        private MessageBundleCodeModel(final String interfaceName)
                throws JClassAlreadyExistsException {
            super(interfaceName);
            init();
        }

        @Override
        public Implementation type() {
            return Implementation.BUNDLE;
        }

        @Override
        public void addMethod(final ExecutableElement method) {
            final String methodName = method.getSimpleName().toString();
            final JClass returnType = codeModel().ref(
                    method.getReturnType().toString());
            final JMethod jMethod = definedClass().method(
                    JMod.PUBLIC | JMod.FINAL, returnType, methodName);
            final Message message = method.getAnnotation(Message.class);
            final LogMessage logMessage = method
                    .getAnnotation(LogMessage.class);
            final List<JVar> parameters = new ArrayList<JVar>();
            final Message.Format format = message.format();
            Logger.Level logLevel = Logger.Level.INFO;
            final String msg = message.value();

            final JMethod msgMethod = addMessageMethod(methodName, msg);

            if (logMessage != null) {
                logLevel = logMessage.level();
            }
            // Create the parameters
            for (VariableElement param : method.getParameters()) {
                final JClass paramType = codeModel().ref(
                        param.asType().toString());
                final JVar var = jMethod.param(JMod.FINAL, paramType, param
                        .getSimpleName().toString());
                if (param.getAnnotation(Cause.class) != null) {
                    // TODO - Handle the cause element
                } else {
                    parameters.add(var);
                }
            }

            final JBlock body = jMethod.body();
            for (VariableElement param : method.getParameters()) {
                final JClass paramType = codeModel().ref(
                        param.asType().toString());
                jMethod.param(JMod.FINAL, paramType, param.getSimpleName()
                        .toString());

            }
            final JClass returnField = codeModel().ref(returnType.fullName());
            JVar var = body.decl(returnField, "result");
            var.init(JExpr.invoke(msgMethod));
            body._return(var);
        }

        private void init() {
            final JFieldVar instance = definedClass().field(
                    JMod.PUBLIC | JMod.STATIC | JMod.FINAL, definedClass(),
                    "INSTANCE");
            instance.init(JExpr._new(definedClass()));
            // Add default constructor
            definedClass().constructor(JMod.PROTECTED);
            final JMethod readResolveMethod = definedClass().method(
                    JMod.PROTECTED, definedClass(), "readResolve");
            readResolveMethod.body()._return(instance);
        }
    }

    /**
     * Class constructor for singleton.
     */
    private CodeModelFactory() {

    }

    public static final CodeModel createMessageLogger(final String interfaceName)
            throws JClassAlreadyExistsException {
        return new MessageLoggerCodeModel(interfaceName);
    }

    public static final CodeModel createMessageBundle(final String interfaceName)
            throws JClassAlreadyExistsException {
        return new MessageBundleCodeModel(interfaceName);
    }
}
