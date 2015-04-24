/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
import static org.jboss.logging.processor.model.Parameter.ParameterType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.processing.Filer;

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
import org.jboss.logging.annotations.Message.Format;
import org.jboss.logging.annotations.Once;
import org.jboss.logging.annotations.Pos;
import org.jboss.logging.annotations.Transform;
import org.jboss.logging.processor.model.MessageInterface;
import org.jboss.logging.processor.model.MessageInterface.AnnotatedType;
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

    private final boolean useLogging31;
    private final Map<String, JVarDeclaration> logOnceVars = new HashMap<>();

    /**
     * Creates a new message logger code model.
     *
     * @param filer            the filer used to create the source file
     * @param messageInterface the message interface to implement.
     * @param useLogging31     {@code true} to use logging 3.1, {@code false} to remain compatible with 3.0
     */
    public MessageLoggerImplementor(final Filer filer, final MessageInterface messageInterface, final boolean useLogging31) {
        super(filer, messageInterface);
        this.useLogging31 = useLogging31;
    }

    /**
     * Determine whether to use JBoss Logging 3.1 constructs.  Defaults to {@code true}.
     *
     * @return {@code true} to use JBoss Logging 3.1 constructs, {@code false} to remain compatible with 3.0
     */
    public boolean isUseLogging31() {
        return useLogging31;
    }

    @Override
    protected JClassDef generateModel() throws IllegalStateException {
        final JClassDef classDef = super.generateModel();

        // Add FQCN
        final JVarDeclaration fqcn;
        if (messageInterface().loggingFQCN() == null) {
            fqcn = classDef.field(JMod.PRIVATE | JMod.FINAL | JMod.STATIC, String.class, FQCN_FIELD_NAME, $t(classDef)._class().call("getName"));
        } else {
            fqcn = classDef.field(JMod.PRIVATE | JMod.FINAL | JMod.STATIC, String.class, FQCN_FIELD_NAME, $t(messageInterface().loggingFQCN())._class().call("getName"));
        }

        // Add default constructor
        final JMethodDef constructor = classDef.constructor(JMod.PUBLIC);
        final JParamDeclaration constructorParam = constructor.param(JMod.FINAL, Logger.class, LOG_FIELD_NAME);
        final JBlock constructorBody = constructor.body();
        final JAssignableExpr logger;
        if (messageInterface().extendsLoggerInterface()) {
            if (useLogging31) {
                classDef._extends(DelegatingBasicLogger.class);
                constructorBody.callSuper().arg($v(constructorParam));
                logger = $v(constructorParam);
            } else {
                JVarDeclaration logVar = classDef.field(JMod.PROTECTED | JMod.FINAL, Logger.class, LOG_FIELD_NAME);
                constructorBody.assign(THIS.field(logVar.name()), $v(constructorParam));
                logger = $v(logVar);
                generateDelegatingLoggerMethods(classDef, logger, fqcn);
            }
        } else {
            JVarDeclaration logVar = classDef.field(JMod.PROTECTED | JMod.FINAL, Logger.class, LOG_FIELD_NAME);
            constructorBody.assign(THIS.field(logVar.name()), $v(constructorParam));
            logger = $v(logVar);
        }

        // Process the method descriptors and add to the model before writing.
        final Set<MessageMethod> messageMethods = new LinkedHashSet<MessageMethod>();
        messageMethods.addAll(messageInterface().methods());
        for (MessageInterface messageInterface : messageInterface().extendedInterfaces()) {
            // Handle logger interface
            if (messageInterface.getAnnotatedType() == AnnotatedType.NONE) {
                continue;
            }
            messageMethods.addAll(messageInterface.methods());
        }
        for (MessageMethod messageMethod : messageMethods) {

            // Create the messageMethod body
            if (messageMethod.isLoggerMethod()) {
                createLoggerMethod(messageMethod, classDef, logger);
            } else {
                createBundleMethod(classDef, messageMethod);
            }
        }
        return classDef;
    }

    private void generateDelegatingLoggerMethods(final JClassDef classDef, final JAssignableExpr logVar, JVarDeclaration fqcn) {
        final Class<?> logLevelClass = Logger.Level.class;
        // Generate these methods so they look the same as they appear in DelegatedBasicLogger.
        for (String level : Arrays.asList("TRACE", "DEBUG", "INFO", "WARN", "ERROR", "FATAL")) {
            // string prep
            String firstUppered = level.charAt(0) + level.substring(1).toLowerCase(Locale.US);
            String lowered = level.toLowerCase(Locale.US);

            if ("TRACE".equals(level) || "DEBUG".equals(level) || "INFO".equals(level)) {
                // isXxxEnabled...
                final String isXxxEnabledStr = "is" + firstUppered + "Enabled";
                final JMethodDef isXxxEnabled = classDef.method(JMod.PUBLIC | JMod.FINAL, JType.BOOLEAN, isXxxEnabledStr);
                isXxxEnabled.annotate(Override.class);
                isXxxEnabled.body()._return(logVar.call(isXxxEnabledStr));
            }

            // now, the four "raw" level-specific methods
            final JMethodDef xxx1 = classDef.method(JMod.PUBLIC | JMod.FINAL, JType.VOID, lowered);
            xxx1.annotate(Override.class);
            final JParamDeclaration xxx1message = xxx1.param(Object.class, "message");
            xxx1.body().add(
                    logVar.call(lowered).arg($v(fqcn))
                            .arg($v(xxx1message))
                            .arg(NULL)
            );

            final JMethodDef xxx2 = classDef.method(JMod.PUBLIC | JMod.FINAL, JType.VOID, lowered);
            xxx2.annotate(Override.class);
            final JParamDeclaration xxx2message = xxx2.param(Object.class, "message");
            final JParamDeclaration xxx2t = xxx2.param(Throwable.class, "t");
            xxx2.body().add(
                    logVar.call(lowered).arg($v(fqcn))
                            .arg($v(xxx2message))
                            .arg($v(xxx2t))
            );

            final JMethodDef xxx3 = classDef.method(JMod.PUBLIC | JMod.FINAL, JType.VOID, lowered);
            xxx3.annotate(Override.class);
            final JParamDeclaration xxx3loggerFqcn = xxx3.param(String.class, "loggerFqcn");
            final JParamDeclaration xxx3message = xxx3.param(Object.class, "message");
            final JParamDeclaration xxx3t = xxx3.param(Throwable.class, "t");
            xxx3.body().add(
                    logVar.call(lowered)
                            .arg($v(xxx3loggerFqcn))
                            .arg($v(xxx3message))
                            .arg($v(xxx3t))
            );

            final JMethodDef xxx4 = classDef.method(JMod.PUBLIC | JMod.FINAL, JType.VOID, lowered);
            xxx4.annotate(Override.class);
            final JParamDeclaration xxx4loggerFqcn = xxx4.param(String.class, "loggerFqcn");
            final JParamDeclaration xxx4message = xxx4.param(Object.class, "message");
            final JParamDeclaration xxx4params = xxx4.param($t(Object.class).array(), "params");
            final JParamDeclaration xxx4t = xxx4.param(Throwable.class, "t");
            xxx4.body().add(
                    logVar.call(lowered)
                            .arg($v(xxx4loggerFqcn))
                            .arg($v(xxx4message))
                            .arg($v(xxx4params))
                            .arg($v(xxx4t))
            );

            // 8 methods each for v and f
            for (String affix : Arrays.asList("v", "f")) {
                final String name = lowered + affix;
                final String target = "log" + affix;

                // 4 methods each for with- and without-throwable
                for (boolean renderThr : new boolean[] {false, true}) {
                    JParamDeclaration thr = null;

                    final JMethodDef xxx1x = classDef.method(JMod.PUBLIC | JMod.FINAL, JType.VOID, name);
                    xxx1x.annotate(Override.class);
                    if (renderThr) thr = xxx1x.param(Throwable.class, "t");
                    final JParamDeclaration xxx1xFormat = xxx1x.param(String.class, "format");
                    final JParamDeclaration xxx1xParams = xxx1x.varargParam(Object.class, "params");
                    xxx1x.body().add(
                            logVar.call(target)
                                    .arg($v(fqcn))
                                    .arg($t(logLevelClass).$v(level))
                                    .arg(renderThr ? $v(thr) : NULL)
                                    .arg($v(xxx1xFormat))
                                    .arg($v(xxx1xParams))
                    );

                    // 3 methods for 3 parameter counts
                    for (int i = 1; i <= 3; i++) {
                        final JMethodDef xxx2x = classDef.method(JMod.PUBLIC | JMod.FINAL, JType.VOID, name);
                        xxx2x.annotate(Override.class);
                        if (renderThr) thr = xxx2x.param(Throwable.class, "t");
                        final JParamDeclaration xxx2xFormat = xxx2x.param(String.class, "format");
                        final JParamDeclaration[] params = new JParamDeclaration[i];
                        for (int j = 0; j < i; j++) {
                            params[j] = xxx2x.param(Object.class, "param" + (j + 1));
                        }
                        final JCall xxx2xCaller = logVar.call(target);
                        xxx2xCaller.arg($v(fqcn))
                                .arg($t(logLevelClass).$v(level))
                                .arg(renderThr ? $v(thr) : NULL)
                                .arg($v(xxx2xFormat));
                        for (int j = 0; j < i; j++) {
                            xxx2xCaller.arg($v(params[j]));
                        }
                        xxx2x.body().add(xxx2xCaller);
                    }
                }
            }
        }

        // Now the plain "log" methods which take a level

        // isEnabled...
        final JMethodDef isEnabled = classDef.method(JMod.PUBLIC | JMod.FINAL, JType.BOOLEAN, "isEnabled");
        isEnabled.annotate(Override.class);
        final JParamDeclaration isEnabledLevel = isEnabled.param(logLevelClass, "level");
        isEnabled.body()._return(
                logVar.call("isEnabled")
                        .arg($v(isEnabledLevel))
        );

        // now, the four "raw" log methods
        final JMethodDef log1 = classDef.method(JMod.PUBLIC | JMod.FINAL, JType.VOID, "log");
        log1.annotate(Override.class);
        final JParamDeclaration log1Level = log1.param(logLevelClass, "level");
        final JParamDeclaration log1Message = log1.param(Object.class, "message");
        log1.body().add(
                logVar.call("log")
                        .arg($v(fqcn))
                        .arg($v(log1Level))
                        .arg($v(log1Message))
                        .arg(NULL)
                        .arg(NULL)
        );

        final JMethodDef log2 = classDef.method(JMod.PUBLIC | JMod.FINAL, JType.VOID, "log");
        log2.annotate(Override.class);
        final JParamDeclaration log2Level = log2.param(logLevelClass, "level");
        final JParamDeclaration log2message = log2.param(Object.class, "message");
        final JParamDeclaration log2t = log2.param(Throwable.class, "t");
        log2.body().add(
                logVar.call("log")
                        .arg($v(fqcn))
                        .arg($v(log2Level))
                        .arg($v(log2message))
                        .arg(NULL)
                        .arg($v(log2t))
        );

        final JMethodDef log3 = classDef.method(JMod.PUBLIC | JMod.FINAL, JType.VOID, "log");
        log3.annotate(Override.class);
        final JParamDeclaration log3Level = log3.param(logLevelClass, "level");
        final JParamDeclaration log3loggerFqcn = log3.param(String.class, "loggerFqcn");
        final JParamDeclaration log3message = log3.param(Object.class, "message");
        final JParamDeclaration log3t = log3.param(Throwable.class, "t");
        log3.body().add(
                logVar.call("log")
                        .arg($v(log3Level))
                        .arg($v(log3loggerFqcn))
                        .arg($v(log3message))
                        .arg($v(log3t))
        );

        final JMethodDef log4 = classDef.method(JMod.PUBLIC | JMod.FINAL, JType.VOID, "log");
        log4.annotate(Override.class);
        final JParamDeclaration log4loggerFqcn = log4.param(String.class, "loggerFqcn");
        final JParamDeclaration log4Level = log4.param(logLevelClass, "level");
        final JParamDeclaration log4message = log4.param(Object.class, "message");
        final JParamDeclaration log4params = log4.param($t(Object.class).array(), "params");
        final JParamDeclaration log4t = log4.param(Throwable.class, "t");
        log4.body().add(
                logVar.call("log")
                        .arg($v(log4loggerFqcn))
                        .arg($v(log4Level))
                        .arg($v(log4message))
                        .arg($v(log4params))
                        .arg($v(log4t))
        );

        // 12 methods each for v and f
        for (String affix : Arrays.asList("v", "f")) {
            final String name = "log" + affix;

            // 4 methods each for with- and without-throwable and fqcn
            for (RenderLog render : RenderLog.values()) {
                JParamDeclaration logFqcn = null;
                JParamDeclaration thr = null;
                final boolean renderThr = render.isThr();
                final boolean renderFqcn = render.isFqcn();

                final JMethodDef log1x = classDef.method(JMod.PUBLIC | JMod.FINAL, JType.VOID, name);
                log1x.annotate(Override.class);
                if (renderFqcn) logFqcn = log1x.param(String.class, "loggerFqcn");
                final JParamDeclaration log1xLevel = log1x.param(logLevelClass, "level");
                if (renderThr) thr = log1x.param(Throwable.class, "t");
                final JParamDeclaration log1xFormat = log1x.param(String.class, "format");
                final JParamDeclaration log1xParams = log1x.varargParam(Object.class, "params");
                log1x.body().add(
                        logVar.call(name)
                                .arg(renderFqcn ? $v(logFqcn) : $v(fqcn))
                                .arg($v(log1xLevel))
                                .arg(renderThr ? $v(thr) : NULL)
                                .arg($v(log1xFormat))
                                .arg($v(log1xParams))
                );

                // 3 methods for 3 parameter counts
                for (int i = 1; i <= 3; i++) {
                    final JMethodDef log2x = classDef.method(JMod.PUBLIC | JMod.FINAL, JType.VOID, name);
                    log2x.annotate(Override.class);
                    if (renderFqcn) logFqcn = log2x.param(String.class, "loggerFqcn");
                    final JParamDeclaration log2xLevel = log2x.param(logLevelClass, "level");
                    if (renderThr) thr = log2x.param(Throwable.class, "t");
                    final JParamDeclaration log2xFormat = log2x.param(String.class, "format");
                    final JParamDeclaration[] params = new JParamDeclaration[i];
                    for (int j = 0; j < i; j++) {
                        params[j] = log2x.param(Object.class, "param" + (j + 1));
                    }
                    final JCall log2xCaller = logVar.call(name);
                    log2xCaller.arg(renderFqcn ? $v(logFqcn) : $v(fqcn))
                            .arg($v(log2xLevel))
                            .arg(renderThr ? $v(thr) : NULL)
                            .arg($v(log2xFormat));
                    for (int j = 0; j < i; j++) {
                        log2xCaller.arg($v(params[j]));
                    }
                    log2x.body().add(log2xCaller);
                }
            }
        }
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
        if (ElementHelper.isAnnotatedWith(messageMethod.reference(), Once.class) && messageMethod.isLoggerMethod()) {
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
        } else if (!messageMethod.parameters(ParameterType.TRANSFORM).isEmpty()) {
            body = method.body()._if(logger.call("isEnabled").arg($v(messageMethod.logLevel()))).block(Braces.REQUIRED);
        } else {
            body = method.body();
        }

        // Determine which logger method to invoke
        final JCall logCaller = logger.call(messageMethod.loggerMethod());
        if (messageMethod.parameters(ParameterType.FQCN).isEmpty()) {
            logCaller.arg($v(FQCN_FIELD_NAME));
        } else {
            logCaller.arg($v(params.get(messageMethod.parameters(ParameterType.FQCN).iterator().next())).call("getName"));
        }
        logCaller.arg($v(messageMethod.logLevel()));


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
                switch (param.parameterType()) {
                    case FORMAT:
                        if (formatterClass == null) {
                            if (param.isArray() || param.isVarArgs()) {
                                args.add($t(Arrays.class).call("toString").arg($v(var)));
                            } else {
                                args.add($v(var));
                            }
                        } else {
                            args.add($t(formatterClass)._new().arg($v(var)));
                        }
                        break;
                    case TRANSFORM:
                        final JAssignableExpr transformVar = createTransformVar(parameterNames, body, param, $v(var));
                        if (formatterClass == null) {
                            args.add(transformVar);
                        } else {
                            args.add($t(formatterClass)._new().arg(transformVar));
                        }
                        break;
                    case POS:
                        final Pos pos = param.pos();
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
                        break;
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
        for (Parameter param : messageMethod.parameters(ParameterType.ANY)) {
            final JParamDeclaration var = addMethodParameter(method, param);
            result.put(param, var);
        }
        return result;
    }

    enum RenderLog {
        NONE(false, false),
        CAUSE(true, false),
        FQCN(true, true),;
        private final boolean thr;
        private final boolean fqcn;

        private RenderLog(boolean thr, boolean fqcn) {
            this.thr = thr;
            this.fqcn = fqcn;
        }

        public boolean isThr() {
            return thr;
        }

        public boolean isFqcn() {
            return fqcn;
        }
    }
}
