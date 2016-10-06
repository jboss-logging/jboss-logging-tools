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
import static org.jboss.jdeparser.JExprs.$v;
import static org.jboss.jdeparser.JMod.FINAL;
import static org.jboss.jdeparser.JTypes.$t;
import static org.jboss.logging.processor.generator.model.ClassModelHelper.implementationClassName;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Filer;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleAnnotationValueVisitor8;

import org.jboss.jdeparser.JAssignableExpr;
import org.jboss.jdeparser.JBlock;
import org.jboss.jdeparser.JBlock.Braces;
import org.jboss.jdeparser.JCall;
import org.jboss.jdeparser.JClassDef;
import org.jboss.jdeparser.JExpr;
import org.jboss.jdeparser.JExprs;
import org.jboss.jdeparser.JIf;
import org.jboss.jdeparser.JMethodDef;
import org.jboss.jdeparser.JMod;
import org.jboss.jdeparser.JParamDeclaration;
import org.jboss.jdeparser.JType;
import org.jboss.jdeparser.JTypes;
import org.jboss.jdeparser.JVarDeclaration;
import org.jboss.logging.annotations.Field;
import org.jboss.logging.annotations.Fields;
import org.jboss.logging.annotations.Pos;
import org.jboss.logging.annotations.Properties;
import org.jboss.logging.annotations.Property;
import org.jboss.logging.annotations.Suppressed;
import org.jboss.logging.annotations.Transform;
import org.jboss.logging.annotations.Transform.TransformType;
import org.jboss.logging.processor.apt.ProcessingException;
import org.jboss.logging.processor.model.MessageInterface;
import org.jboss.logging.processor.model.MessageMethod;
import org.jboss.logging.processor.model.Parameter;
import org.jboss.logging.processor.model.ThrowableType;
import org.jboss.logging.processor.util.ElementHelper;

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
     * @param filer            the filer used to create the source file
     * @param messageInterface the message interface to implement.
     * @param annotateOutput   true to have the generated class annotated with javax.annotation.Generated.
     */
    ImplementationClassModel(final Filer filer, final MessageInterface messageInterface, final boolean annotateOutput) {
        super(filer, messageInterface, implementationClassName(messageInterface), null, annotateOutput);
    }

    /**
     * Create the bundle method body.
     *
     * @param classDef      the class definition
     * @param messageMethod the message method.
     */
    void createBundleMethod(final JClassDef classDef, final MessageMethod messageMethod) {
        // Add the message messageMethod.
        addMessageMethod(messageMethod);
        final JType returnType = $t(messageMethod.returnType().name());
        sourceFile._import(returnType);
        final JMethodDef method = classDef.method(JMod.PUBLIC | FINAL, returnType, messageMethod.name());
        method.annotate(Override.class);
        addThrownTypes(messageMethod, method);
        // Create the body of the method and add the text
        final JBlock body = method.body();
        final MessageMethod.Message message = messageMethod.message();
        final JCall formatterCall;

        switch (message.format()) {
            case MESSAGE_FORMAT: {
                if (messageMethod.formatParameterCount() > 0) {
                    final JType formatter = $t(MessageFormat.class);
                    formatterCall = formatter.call("format");
                    formatterCall.arg(JExprs.call(messageMethod.messageMethodName()));
                } else {
                    formatterCall = JExprs.call(messageMethod.messageMethodName());
                }
                break;
            }
            case PRINTF: {
                final JType formatter = $t(String.class);
                formatterCall = formatter.call("format").arg(JExprs.call(messageMethod.messageMethodName()));
                break;
            }
            default:
                formatterCall = JExprs.call(messageMethod.messageMethodName());
                break;
        }

        // Create maps for the fields and properties. Key is the field or setter method, value is the parameter to set
        // the value to.
        final Set<Parameter> allParameters = messageMethod.parameters();
        final Map<String, JParamDeclaration> fields = new LinkedHashMap<>();
        final Map<String, JParamDeclaration> properties = new LinkedHashMap<>();

        // First load the parameter names
        final List<String> parameterNames = new ArrayList<>(allParameters.size());
        for (Parameter param : allParameters) {
            parameterNames.add(param.name());
        }
        final List<JExpr> args = new ArrayList<>();
        // Create the parameters
        for (Parameter param : allParameters) {
            final JParamDeclaration var = addMethodParameter(method, param);
            final String formatterClass = param.formatterClass();
            if (param.isFormatParameter()) {
                boolean added = false;
                if (param.isAnnotatedWith(Transform.class)) {
                    if (formatterCall == null) {
                        // This should never happen, but let's safe guard against it
                        throw new IllegalStateException("No format parameters are allowed when NO_FORMAT is specified.");
                    } else {
                        final JAssignableExpr transformVar = createTransformVar(parameterNames, body, param, $v(var));
                        if (formatterClass == null) {
                            args.add(transformVar);
                        } else {
                            args.add($t(formatterClass)._new().arg(transformVar));
                        }
                    }
                    added = true;
                }
                if (param.isAnnotatedWith(Pos.class)) {
                    if (formatterCall == null) {
                        // This should never happen, but let's safe guard against it
                        throw new IllegalStateException("No format parameters are allowed when NO_FORMAT is specified.");
                    } else {
                        final Pos pos = param.getAnnotation(Pos.class);
                        final int[] positions = pos.value();
                        final Transform[] transform = pos.transform();
                        for (int i = 0; i < positions.length; i++) {
                            final int index = positions[i] - 1;
                            if (transform != null && transform.length > 0) {
                                final JAssignableExpr tVar = createTransformVar(parameterNames, method.body(), param, transform[i], $v(var));
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
                    }
                    added = true;
                }
                if (!added) {
                    if (formatterCall == null) {
                        // This should never happen, but let's safe guard against it
                        throw new ProcessingException(messageMethod, "No format parameters are allowed when NO_FORMAT is specified.");
                    } else {
                        if (formatterClass == null) {
                            if (param.isArray() || param.isVarArgs()) {
                                final JType arrays = $t(Arrays.class);
                                sourceFile._import(arrays);
                                args.add(arrays.call("toString").arg($v(var)));
                            } else {
                                args.add($v(var));
                            }
                        } else {
                            args.add($t(formatterClass)._new().arg($v(var)));
                        }
                    }
                }
            }
            if (param.isAnnotatedWith(Field.class)) {
                fields.put(param.targetName(), var);
            }
            if (param.isAnnotatedWith(Property.class)) {
                properties.put(param.targetName(), var);
            }
        }
        // If a format method, add the arguments
        for (JExpr arg : args) {
            formatterCall.arg(arg);
        }
        // Setup the return type
        final JExpr result;
        if (messageMethod.returnType().isThrowable()) {
            result = $v(createReturnType(messageMethod, body, formatterCall));
        } else {
            result = formatterCall;
        }
        // Set the fields and properties of the return type
        for (Map.Entry<String, JParamDeclaration> entry : fields.entrySet()) {
            body.assign(result.field(entry.getKey()), $v(entry.getValue()));
        }
        for (Map.Entry<String, JParamDeclaration> entry : properties.entrySet()) {
            body.add(result.call(entry.getKey()).arg($v(entry.getValue())));
        }
        body._return(result);
    }

    JAssignableExpr createTransformVar(final List<String> parameterNames, final JBlock methodBody, final Parameter param, final JExpr var) {
        return createTransformVar(parameterNames, methodBody, param, param.getAnnotation(Transform.class), var);
    }

    JAssignableExpr createTransformVar(final List<String> parameterNames, final JBlock methodBody, final Parameter param, final Transform transform, final JExpr var) {
        final List<TransformType> transformTypes = Arrays.asList(transform.value());
        // GET_CLASS should always be processed first
        final JAssignableExpr result;
        if (transformTypes.contains(TransformType.GET_CLASS)) {
            // Determine the result field type
            if (transformTypes.size() == 1) {
                // Get the parameter name
                final String paramName = getUniqueName(parameterNames, param, "Class");
                parameterNames.add(paramName);
                result = $v(methodBody.var(FINAL, $t(Class.class).typeArg(JType.WILDCARD), paramName));
                final JIf stmt = methodBody._if(var.eq(NULL));
                stmt.block(Braces.REQUIRED).assign(result, NULL);
                stmt._else().assign(result, var.call("getClass"));
            } else {
                // Get the parameter name
                final String paramName = getUniqueName(parameterNames, param, "HashCode");
                parameterNames.add(paramName);
                result = $v(methodBody.var(FINAL, JType.INT, paramName));
                final JIf stmt = methodBody._if(var.eq(NULL));
                stmt.assign(result, JExpr.ZERO);
                if (transformTypes.contains(TransformType.HASH_CODE)) {
                    stmt._else().assign(result, var.call("getClass").call("hashCode"));
                } else if (transformTypes.contains(TransformType.IDENTITY_HASH_CODE)) {
                    stmt._else().assign(result, $t(System.class).call("identityHashCode").arg(var.call("getClass")));
                } else {
                    throw new IllegalStateException(String.format("Invalid transform type combination: %s", transformTypes));
                }
            }
        } else if (transformTypes.contains(TransformType.HASH_CODE)) {
            // Get the parameter name
            final String paramName = getUniqueName(parameterNames, param, "HashCode");
            parameterNames.add(paramName);
            result = $v(methodBody.var(FINAL, JType.INT, paramName));
            final JIf stmt = methodBody._if(var.eq(NULL));
            stmt.assign(result, JExpr.ZERO);
            if (param.isArray() || param.isVarArgs()) {
                final JType arrays = $t(Arrays.class);
                sourceFile._import(arrays);
                stmt._else().assign(result, arrays.call("hashCode").arg(var));
            } else {
                stmt._else().assign(result, var.call("hashCode"));
            }
        } else if (transformTypes.contains(TransformType.IDENTITY_HASH_CODE)) {
            // Get the parameter name
            final String paramName = getUniqueName(parameterNames, param, "HashCode");
            parameterNames.add(paramName);
            result = $v(methodBody.var(FINAL, JType.INT, paramName));
            final JIf stmt = methodBody._if(var.eq(NULL));
            stmt.assign(result, JExpr.ZERO);
            stmt._else().assign(result, $t(System.class).call("identityHashCode").arg(var));
        } else if (transformTypes.contains(TransformType.SIZE)) {
            // Get the parameter name
            final String paramName = getUniqueName(parameterNames, param, "Size");
            parameterNames.add(paramName);
            result = $v(methodBody.var(FINAL, JType.INT, paramName));
            final JIf stmt = methodBody._if(var.eq(NULL));
            stmt.assign(result, JExpr.ZERO);
            if (param.isArray() || param.isVarArgs()) {
                stmt._else().assign(result, var.field("length"));
            } else if (param.isSubtypeOf(Map.class) || param.isSubtypeOf(Collection.class)) {
                stmt._else().assign(result, var.call("size"));
            } else if (param.isSubtypeOf(CharSequence.class)) {
                stmt._else().assign(result, var.call("length"));
            } else {
                throw new IllegalStateException(String.format("Invalid type for %s. Must be an array, %s, %s or %s.",
                        TransformType.SIZE, Collection.class.getName(), Map.class.getName(), CharSequence.class.getName()));
            }

        } else {
            throw new IllegalStateException(String.format("Invalid transform type: %s", transformTypes));
        }
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

    private JVarDeclaration createReturnType(final MessageMethod messageMethod, final JBlock body, final JCall format) {
        boolean callInitCause = false;
        final ThrowableType returnType = messageMethod.returnType().throwableReturnType();
        final JType type = $t(returnType.name());
        // Import once more as the throwable return type may be different than the actual return type
        sourceFile._import(type);
        final JCall result = type._new();
        final JVarDeclaration resultField = body.var(FINAL, type, "result", result);
        if (returnType.useConstructionParameters()) {
            for (Parameter param : returnType.constructionParameters()) {
                if (param.isMessageMethod()) {
                    result.arg(format);
                } else {
                    result.arg($v(param.name()));
                }
            }
            callInitCause = messageMethod.hasCause() && !returnType.causeSetInConstructor();
        } else if (returnType.hasStringAndThrowableConstructor() && messageMethod.hasCause()) {
            result.arg(format).arg($v(messageMethod.cause().name()));
        } else if (returnType.hasThrowableAndStringConstructor() && messageMethod.hasCause()) {
            result.arg($v(messageMethod.cause().name())).arg(format);
        } else if (returnType.hasStringConstructor()) {
            result.arg(format);
            if (messageMethod.hasCause()) {
                callInitCause = true;
            }
        } else if (returnType.hasThrowableConstructor() && messageMethod.hasCause()) {
            result.arg($v(messageMethod.cause().name()));
        } else if (returnType.hasStringAndThrowableConstructor() && !messageMethod.hasCause()) {
            result.arg(format).arg(NULL);
        } else if (returnType.hasThrowableAndStringConstructor() && !messageMethod.hasCause()) {
            result.arg(NULL).arg(format);
        } else if (messageMethod.hasCause()) {
            callInitCause = true;
        }
        // Assign the result field the result value
        if (callInitCause) {
            body.add($v(resultField).call("initCause").arg($v(messageMethod.cause().name())));
        }

        // Get the @Property or @Properties annotation values
        addDefultProperties(messageMethod, ElementHelper.getAnnotations(messageMethod, Properties.class, Property.class), body, $v(resultField), false);
        addDefultProperties(messageMethod, ElementHelper.getAnnotations(messageMethod, Fields.class, Field.class), body, $v(resultField), true);

        // Remove this caller from the stack trace
        final JType arrays = $t(Arrays.class);
        sourceFile._import(arrays);
        final JVarDeclaration st = body.var(FINAL, $t(StackTraceElement.class).array(), "st", $v(resultField).call("getStackTrace"));
        body.add($v(resultField).call("setStackTrace").arg(arrays.call("copyOfRange").arg($v(st)).arg(JExpr.ONE).arg($v(st).field("length"))));

        // Add any suppressed messages
        final Set<Parameter> suppressed = messageMethod.parametersAnnotatedWith(Suppressed.class);
        for (Parameter p : suppressed) {
            if (p.isArray() || p.isVarArgs()) {
                final String name = String.format("$%sVar", p.name());
                // TODO (jrp) the " " can be removed after an upgrade to JDeparser2; this is a workaround for a formatting bug
                body.forEach(0, JTypes.typeOf(((ArrayType) p.asType()).getComponentType()), " " + name, $v(p.name()))
                        .block(Braces.REQUIRED)
                        .add($v(resultField).call("addSuppressed").arg($v(name)));
            } else if (p.isAssignableFrom(Collection.class)) {
                final String name = String.format("$%sVar", p.name());
                body.add($v(p.name()).call("forEach").arg(JExprs.lambda().param(name).body($v(resultField).call("addSuppressed").arg($v(name)))));
            } else {
                body.add($v(resultField).call("addSuppressed").arg($v(p.name())));
            }
        }
        return resultField;
    }

    protected final void addThrownTypes(final MessageMethod messageMethod, final JMethodDef jMethod) {
        for (ThrowableType thrownType : messageMethod.thrownTypes()) {
            jMethod._throws(thrownType.name());
        }
    }

    /**
     * Adds the parameter to the method returning the reference to the parameter.
     *
     * @param method the method to add the parameter to
     * @param param  the parameter to add
     *
     * @return the reference to the parameter on the method
     */
    protected JParamDeclaration addMethodParameter(final JMethodDef method, final Parameter param) {
        final JType paramType = JTypes.typeOf(param.asType());
        if (!param.isPrimitive()) {
            sourceFile._import(paramType);
        }
        return method.param(FINAL, paramType, param.name());
    }

    private void addDefultProperties(final MessageMethod messageMethod, final Collection<AnnotationMirror> annotations, final JBlock body, final JAssignableExpr resultField, final boolean field) {
        for (AnnotationMirror propertyAnnotation : annotations) {
            String name = null;
            AnnotationValue annotationValue = null;
            for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : propertyAnnotation.getElementValues().entrySet()) {
                final ExecutableElement attribute = entry.getKey();
                final AnnotationValue attributeValue = entry.getValue();
                if ("name".contentEquals(attribute.getSimpleName())) {
                    name = String.valueOf(attributeValue.getValue());
                } else {
                    annotationValue = attributeValue;
                }
            }
            if (name == null) {
                throw new ProcessingException(messageMethod, propertyAnnotation, "The name attribute is required for the annotation.");
            }
            if (annotationValue == null || annotationValue.getValue() == null) {
                throw new ProcessingException(messageMethod, propertyAnnotation, "A value for one of the default attributes is required.");
            }
            final JExpr resultValue = annotationValue.accept(JExprAnnotationValueVisitor.INSTANCE, null);
            if (resultValue == null) {
                final Object value = annotationValue.getValue();
                throw new ProcessingException(messageMethod, propertyAnnotation, annotationValue, "Could not resolve value type for %s (%s)", value, value.getClass());
            }
            if (field) {
                body.add(resultField.field(name).assign(resultValue));
            } else {
                body.add(resultField.call("set" + Character.toUpperCase(name.charAt(0)) + name.substring(1)).arg(resultValue));
            }
        }
    }

    private static class JExprAnnotationValueVisitor extends SimpleAnnotationValueVisitor8<JExpr, Void> {
        private static final JExprAnnotationValueVisitor INSTANCE = new JExprAnnotationValueVisitor();
        @Override
        public JExpr visitBoolean(final boolean b, final Void v) {
            return b ? JExpr.TRUE : JExpr.FALSE;
        }

        @Override
        public JExpr visitByte(final byte b, final Void v) {
            return JExprs.decimal(b).cast(JType.BYTE);
        }

        @Override
        public JExpr visitChar(final char c, final Void v) {
            return JExprs.ch(c);
        }

        @Override
        public JExpr visitDouble(final double d, final Void v) {
            // TODO (jrp) this is fixed in upstream JDeparser, but for now we need to use this
            // setterValue = JExprs.hex(d);
            return JExprs.$v(String.format("%ad", d));
        }

        @Override
        public JExpr visitFloat(final float f, final Void v) {
            return JExprs.hex(f);
        }

        @Override
        public JExpr visitInt(final int i, final Void v) {
            return JExprs.decimal(i);
        }

        @Override
        public JExpr visitLong(final long i, final Void v) {
            return JExprs.decimal(i);
        }

        @Override
        public JExpr visitShort(final short s, final Void v) {
            return JExprs.decimal((int) s).cast(JType.SHORT);
        }

        @Override
        public JExpr visitString(final String s, final Void v) {
            return JExprs.str(s);
        }

        @Override
        public JExpr visitType(final TypeMirror t, final Void v) {
            return JExprs.$v(t.toString()+ ".class");
        }
    }
}
