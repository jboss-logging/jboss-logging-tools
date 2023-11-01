/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2023 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.annotation.processing.RoundEnvironment;

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
import org.jboss.jdeparser.JTry;
import org.jboss.jdeparser.JType;
import org.jboss.jdeparser.JVarDeclaration;
import org.jboss.logging.DelegatingBasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.LoggingClass;
import org.jboss.logging.annotations.Message.Format;
import org.jboss.logging.annotations.MessageBundle;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.logging.annotations.Once;
import org.jboss.logging.annotations.Pos;
import org.jboss.logging.annotations.Transform;
import org.jboss.logging.processor.model.LoggerMessageMethod;
import org.jboss.logging.processor.model.MessageInterface;
import org.jboss.logging.processor.model.MessageMethod;
import org.jboss.logging.processor.model.Parameter;
import org.jboss.logging.processor.util.ElementHelper;

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
    public MessageLoggerImplementor(final ProcessingEnvironment processingEnv, final RoundEnvironment roundEnv, final MessageInterface messageInterface) {
        super(processingEnv, roundEnv, messageInterface);
    }

    @Override
    protected JClassDef generateModel() throws IllegalStateException {
        final JClassDef classDef = super.generateModel();

        // Add FQCN
        if (messageInterface().loggingFQCN() == null) {
            classDef.field(JMod.PRIVATE | JMod.FINAL | JMod.STATIC, String.class, FQCN_FIELD_NAME,
                    $t(classDef)._class().call("getName"));
        } else {
            classDef.field(JMod.PRIVATE | JMod.FINAL | JMod.STATIC, String.class, FQCN_FIELD_NAME,
                    $t(messageInterface().loggingFQCN())._class().call("getName"));
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
            if (messageInterface.isAnnotatedWith(MessageBundle.class)
                    || messageInterface.isAnnotatedWith(MessageLogger.class)) {
                messageMethods.addAll(messageInterface.methods());
            }
        }
        for (MessageMethod messageMethod : messageMethods) {

            // Create the messageMethod body
            if (messageMethod instanceof LoggerMessageMethod) {
                createLoggerMethod((LoggerMessageMethod) messageMethod, classDef, logger);
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
    private void createLoggerMethod(final LoggerMessageMethod messageMethod, final JClassDef classDef,
            final JAssignableExpr logger) {
        final String msgMethodName = messageMethod.messageMethodName();
        final JMethodDef method = classDef.method(JMod.PUBLIC | JMod.FINAL, messageMethod.returnType().name(),
                messageMethod.name());
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

        final LogMessage logMessage = messageMethod.getAnnotation(LogMessage.class);

        final JBlock baseBody;
        if (logMessage.useThreadContext()) {
            baseBody = wrapTccl(method.body(), getUniqueName(parameterNames, "currentTccl", 0));
        } else {
            baseBody = method.body();
        }

        // Check for the @Once annotation
        final JBlock body;
        if (messageMethod.isAnnotatedWith(Once.class)) {
            final JType atomicBoolean = $t(AtomicBoolean.class);
            sourceFile._import(atomicBoolean);
            // The variable will be shared with overloaded methods
            final String varName = messageMethod.name() + "_$Once";
            final JVarDeclaration var;
            if (logOnceVars.containsKey(varName)) {
                var = logOnceVars.get(varName);
            } else {
                var = classDef.field(JMod.PRIVATE | JMod.STATIC | JMod.FINAL, atomicBoolean, varName,
                        atomicBoolean._new().arg(JExpr.FALSE));
                logOnceVars.put(varName, var);
            }
            body = baseBody._if(
                    logger.call("isEnabled").arg($v(messageMethod.logLevel())).and(
                            $v(var).call("compareAndSet").arg(JExpr.FALSE).arg(JExpr.TRUE)))
                    .block(Braces.REQUIRED);
        } else if (messageMethod.wrapInEnabledCheck()) {
            body = baseBody._if(logger.call("isEnabled").arg($v(messageMethod.logLevel()))).block(Braces.REQUIRED);
        } else {
            body = baseBody;
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
                            if (transform.length > 0) {
                                final JAssignableExpr tVar = createTransformVar(parameterNames, body, param, transform[i],
                                        $v(var));
                                if (index < args.size()) {
                                    args.add(index, tVar);
                                } else {
                                    args.add(tVar);
                                }
                            } else {
                                final JExpr resolvedVar;
                                if (param.isSubtypeOf(Supplier.class)) {
                                    resolvedVar = $v(var).call("get");
                                } else {
                                    resolvedVar = $v(var);
                                }
                                if (index < args.size()) {
                                    args.add(index, resolvedVar);
                                } else {
                                    args.add(resolvedVar);
                                }
                            }
                        }
                        added = true;
                    }

                    if (!added) {
                        if (formatterClass == null) {
                            final JExpr resolvedVar;
                            if (param.isSubtypeOf(Supplier.class)) {
                                // Handle the supplier type, if it's an array we need to invoke Arrays.toString(supplier.get())
                                final Optional<TypeMirror> typeArg = ElementHelper.getTypeArgument(param);
                                if (typeArg.isPresent() && typeArg.get().getKind() == TypeKind.ARRAY) {
                                    sourceFile._import(Arrays.class);
                                    resolvedVar = $t(Arrays.class).call("toString").arg($v(var).call("get"));
                                } else {
                                    // Get the value from the supplier
                                    resolvedVar = $v(var).call("get");
                                }
                            } else {
                                // We are not a supplier, so just wrap the var
                                resolvedVar = $v(var);
                            }
                            if (param.isArray() || param.isVarArgs()) {
                                sourceFile._import(Arrays.class);
                                args.add($t(Arrays.class).call("toString").arg($v(var)));
                            } else {
                                args.add(resolvedVar);
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

    private JBlock wrapTccl(final JBlock body, final String currentName) {
        final JExpr classExpression = THIS.call("getClass");
        final JType thread = $t(Thread.class);

        // Capture the current TCCL
        final JAssignableExpr currentTccl = $v(body.var(JMod.FINAL, $t(ClassLoader.class), currentName,
                JExprs.callStatic(thread, "currentThread").call("getContextClassLoader")));

        final JTry tryBlock = body._try();
        // Set the new TCCL then log the message
        tryBlock.add(JExprs.callStatic(thread, "currentThread").call("setContextClassLoader")
                .arg(classExpression.call("getClassLoader")));

        final JBlock finallyBlock = tryBlock._finally();
        // Reset the TCCL
        finallyBlock.add(JExprs.callStatic(thread, "currentThread").call("setContextClassLoader").arg(currentTccl));

        return tryBlock;
    }
}
