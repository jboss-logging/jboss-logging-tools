/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.logging.processor.generator.model;

import static org.jboss.jdeparser.JExpr.NULL;
import static org.jboss.jdeparser.JExpr.THIS;
import static org.jboss.jdeparser.JExprs.$v;
import static org.jboss.jdeparser.JTypes.$t;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.processing.ProcessingEnvironment;

import org.jboss.jdeparser.JAssignableExpr;
import org.jboss.jdeparser.JBlock;
import org.jboss.jdeparser.JBlock.Braces;
import org.jboss.jdeparser.JCall;
import org.jboss.jdeparser.JClassDef;
import org.jboss.jdeparser.JExpr;
import org.jboss.jdeparser.JExprs;
import org.jboss.jdeparser.JMethodDef;
import org.jboss.jdeparser.JMod;
import org.jboss.jdeparser.JParamDeclaration;
import org.jboss.jdeparser.JType;
import org.jboss.jdeparser.JVarDeclaration;
import org.jboss.logging.DelegatingBasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.LoggingClass;
import org.jboss.logging.annotations.Message.Format;
import org.jboss.logging.annotations.MessageBundle;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.logging.annotations.Once;
import org.jboss.logging.annotations.Pos;
import org.jboss.logging.annotations.Transform;
import org.jboss.logging.processor.model.MessageInterface;
import org.jboss.logging.processor.model.MessageMethod;
import org.jboss.logging.processor.model.Parameter;

/**
 * Used to generate a message logger implementation.
 * <p>
 * Creates an implementation of the interface passed in.
 * </p>
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 * @author <a href="mailto:david.lloyd@redhat.com">David M. Lloyd</a>
 */
final class MessageLoggerImplementor extends ImplementationClassModel {

    private static final String LOG_FIELD_NAME = "log";
    private static final String FQCN_FIELD_NAME = "FQCN";

    private final Map<String, JVarDeclaration> logOnceVars = new HashMap<>();

    /**
     * Creates a new message logger code model.
     *
     * @param processingEnv    the processing environment
     * @param messageInterface the message interface to implement
     */
    public MessageLoggerImplementor(final ProcessingEnvironment processingEnv, final MessageInterface messageInterface) {
        super(processingEnv, messageInterface);
    }

    @Override
    protected JClassDef generateModel() throws IllegalStateException {
        final JClassDef classDef = super.generateModel();

        // Add FQCN
        if (messageInterface().loggingFQCN() == null) {
            classDef.field(JMod.PRIVATE | JMod.FINAL | JMod.STATIC, String.class, FQCN_FIELD_NAME, $t(classDef)._class().call("getName"));
        } else {
            classDef.field(JMod.PRIVATE | JMod.FINAL | JMod.STATIC, String.class, FQCN_FIELD_NAME, $t(messageInterface().loggingFQCN())._class().call("getName"));
        }

        // Add default constructor
        final JMethodDef constructor = classDef.constructor(JMod.PUBLIC);
        final JType loggerType = $t(Logger.class);
        // Import the logger type and the level type
        sourceFile._import(loggerType);
        final JParamDeclaration constructorParam = constructor.param(JMod.FINAL, loggerType, LOG_FIELD_NAME);
        final JBlock constructorBody = constructor.body();
        final JAssignableExpr logger;
        if (messageInterface().extendsLoggerInterface()) {
            sourceFile._import(DelegatingBasicLogger.class);
            classDef._extends(DelegatingBasicLogger.class);
            constructorBody.callSuper().arg($v(constructorParam));
            logger = $v("super").field("log");
        } else {
            JVarDeclaration logVar = classDef.field(JMod.PROTECTED | JMod.FINAL, loggerType, LOG_FIELD_NAME);
            constructorBody.assign(THIS.field(logVar.name()), $v(constructorParam));
            logger = $v(logVar);
        }
        final JCall localeGetter = createLocaleGetter(null, false);

        // Process the method descriptors and add to the model before writing.
        final Set<MessageMethod> messageMethods = new LinkedHashSet<>();
        messageMethods.addAll(messageInterface().methods());
        for (MessageInterface messageInterface : messageInterface().extendedInterfaces()) {
            if (messageInterface.isAnnotatedWith(MessageBundle.class) || messageInterface.isAnnotatedWith(MessageLogger.class)) {
                messageMethods.addAll(messageInterface.methods());
            }
        }
        for (MessageMethod messageMethod : messageMethods) {

            // Create the messageMethod body
            if (messageMethod.isLoggerMethod()) {
                createLoggerMethod(messageMethod, classDef, logger);
            } else {
                createBundleMethod(classDef, localeGetter, messageMethod);
            }
        }
        return classDef;
    }

    /**
     * Create the logger method body.
     *
     * @param messageMethod the message method.
     * @param classDef      the class definition used to create the method on
     * @param logger        the logger to use.
     */
    private void createLoggerMethod(final MessageMethod messageMethod, final JClassDef classDef, final JAssignableExpr logger) {
        final String msgMethodName = messageMethod.messageMethodName();
        final JMethodDef method = classDef.method(JMod.PUBLIC | JMod.FINAL, messageMethod.returnType().name(), messageMethod.name());
        method.annotate(Override.class);
        addMessageMethod(messageMethod);
        addThrownTypes(messageMethod, method);
        // Initialize the method parameters
        final Map<Parameter, JParamDeclaration> params = createParameters(messageMethod, method);

        // First load the parameter names
        final List<String> parameterNames = new ArrayList<>(params.size());
        for (Parameter param : params.keySet()) {
            parameterNames.add(param.name());
        }

        // Check for the @Once annotation
        final JBlock body;
        if (messageMethod.isAnnotatedWith(Once.class) && messageMethod.isLoggerMethod()) {
            final JType atomicBoolean = $t(AtomicBoolean.class);
            sourceFile._import(atomicBoolean);
            // The variable will be shared with overloaded methods
            final String varName = messageMethod.name() + "_$Once";
            final JVarDeclaration var;
            if (logOnceVars.containsKey(varName)) {
                var = logOnceVars.get(varName);
            } else {
                var = classDef.field(JMod.PRIVATE | JMod.STATIC | JMod.FINAL, atomicBoolean, varName, atomicBoolean._new().arg(JExpr.FALSE));
                logOnceVars.put(varName, var);
            }
            body = method.body()._if(
                    logger.call("isEnabled").arg($v(messageMethod.logLevel())).and(
                            $v(var).call("compareAndSet").arg(JExpr.FALSE).arg(JExpr.TRUE)))
                    .block(Braces.REQUIRED);
        } else if (!messageMethod.parametersAnnotatedWith(Transform.class).isEmpty()) {
            body = method.body()._if(logger.call("isEnabled").arg($v(messageMethod.logLevel()))).block(Braces.REQUIRED);
        } else {
            body = method.body();
        }

        // Determine which logger method to invoke
        final JCall logCaller = logger.call(messageMethod.loggerMethod());
        final Set<Parameter> fqcnParameters = messageMethod.parametersAnnotatedWith(LoggingClass.class);
        if (fqcnParameters.isEmpty()) {
            logCaller.arg($v(FQCN_FIELD_NAME));
        } else {
            logCaller.arg($v(params.get(fqcnParameters.iterator().next())).call("getName"));
        }
        // Use static imports for the levels
        final String levelName = messageMethod.logLevel();
        sourceFile.importStatic(Logger.Level.class, levelName);
        logCaller.arg($v(levelName));


        final MessageMethod.Message message = messageMethod.message();
        // No format log messages need the message before the cause
        if (message.format() == Format.NO_FORMAT) {
            logCaller.arg(JExprs.call(msgMethodName));
            // Next for no format should always be null
            logCaller.arg(NULL);

            // The cause is the final argument
            if (messageMethod.hasCause()) {
                logCaller.arg($v(messageMethod.cause().name()));
            } else {
                logCaller.arg(NULL);
            }
        } else {
            if (messageMethod.hasCause()) {
                logCaller.arg($v(messageMethod.cause().name()));
            } else {
                logCaller.arg(NULL);
            }
            // The next parameter is the message. Should be accessed via the
            // message retrieval method.
            logCaller.arg(JExprs.call(msgMethodName));
            final List<JExpr> args = new ArrayList<>();
            // Create the parameters
            for (Map.Entry<Parameter, JParamDeclaration> entry : params.entrySet()) {
                final Parameter param = entry.getKey();
                final String formatterClass = param.formatterClass();
                final JParamDeclaration var = entry.getValue();

                boolean added = false;
                if (param.isFormatParameter()) {
                    if (param.isAnnotatedWith(Transform.class)) {
                        final JAssignableExpr transformVar = createTransformVar(parameterNames, body, param, $v(var));
                        if (formatterClass == null) {
                            args.add(transformVar);
                        } else {
                            args.add($t(formatterClass)._new().arg(transformVar));
                        }
                        added = true;
                    }
                    if (param.isAnnotatedWith(Pos.class)) {
                        final Pos pos = param.getAnnotation(Pos.class);
                        final int[] positions = pos.value();
                        final Transform[] transform = pos.transform();
                        for (int i = 0; i < positions.length; i++) {
                            final int index = positions[i] - 1;
                            if (transform != null && transform.length > 0) {
                                final JAssignableExpr tVar = createTransformVar(parameterNames, body, param, transform[i], $v(var));
                                if (index < args.size()) {
                                    args.add(index, tVar);
                                } else {
                                    args.add(tVar);
                                }
                            } else {
                                if (index < args.size()) {
                                    args.add(index, $v(var));
                                } else {
                                    args.add($v(var));
                                }
                            }
                        }
                        added = true;
                    }

                    if (!added) {
                        if (formatterClass == null) {
                            if (param.isArray() || param.isVarArgs()) {
                                args.add($t(Arrays.class).call("toString").arg($v(var)));
                            } else {
                                args.add($v(var));
                            }
                        } else {
                            args.add($t(formatterClass)._new().arg($v(var)));
                        }
                    }
                }
            }
            for (JExpr arg : args) {
                logCaller.arg(arg);
            }
        }
        body.add(logCaller);
    }

    private Map<Parameter, JParamDeclaration> createParameters(final MessageMethod messageMethod, final JMethodDef method) {
        final Map<Parameter, JParamDeclaration> result = new LinkedHashMap<>();
        // Create the parameters
        for (Parameter param : messageMethod.parameters()) {
            final JParamDeclaration var = addMethodParameter(method, param);
            result.put(param, var);
        }
        return result;
    }
}
