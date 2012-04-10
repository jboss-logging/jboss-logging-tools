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

package org.jboss.logging.processor.model;

import static org.jboss.logging.processor.Tools.loggers;
import static org.jboss.logging.processor.intf.model.Parameter.ParameterType;
import static org.jboss.logging.processor.model.ClassModelHelper.formatMessageId;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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
import org.jboss.logging.processor.intf.model.MessageInterface;
import org.jboss.logging.processor.intf.model.MessageMethod;
import org.jboss.logging.processor.intf.model.Parameter;

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

    /**
     * Creates a new message logger code model.
     *
     * @param messageInterface the message interface to implement.
     * @param useLogging31     {@code true} to use logging 3.1, {@code false} to remain compatible with 3.0
     */
    public MessageLoggerImplementor(final MessageInterface messageInterface, final boolean useLogging31) {
        super(messageInterface);
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
        if (messageInterface().loggingFQCN() == null) {
            fqcn.init(getDefinedClass().dotclass().invoke("getName"));
        } else {
            fqcn.init(codeModel.ref(messageInterface().loggingFQCN()).dotclass().invoke("getName"));
        }

        // Add default constructor
        final JMethod constructor = getDefinedClass().constructor(JMod.PUBLIC);
        final JVar constructorParam = constructor.param(JMod.FINAL, loggers().loggerClass(), LOG_FIELD_NAME);
        final JBlock constructorBody = constructor.body();
        final JExpression logger;
        if (messageInterface().extendsLoggerInterface()) {
            if (useLogging31) {
                getDefinedClass()._extends(loggers().delegatingLogger());
                constructorBody.add(JExpr.invoke("super").arg(constructorParam));
                logger = JExpr._super().ref("log");
            } else {
                JFieldVar logVar = getDefinedClass().field(JMod.PROTECTED | JMod.FINAL, loggers().loggerClass(), LOG_FIELD_NAME);
                constructorBody.assign(JExpr._this().ref(logVar), constructorParam);
                logger = logVar;

                // and now... the moment we've been dreading....
                generateDelegatingLoggerMethods(codeModel, logVar, fqcn);
            }
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

    private void generateDelegatingLoggerMethods(JCodeModel codeModel, JFieldVar logVar, JFieldVar fqcn) {
        // Generate these methods so they look the same as they appear in DelegatedBasicLogger.
        for (String level : Arrays.asList("TRACE", "DEBUG", "INFO", "WARN", "ERROR", "FATAL")) {
            // string prep
            String firstUppered = level.charAt(0) + level.substring(1).toLowerCase(Locale.US);
            String lowered = level.toLowerCase(Locale.US);

            if (level.equals("TRACE") || level.equals("DEBUG") || level.equals("INFO")) {
                // isXxxEnabled...
                final String isXxxEnabledStr = "is" + firstUppered + "Enabled";
                final JMethod isXxxEnabled = getDefinedClass().method(JMod.PUBLIC | JMod.FINAL, codeModel.BOOLEAN, isXxxEnabledStr);
                isXxxEnabled.body()._return(JExpr.invoke(logVar, isXxxEnabledStr));
            }

            // now, the four "raw" level-specific methods
            final JMethod xxx1 = getDefinedClass().method(JMod.PUBLIC | JMod.FINAL, codeModel.VOID, lowered);
            final JVar xxx1message = xxx1.param(codeModel.ref(Object.class), "message");
            final JInvocation xxx1inv = xxx1.body().invoke(logVar, lowered);
            xxx1inv.arg(fqcn);
            xxx1inv.arg(xxx1message);
            xxx1inv.arg(JExpr._null());

            final JMethod xxx2 = getDefinedClass().method(JMod.PUBLIC | JMod.FINAL, codeModel.VOID, lowered);
            final JVar xxx2message = xxx2.param(codeModel.ref(Object.class), "message");
            final JVar xxx2t = xxx2.param(codeModel.ref(Throwable.class), "t");
            final JInvocation xxx2inv = xxx2.body().invoke(logVar, lowered);
            xxx2inv.arg(fqcn);
            xxx2inv.arg(xxx2message);
            xxx2inv.arg(xxx2t);

            final JMethod xxx3 = getDefinedClass().method(JMod.PUBLIC | JMod.FINAL, codeModel.VOID, lowered);
            final JVar xxx3loggerFqcn = xxx3.param(codeModel.ref(String.class), "loggerFqcn");
            final JVar xxx3message = xxx3.param(codeModel.ref(Object.class), "message");
            final JVar xxx3t = xxx3.param(codeModel.ref(Throwable.class), "t");
            final JInvocation xxx3inv = xxx3.body().invoke(logVar, lowered);
            xxx3inv.arg(xxx3loggerFqcn);
            xxx3inv.arg(xxx3message);
            xxx3inv.arg(xxx3t);

            final JMethod xxx4 = getDefinedClass().method(JMod.PUBLIC | JMod.FINAL, codeModel.VOID, lowered);
            final JVar xxx4loggerFqcn = xxx4.param(codeModel.ref(String.class), "loggerFqcn");
            final JVar xxx4message = xxx4.param(codeModel.ref(Object.class), "message");
            final JVar xxx4params = xxx4.param(codeModel.ref(Object[].class), "params");
            final JVar xxx4t = xxx4.param(codeModel.ref(Throwable.class), "t");
            final JInvocation xxx4inv = xxx4.body().invoke(logVar, lowered);
            xxx4inv.arg(xxx4loggerFqcn);
            xxx4inv.arg(xxx4message);
            xxx4inv.arg(xxx4params);
            xxx4inv.arg(xxx4t);

            // 8 methods each for v and f
            for (String affix : Arrays.asList("v", "f")) {
                final String name = lowered + affix;
                final String target = "log" + affix;

                // 4 methods each for with- and without-throwable
                for (boolean renderThr : new boolean[] {false, true}) {
                    JVar thr = null;

                    final JMethod xxx1x = getDefinedClass().method(JMod.PUBLIC | JMod.FINAL, codeModel.VOID, name);
                    if (renderThr) thr = xxx1x.param(codeModel.ref(Throwable.class), "t");
                    final JVar xxx1xFormat = xxx1x.param(codeModel.ref(String.class), "format");
                    final JVar xxx1xParams = xxx1x.varParam(codeModel.ref(Object.class), "params");
                    final JInvocation xxx1xInv = xxx1x.body().invoke(logVar, target);
                    xxx1xInv.arg(fqcn);
                    xxx1xInv.arg(codeModel.ref(loggers().logLevelClass()).staticRef(level));
                    xxx1xInv.arg(renderThr ? thr : JExpr._null());
                    xxx1xInv.arg(xxx1xFormat);
                    xxx1xInv.arg(xxx1xParams);

                    // 3 methods for 3 parameter counts
                    for (int i = 1; i <= 3; i++) {
                        final JMethod xxx2x = getDefinedClass().method(JMod.PUBLIC | JMod.FINAL, codeModel.VOID, name);
                        if (renderThr) thr = xxx2x.param(codeModel.ref(Throwable.class), "t");
                        final JVar xxx2xFormat = xxx2x.param(codeModel.ref(String.class), "format");
                        final JVar[] params = new JVar[i];
                        for (int j = 0; j < i; j++) {
                            params[j] = xxx2x.param(codeModel.ref(Object.class), "param" + (j + 1));
                        }
                        final JInvocation xxx2xInv = xxx2x.body().invoke(logVar, target);
                        xxx2xInv.arg(fqcn);
                        xxx2xInv.arg(codeModel.ref(loggers().logLevelClass()).staticRef(level));
                        xxx2xInv.arg(renderThr ? thr : JExpr._null());
                        xxx2xInv.arg(xxx2xFormat);
                        for (int j = 0; j < i; j++) {
                            xxx2xInv.arg(params[j]);
                        }
                    }
                }
            }
        }

        // Now the plain "log" methods which take a level

        // isEnabled...
        final JMethod isEnabled = getDefinedClass().method(JMod.PUBLIC | JMod.FINAL, codeModel.BOOLEAN, "isEnabled");
        final JVar isEnabledLevel = isEnabled.param(codeModel.ref(loggers().logLevelClass()), "level");
        final JInvocation isEnabledInv = JExpr.invoke(logVar, "isEnabled");
        isEnabledInv.arg(isEnabledLevel);
        isEnabled.body()._return(isEnabledInv);

        // now, the four "raw" log methods
        final JMethod log1 = getDefinedClass().method(JMod.PUBLIC | JMod.FINAL, codeModel.VOID, "log");
        final JVar log1Level = log1.param(codeModel.ref(loggers().logLevelClass()), "level");
        final JVar log1Message = log1.param(codeModel.ref(Object.class), "message");
        final JInvocation log1inv = log1.body().invoke(logVar, "log");
        log1inv.arg(fqcn);
        log1inv.arg(log1Level);
        log1inv.arg(log1Message);
        log1inv.arg(JExpr._null());
        log1inv.arg(JExpr._null());

        final JMethod log2 = getDefinedClass().method(JMod.PUBLIC | JMod.FINAL, codeModel.VOID, "log");
        final JVar log2Level = log2.param(codeModel.ref(loggers().logLevelClass()), "level");
        final JVar log2message = log2.param(codeModel.ref(Object.class), "message");
        final JVar log2t = log2.param(codeModel.ref(Throwable.class), "t");
        final JInvocation log2inv = log2.body().invoke(logVar, "log");
        log2inv.arg(fqcn);
        log2inv.arg(log2Level);
        log2inv.arg(log2message);
        log2inv.arg(JExpr._null());
        log2inv.arg(log2t);

        final JMethod log3 = getDefinedClass().method(JMod.PUBLIC | JMod.FINAL, codeModel.VOID, "log");
        final JVar log3Level = log3.param(codeModel.ref(loggers().logLevelClass()), "level");
        final JVar log3loggerFqcn = log3.param(codeModel.ref(String.class), "loggerFqcn");
        final JVar log3message = log3.param(codeModel.ref(Object.class), "message");
        final JVar log3t = log3.param(codeModel.ref(Throwable.class), "t");
        final JInvocation log3inv = log3.body().invoke(logVar, "log");
        log3inv.arg(log3Level);
        log3inv.arg(log3loggerFqcn);
        log3inv.arg(log3message);
        log3inv.arg(log3t);

        final JMethod log4 = getDefinedClass().method(JMod.PUBLIC | JMod.FINAL, codeModel.VOID, "log");
        final JVar log4loggerFqcn = log4.param(codeModel.ref(String.class), "loggerFqcn");
        final JVar log4Level = log4.param(codeModel.ref(loggers().logLevelClass()), "level");
        final JVar log4message = log4.param(codeModel.ref(Object.class), "message");
        final JVar log4params = log4.param(codeModel.ref(Object[].class), "params");
        final JVar log4t = log4.param(codeModel.ref(Throwable.class), "t");
        final JInvocation log4inv = log4.body().invoke(logVar, "log");
        log4inv.arg(log4loggerFqcn);
        log4inv.arg(log4Level);
        log4inv.arg(log4message);
        log4inv.arg(log4params);
        log4inv.arg(log4t);

        // 12 methods each for v and f
        for (String affix : Arrays.asList("v", "f")) {
            final String name = "log" + affix;
            JVar logFqcn = null;
            JVar thr = null;

            // 4 methods each for with- and without-throwable and fqcn
            for (RenderLog render : RenderLog.values()) {
                thr = null;
                final boolean renderThr = render.isThr();
                final boolean renderFqcn = render.isFqcn();

                final JMethod log1x = getDefinedClass().method(JMod.PUBLIC | JMod.FINAL, codeModel.VOID, name);
                if (renderFqcn) logFqcn = log1x.param(codeModel.ref(String.class), "loggerFqcn");
                final JVar log1xLevel = log1x.param(codeModel.ref(loggers().logLevelClass()), "level");
                if (renderThr) thr = log1x.param(codeModel.ref(Throwable.class), "t");
                final JVar log1xFormat = log1x.param(codeModel.ref(String.class), "format");
                final JVar log1xParams = log1x.varParam(codeModel.ref(Object.class), "params");
                final JInvocation log1xInv = log1x.body().invoke(logVar, name);
                log1xInv.arg(renderFqcn ? logFqcn : fqcn);
                log1xInv.arg(log1xLevel);
                log1xInv.arg(renderThr ? thr : JExpr._null());
                log1xInv.arg(log1xFormat);
                log1xInv.arg(log1xParams);

                // 3 methods for 3 parameter counts
                for (int i = 1; i <= 3; i++) {
                    final JMethod log2x = getDefinedClass().method(JMod.PUBLIC | JMod.FINAL, codeModel.VOID, name);
                    if (renderFqcn) logFqcn = log2x.param(codeModel.ref(String.class), "loggerFqcn");
                    final JVar log2xLevel = log2x.param(codeModel.ref(loggers().logLevelClass()), "level");
                    if (renderThr) thr = log2x.param(codeModel.ref(Throwable.class), "t");
                    final JVar log2xFormat = log2x.param(codeModel.ref(String.class), "format");
                    final JVar[] params = new JVar[i];
                    for (int j = 0; j < i; j++) {
                        params[j] = log2x.param(codeModel.ref(Object.class), "param" + (j + 1));
                    }
                    final JInvocation log2xInv = log2x.body().invoke(logVar, name);
                    log2xInv.arg(renderFqcn ? logFqcn : fqcn);
                    log2xInv.arg(log2xLevel);
                    log2xInv.arg(renderThr ? thr : JExpr._null());
                    log2xInv.arg(log2xFormat);
                    for (int j = 0; j < i; j++) {
                        log2xInv.arg(params[j]);
                    }
                }
            }
        }
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
        // Initialize the method parameters
        final Map<Parameter, JVar> params = createParameters(messageMethod, method);

        // Determine which logger method to invoke
        final JInvocation logInv = body.invoke(logger, messageMethod.loggerMethod());
        if (messageMethod.parameters(ParameterType.FQCN).isEmpty()) {
            logInv.arg(JExpr.ref(FQCN_FIELD_NAME));
        } else {
            logInv.arg(params.get(messageMethod.parameters(ParameterType.FQCN).iterator().next()).invoke("getName"));
        }
        logInv.arg(JExpr.direct(messageMethod.logLevel()));
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
        for (Map.Entry<Parameter, JVar> entry : params.entrySet()) {
            final Parameter param = entry.getKey();
            final String formatterClass = param.formatterClass();
            switch (param.parameterType()) {
                case FORMAT:
                    if (formatterClass == null) {
                        logInv.arg(entry.getValue());
                    } else {
                        logInv.arg(JExpr._new(getCodeModel().ref(formatterClass)).arg(entry.getValue()));
                    }
                    break;
            }
        }
    }

    private Map<Parameter, JVar> createParameters(final MessageMethod messageMethod, final JMethod method) {
        final Map<Parameter, JVar> result = new LinkedHashMap<Parameter, JVar>();
        // Create the parameters
        for (Parameter param : messageMethod.parameters(ParameterType.ANY)) {
            final JClass paramType = getCodeModel().ref(param.type());
            final JVar var = method.param(JMod.FINAL, paramType, param.name());
            result.put(param, var);
        }
        return result;
    }
}
