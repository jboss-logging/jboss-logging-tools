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
        private final Generator generator;
        private JFieldVar log;

        MessageLoggerCodeModel(final Generator generator,
                final String interfaceName) throws JClassAlreadyExistsException {
            super(interfaceName);
            this.generator = generator;
            init();
        }

        @Override
        public Implementation type() {
            return Implementation.LOGGER;
        }

        @Override
        public void addMethod(final ExecutableElement method) {
            // Create the method
            final JMethod jMethod = definedClass().method(
                    JMod.PUBLIC | JMod.FINAL, codeModel().VOID,
                    method.getSimpleName().toString());
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
            StringBuilder sb = new StringBuilder();
            if (message != null) {
                format = message.format();
                final int id = message.id();
                msg = message.value();
            }
            sb.append(log.name());
            sb.append(".");
            sb.append(logLevel.name().toLowerCase());

            switch (format) {
                case MESSAGE_FORMAT:
                    sb.append("v");
                    break;
                case PRINTF:
                    sb.append("f");
                    break;
            }
            sb.append("(");
            // Create the parameters
            for (VariableElement param : method.getParameters()) {
                final JClass paramType = codeModel().ref(
                        param.asType().toString());
                final JVar var = jMethod.param(JMod.FINAL, paramType, param
                        .getSimpleName().toString());
                if (param.getAnnotation(Cause.class) != null) {
                    sb.append(var.name());
                    sb.append(", ");
                } else {
                    parameters.add(var);
                }
            }
            sb.append("\"");
            sb.append(msg);
            sb.append("\"");
            for (JVar var : parameters) {
                sb.append(", ");
                sb.append(var.name());
            }
            sb.append(");");
            // Create the model
            final JBlock body = jMethod.body();
            body.directStatement(sb.toString());
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
        MessageBundleCodeModel(final String interfaceName)
                throws JClassAlreadyExistsException {
            super(interfaceName);
        }

        @Override
        public Implementation type() {
            return Implementation.BUNDLE;
        }

        @Override
        public void addMethod(final ExecutableElement method) {
            final JClass returnType = codeModel().ref(
                    method.getReturnType().toString());
            final JMethod jMethod = definedClass().method(
                    JMod.PUBLIC | JMod.FINAL, returnType,
                    method.getSimpleName().toString());
            final JBlock body = jMethod.body();
            for (VariableElement param : method.getParameters()) {
                final JClass paramType = codeModel().ref(
                        param.asType().toString());
                jMethod.param(JMod.FINAL, paramType, param.getSimpleName()
                        .toString());

            }
            if (returnType.isPrimitive() || returnType.isReference()) {
                if (!method.getReturnType().getKind().equals(TypeKind.VOID)) {
                    final JClass returnField = codeModel().ref(
                            returnType.fullName());
                    JVar var = body.decl(returnField, "result");
                    var.init(JExpr._null());
                    body._return(var);
                }
            }

        }
    }

    /**
     * Class constructor for singleton.
     */
    private CodeModelFactory() {

    }

    public static final CodeModel createMessageLogger(
            final Generator generator, final String interfaceName)
            throws JClassAlreadyExistsException {
        return new MessageLoggerCodeModel(generator, interfaceName);
    }

    public static final CodeModel createMessageBundle(final String interfaceName)
            throws JClassAlreadyExistsException {
        return new MessageBundleCodeModel(interfaceName);
    }
}
