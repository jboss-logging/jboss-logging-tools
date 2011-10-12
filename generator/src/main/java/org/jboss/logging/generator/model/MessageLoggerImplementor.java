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
import com.sun.codemodel.internal.JType;
import com.sun.codemodel.internal.JVar;
import org.jboss.logging.generator.intf.model.MessageInterface;
import org.jboss.logging.generator.intf.model.Method;
import org.jboss.logging.generator.intf.model.Parameter;
import org.jboss.logging.generator.intf.model.ReturnType;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
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
    private JFieldVar log;

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
        log = getDefinedClass().field(JMod.PROTECTED | JMod.FINAL, loggers().loggerClass(), LOG_FIELD_NAME);
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
        constructorBody.assign(JExpr._this().ref(log), constructorParam);

        // Process the method descriptors and add to the model before writing.
        final Set<Method> methods = new HashSet<Method>();
        methods.addAll(messageInterface().methods());
        for (MessageInterface messageInterface : messageInterface().extendedInterfaces()) {
            // Handle basic logger
            if (messageInterface.isBasicLogger()) {
                implementBasicLogger(codeModel, messageInterface);
                continue;
            }
            methods.addAll(messageInterface.methods());
        }
        for (Method method : methods) {
            final JClass returnType = codeModel.ref(method.returnType().name());
            // Create the method
            final JMethod jMethod = getDefinedClass().method(JMod.PUBLIC | JMod.FINAL, returnType, method.name());
            jMethod.annotate(Override.class);
            // Add the message method.
            final JMethod msgMethod = addMessageMethod(method);

            // Create the method body
            if (method.isLoggerMethod()) {
                createLoggerMethod(method, jMethod, msgMethod, projectCodeVar);
            } else {
                createBundleMethod(method, jMethod, msgMethod, projectCodeVar);
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
    private void createLoggerMethod(final Method messageMethod, final JMethod method, final JMethod msgMethod, final JVar projectCodeVar) {
        addThrownTypes(messageMethod, method);
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
        final Method.Message message = messageMethod.message();
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
            final String formatterClass = param.getFormatterClass();
            if (param.isFormatParam()) {
                if (formatterClass == null) {
                    logInv.arg(var);
                } else {
                    logInv.arg(JExpr._new(getCodeModel().ref(formatterClass)).arg(var));
                }
            }
        }
    }

    /**
     * Implements the basic logger methods.
     *
     * @param codeModel        the code model to implement to.
     * @param messageInterface the message interface to implement.
     */
    private void implementBasicLogger(final JCodeModel codeModel, final MessageInterface messageInterface) {
        for (Method method : messageInterface.methods()) {
            final JType returnType;
            if (method.returnType().equals(ReturnType.VOID)) {
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
            // Create the method body
            final JBlock body = blMethod.body();
            // Create the delegate method
            final DelegateLogMethod delegateLogMethod = DelegateLogMethod.of(method);
            // Create the invocation for the delegate
            final JInvocation logInv = JExpr.invoke(log, delegateLogMethod.methodName);
            final Map<Parameter, JExpression> args = new LinkedHashMap<Parameter, JExpression>();
            // Create sequenced parameters
            final Map<DelegateParameter, JExpression> delegateParameters = new HashMap<DelegateParameter, JExpression>();
            delegateParameters.put(DelegateParameter.FQCN, JExpr.ref(FQCN_FIELD_NAME));
            if (delegateLogMethod.level != null) {
                delegateParameters.put(DelegateParameter.LEVEL, JExpr.direct(delegateLogMethod.level));
            }
            boolean first = true;
            for (Parameter parameter : method.allParameters()) {
                final JType param = codeModel.ref(parameter.type());
                // Assume if first parameter is a String and it's not a format method, it's th FQCN
                if (first) {
                    first = false;
                    if (!delegateLogMethod.isFormatMethod && parameter.isSameAs(String.class)) {
                        delegateParameters.put(DelegateParameter.FQCN, blMethod.param(JMod.FINAL, param, parameter.name()));
                        continue;
                    }
                }
                // Check the parameter types
                if (parameter.isSameAs(loggers().logLevelClass())) {
                    delegateParameters.put(DelegateParameter.LEVEL, blMethod.param(JMod.FINAL, param, parameter.name()));
                } else if (parameter.isAssignableFrom(Throwable.class)) {
                    delegateParameters.put(DelegateParameter.THROWABLE, blMethod.param(JMod.FINAL, param, parameter.name()));
                } else if (parameter.isVarArgs()) {
                    args.put(parameter, blMethod.varParam(param, parameter.name()));
                } else if (parameter.isArray()) {
                    args.put(parameter, blMethod.param(JMod.FINAL, param.array(), parameter.name()));
                } else {
                    args.put(parameter, blMethod.param(JMod.FINAL, param, parameter.name()));
                }
            }
            // Void return types should be logging methods.
            if (ReturnType.VOID.equals(method.returnType())) {
                if (!delegateParameters.isEmpty()) {
                    logInv.arg(delegateParameters.get(DelegateParameter.FQCN));
                    if (delegateParameters.containsKey(DelegateParameter.LEVEL)) {
                        logInv.arg(delegateParameters.get(DelegateParameter.LEVEL));
                    }
                    if (delegateParameters.containsKey(DelegateParameter.THROWABLE)) {
                        logInv.arg(delegateParameters.get(DelegateParameter.THROWABLE));
                    } else {
                        logInv.arg(JExpr._null());
                    }
                    first = true;
                    for (Map.Entry<Parameter, JExpression> entry : args.entrySet()) {
                        final Parameter parameter = entry.getKey();
                        final JExpression methodParameter = entry.getValue();
                        // Kind of hacky, but if the parameter is an object we need to get it's string value or pass null
                        if (first && parameter.isSameAs(Object.class)) {
                            first = false;
                            final StringBuilder sb = new StringBuilder();
                            sb.append(parameter.name()).
                                    append(" == null ? null : ").
                                    append(parameter.name())
                                    .append(".toString()");
                            logInv.arg(JExpr.direct(sb.toString()));
                        } else {
                            logInv.arg(methodParameter);
                        }
                    }
                }
                // Add the delegate invocation to the body
                body.add(logInv);
            } else {
                // Should be boolean and only, optionally, have a level parameter
                if (delegateParameters.containsKey(DelegateParameter.LEVEL)) {
                    logInv.arg(delegateParameters.get(DelegateParameter.LEVEL));
                }
                body._return(logInv);
            }
        }
    }

    /**
     * Simple enum in the sequence order
     */
    private static enum DelegateParameter {
        FQCN,
        LEVEL,
        THROWABLE
    }

    /**
     * Simple holder and parser for which method the log should delegate to.
     */
    private static class DelegateLogMethod {
        final String level;
        final String methodName;
        final boolean isFormatMethod;

        private DelegateLogMethod(final Method method) {
            final String methodName = method.name();
            final char lastChar = methodName.charAt(methodName.length() - 1);
            isFormatMethod = (lastChar == 'f' || lastChar == 'v');
            final String logMethod;
            if (methodName.startsWith("log")) {
                if (isFormatMethod) {
                    logMethod = methodName;
                } else {
                    logMethod = methodName + "v";
                }
                level = null;
            } else if (ReturnType.VOID.equals(method.returnType())) {
                if (isFormatMethod) {
                    logMethod = "log" + lastChar;
                    level = "Logger.Level." + methodName.substring(0, methodName.length() - 1).toUpperCase(Locale.US);
                } else {
                    logMethod = "logv";
                    level = "Logger.Level." + methodName.substring(0, methodName.length()).toUpperCase(Locale.US);
                }
            } else {
                logMethod = methodName;
                level = null;
            }
            this.methodName = logMethod;
        }

        public static DelegateLogMethod of(final Method method) {
            return new DelegateLogMethod(method);
        }

    }
}
