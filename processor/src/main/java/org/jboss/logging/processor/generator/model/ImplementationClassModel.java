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

import static org.jboss.logging.processor.generator.model.ClassModelHelper.implementationClassName;
import static org.jboss.logging.processor.model.Parameter.ParameterType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jboss.jdeparser.JBlock;
import org.jboss.jdeparser.JClass;
import org.jboss.jdeparser.JConditional;
import org.jboss.jdeparser.JDeparser;
import org.jboss.jdeparser.JExpr;
import org.jboss.jdeparser.JExpression;
import org.jboss.jdeparser.JFieldVar;
import org.jboss.jdeparser.JInvocation;
import org.jboss.jdeparser.JMethod;
import org.jboss.jdeparser.JMod;
import org.jboss.jdeparser.JVar;
import org.jboss.logging.annotations.Pos;
import org.jboss.logging.annotations.Transform;
import org.jboss.logging.annotations.Transform.TransformType;
import org.jboss.logging.processor.model.MessageInterface;
import org.jboss.logging.processor.model.MessageMethod;
import org.jboss.logging.processor.model.Parameter;
import org.jboss.logging.processor.model.ThrowableType;

/**
 * An abstract code model to create the source file that implements the
 * interface.
 * <p/>
 * <p>
 * Essentially this uses the org.jboss.jdeparser.JDeparser to generate the
 * source files with. This class is for convenience in generating default source
 * files.
 * </p>
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
abstract class ImplementationClassModel extends ClassModel {

    /**
     * Class constructor.
     *
     * @param messageInterface the message interface to implement.
     */
    ImplementationClassModel(final MessageInterface messageInterface) {
        super(messageInterface, implementationClassName(messageInterface), null);
    }

    @Override
    protected JDeparser generateModel() throws IllegalStateException {
        JDeparser codeModel = super.generateModel();
        getDefinedClass()._implements(codeModel.directClass(Serializable.class.getName()));
        // Add the serializable UID
        JFieldVar serialVersionUID = getDefinedClass().field(JMod.PRIVATE | JMod.STATIC | JMod.FINAL, codeModel.LONG, "serialVersionUID");
        serialVersionUID.init(JExpr.lit(1L));

        return codeModel;
    }

    /**
     * Create the bundle method body.
     *
     * @param messageMethod the message method.
     * @param method        the method to create the body for.
     * @param msgMethod     the message method for retrieving the message.
     */
    void createBundleMethod(final MessageMethod messageMethod, final JMethod method, final JMethod msgMethod) {
        addThrownTypes(messageMethod, method);
        // Create the body of the method and add the text
        final JBlock body = method.body();
        final JClass returnField = getCodeModel().directClass(method.type().fullName());
        final MessageMethod.Message message = messageMethod.message();
        final JExpression expression;
        final JInvocation formatterMethod;
        final boolean noFormatParameters = messageMethod.parameters(ParameterType.FORMAT).isEmpty();

        switch (message.format()) {
            case MESSAGE_FORMAT: {
                final JClass formatter = getCodeModel().directClass(message.format().formatClass().getName());
                formatterMethod = formatter.staticInvoke(message.format().staticMethod());
                if (noFormatParameters) {
                    expression = JExpr.invoke(msgMethod);
                } else {
                    formatterMethod.arg(JExpr.invoke(msgMethod));
                    expression = formatterMethod;
                }
                break;
            }
            case PRINTF: {
                final JClass formatter = getCodeModel().directClass(message.format().formatClass().getName());
                formatterMethod = formatter.staticInvoke(message.format().staticMethod()).arg(JExpr.invoke(msgMethod));
                expression = formatterMethod;
                break;
            }
            default:
                formatterMethod = null;
                expression = JExpr.invoke(msgMethod);
                break;
        }

        // Create maps for the fields and properties. Key is the field or setter method, value is the parameter to set
        // the value to.
        final Set<Parameter> allParameters = messageMethod.parameters(ParameterType.ANY);
        final Map<String, JVar> fields = new LinkedHashMap<String, JVar>();
        final Map<String, JVar> properties = new LinkedHashMap<String, JVar>();

        // First load the parameter names
        final List<String> parameterNames = new ArrayList<String>(allParameters.size());
        for (Parameter param : allParameters) {
            parameterNames.add(param.name());
        }
        final List<JExpression> args = new ArrayList<JExpression>();
        // Create the parameters
        for (Parameter param : allParameters) {
            final JClass paramType;
            if (param.isArray() && !param.isVarArgs()) {
                paramType = getCodeModel().directClass(param.type()).array();
            } else {
                paramType = getCodeModel().directClass(param.type());
            }
            final JVar var;
            if (param.isVarArgs()) {
                var = method.varParam(paramType, param.name());
            } else {
                var = method.param(JMod.FINAL, paramType, param.name());
            }
            final String formatterClass = param.formatterClass();
            switch (param.parameterType()) {
                case FORMAT: {
                    if (formatterMethod == null) {
                        // This should never happen, but let's safe guard against it
                        throw new IllegalStateException("No format parameters are allowed when NO_FORMAT is specified.");
                    } else {
                        if (formatterClass == null) {
                            if (param.isArray() || param.isVarArgs()) {
                                args.add(getCodeModel().directClass(Arrays.class.getName()).staticInvoke("toString").arg(var));
                            } else {
                                args.add(var);
                            }
                        } else {
                            args.add(JExpr._new(getCodeModel().directClass(formatterClass)).arg(var));
                        }
                    }
                    break;
                }
                case TRANSFORM: {
                    if (formatterMethod == null) {
                        // This should never happen, but let's safe guard against it
                        throw new IllegalStateException("No format parameters are allowed when NO_FORMAT is specified.");
                    } else {
                        final JVar transformVar = createTransformVar(parameterNames, body, param, var);
                        if (formatterClass == null) {
                            args.add(transformVar);
                        } else {
                            final JInvocation invocation = JExpr._new(getCodeModel().directClass(formatterClass));
                            args.add(invocation.arg(transformVar));
                        }
                    }
                    break;
                }
                case POS: {
                    if (formatterMethod == null) {
                        // This should never happen, but let's safe guard against it
                        throw new IllegalStateException("No format parameters are allowed when NO_FORMAT is specified.");
                    } else {
                        final Pos pos = param.pos();
                        final int[] positions = pos.value();
                        final Transform[] transform = pos.transform();
                        for (int i = 0; i < positions.length; i++) {
                            final int index = positions[i] - 1;
                            if (transform != null && transform.length > 0) {
                                final JVar tVar = createTransformVar(parameterNames, method.body(), param, transform[i], var);
                                if (index < args.size()) {
                                    args.add(index, tVar);
                                } else {
                                    args.add(tVar);
                                }
                            } else {
                                if (index < args.size()) {
                                    args.add(index, var);
                                } else {
                                    args.add(var);
                                }
                            }
                        }
                    }
                    break;
                }
                case FIELD: {
                    fields.put(param.targetName(), var);
                    break;
                }
                case PROPERTY: {
                    properties.put(param.targetName(), var);
                    break;
                }
            }
        }
        // If a format method, add the arguments
        if (formatterMethod != null)
            for (JExpression arg : args) {
                formatterMethod.arg(arg);
            }
        // Setup the return type
        final JVar result = body.decl(returnField, "result");
        if (messageMethod.returnType().isThrowable()) {
            initCause(result, returnField, body, messageMethod, expression);
        } else {
            result.init(expression);
        }
        // Set the fields and properties of the return type
        for (Map.Entry<String, JVar> entry : fields.entrySet()) {
            body.assign(result.ref(entry.getKey()), entry.getValue());
        }
        for (Map.Entry<String, JVar> entry : properties.entrySet()) {
            body.add(result.invoke(entry.getKey()).arg(entry.getValue()));
        }
        body._return(result);
    }

    JVar createTransformVar(final List<String> parameterNames, final JBlock methodBody, final Parameter param, final JVar var) {
        return createTransformVar(parameterNames, methodBody, param, param.transform(), var);
    }

    JVar createTransformVar(final List<String> parameterNames, final JBlock methodBody, final Parameter param, final Transform transform, final JVar var) {
        final int currentPos = methodBody.pos();
        // Position to method body to start
        methodBody.pos(0);
        final List<TransformType> transformTypes = Arrays.asList(transform.value());
        final JVar result;
        // Create the conditional elements
        final JConditional condition = methodBody._if(var.eq(JExpr._null()));
        final JBlock ifBlock = condition._then();
        final JBlock elseBlock = condition._else();
        // Reposition to start for variable creation
        methodBody.pos(0);
        // GET_CLASS should always be processed first
        if (transformTypes.contains(TransformType.GET_CLASS)) {
            // Determine the result field type
            if (transformTypes.size() == 1) {
                // Get the parameter name
                final String paramName = getUniqueName(parameterNames, param, "Class");
                parameterNames.add(paramName);
                result = methodBody.decl(getCodeModel().directClass("java.lang.Class"), paramName);
                ifBlock.assign(result, JExpr._null());
                elseBlock.assign(result, var.invoke("getClass"));
            } else {
                // Get the parameter name
                final String paramName = getUniqueName(parameterNames, param, "HashCode");
                parameterNames.add(paramName);
                result = methodBody.decl(getCodeModel().INT, paramName);
                ifBlock.assign(result, JExpr.lit(0));
                if (transformTypes.contains(TransformType.HASH_CODE)) {
                    elseBlock.assign(result, var.invoke("getClass").invoke("hashCode"));
                } else if (transformTypes.contains(TransformType.IDENTITY_HASH_CODE)) {
                    elseBlock.assign(result, getCodeModel().directClass("java.lang.System").staticInvoke("identityHashCode").arg(var.invoke("getClass")));
                } else {
                    throw new IllegalStateException(String.format("Invalid transform type combination: %s", transformTypes));
                }
            }
        } else if (transformTypes.contains(TransformType.HASH_CODE)) {
            // Get the parameter name
            final String paramName = getUniqueName(parameterNames, param, "HashCode");
            parameterNames.add(paramName);
            result = methodBody.decl(getCodeModel().INT, paramName);
            ifBlock.assign(result, JExpr.lit(0));
            if (param.isArray() || param.isVarArgs()) {
                elseBlock.assign(result, getCodeModel().directClass(Arrays.class.getName()).staticInvoke("hashCode").arg(var));
            } else {
                elseBlock.assign(result, var.invoke("hashCode"));
            }
        } else if (transformTypes.contains(TransformType.IDENTITY_HASH_CODE)) {
            // Get the parameter name
            final String paramName = getUniqueName(parameterNames, param, "HashCode");
            parameterNames.add(paramName);
            result = methodBody.decl(getCodeModel().INT, paramName);
            ifBlock.assign(result, JExpr.lit(0));
            elseBlock.assign(result, getCodeModel().directClass("java.lang.System").staticInvoke("identityHashCode").arg(var));
        } else if (transformTypes.contains(TransformType.SIZE)) {
            // Get the parameter name
            final String paramName = getUniqueName(parameterNames, param, "Size");
            parameterNames.add(paramName);
            result = methodBody.decl(getCodeModel().INT, paramName);
            ifBlock.assign(result, JExpr.lit(0));
            if (param.isArray() || param.isVarArgs()) {
                elseBlock.assign(result, var.ref("length"));
            } else if (param.isSubtypeOf(Map.class) || param.isSubtypeOf(Collection.class)) {
                elseBlock.assign(result, var.invoke("size"));
            } else if (param.isSubtypeOf(CharSequence.class)) {
                elseBlock.assign(result, var.invoke("length"));
            } else {
                throw new IllegalStateException(String.format("Invalid type for %s. Must be an array, %s, %s or %s.",
                        TransformType.SIZE, Collection.class.getName(), Map.class.getName(), CharSequence.class.getName()));
            }
        } else {
            throw new IllegalStateException(String.format("Invalid transform type: %s", transformTypes));
        }
        // Set the position to the end of the transform blocks. Should always be 2, variable declaration and if/else block.
        methodBody.pos(currentPos + 2);
        return result;
    }

    private String getUniqueName(final List<String> parameterNames, final Parameter parameter, final String suffix) {
        String result = (suffix == null ? parameter.name() : parameter.name().concat(suffix));
        if (parameterNames.contains(result)) {
            return getUniqueName(parameterNames, new StringBuilder(result), 0);
        }
        return result;
    }

    private String getUniqueName(final List<String> parameterNames, final StringBuilder sb, final int index) {
        String result = sb.append(index).toString();
        if (parameterNames.contains(result)) {
            return getUniqueName(parameterNames, sb, index + 1);
        }
        return result;
    }

    /*
     * Initialize the cause (Throwable) return type.
     *
     * @param result        the return variable
     * @param returnField   the return field
     * @param body          the body of the messageMethod
     * @param messageMethod the message messageMethod
     * @param format        the format used to format the string cause
     */
    private void initCause(final JVar result, final JClass returnField, final JBlock body, final MessageMethod messageMethod, final JExpression format) {
        final ThrowableType returnType = messageMethod.returnType().throwableReturnType();
        if (returnType.useConstructionParameters()) {
            final JInvocation invocation = JExpr._new(returnField);
            for (Parameter param : returnType.constructionParameters()) {
                switch (param.parameterType()) {
                    case MESSAGE:
                        invocation.arg(format);
                        break;
                    default:
                        invocation.arg(JExpr.ref(param.name()));
                        break;

                }
            }
            result.init(invocation);
        } else if (returnType.hasStringAndThrowableConstructor() && messageMethod.hasCause()) {
            result.init(JExpr._new(returnField).arg(format).arg(JExpr.ref(messageMethod.cause().name())));
        } else if (returnType.hasThrowableAndStringConstructor() && messageMethod.hasCause()) {
            result.init(JExpr._new(returnField).arg(JExpr.ref(messageMethod.cause().name())).arg(format));
        } else if (returnType.hasStringConstructor()) {
            result.init(JExpr._new(returnField).arg(format));
            if (messageMethod.hasCause()) {
                JInvocation initCause = body.invoke(result, "initCause");
                initCause.arg(JExpr.ref(messageMethod.cause().name()));
            }
        } else if (returnType.hasThrowableConstructor() && messageMethod.hasCause()) {
            result.init(JExpr._new(returnField).arg(JExpr.ref(messageMethod.cause().name())));
        } else if (returnType.hasStringAndThrowableConstructor() && !messageMethod.hasCause()) {
            result.init(JExpr._new(returnField).arg(format).arg(JExpr._null()));
        } else if (returnType.hasThrowableAndStringConstructor() && !messageMethod.hasCause()) {
            result.init(JExpr._new(returnField).arg(JExpr._null()).arg(format));
        } else if (messageMethod.hasCause()) {
            result.init(JExpr._new(returnField));
            JInvocation initCause = body.invoke(result, "initCause");
            initCause.arg(JExpr.ref(messageMethod.cause().name()));
        } else {
            result.init(JExpr._new(returnField));
        }
        final JClass arrays = getCodeModel().directClass(Arrays.class.getName());
        final JClass stClass = getCodeModel().directClass(StackTraceElement.class.getName()).array();
        final JVar st = body.decl(stClass, "st").init(result.invoke("getStackTrace"));
        final JInvocation setStackTrace = result.invoke("setStackTrace");
        setStackTrace.arg(arrays.staticInvoke("copyOfRange").arg(st).arg(JExpr.lit(1)).arg(st.ref("length")));
        body.add(setStackTrace);
    }

    protected final void addThrownTypes(final MessageMethod messageMethod, final JMethod jMethod) {
        for (ThrowableType thrownType : messageMethod.thrownTypes()) {
            jMethod._throws(getCodeModel().directClass(thrownType.name()));
        }
    }
}
