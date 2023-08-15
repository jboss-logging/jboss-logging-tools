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

package org.jboss.logging.processor.apt;

import static org.jboss.logging.processor.util.ElementHelper.isAnnotatedWith;
import static org.jboss.logging.processor.util.Objects.HashCodeBuilder;
import static org.jboss.logging.processor.util.Objects.ToStringBuilder;
import static org.jboss.logging.processor.util.Objects.areEqual;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.Field;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.Message.Format;
import org.jboss.logging.annotations.Param;
import org.jboss.logging.annotations.Pos;
import org.jboss.logging.annotations.Property;
import org.jboss.logging.annotations.Suppressed;
import org.jboss.logging.annotations.Transform;
import org.jboss.logging.processor.model.LoggerMessageMethod;
import org.jboss.logging.processor.model.MessageMethod;
import org.jboss.logging.processor.model.Parameter;
import org.jboss.logging.processor.model.ReturnType;
import org.jboss.logging.processor.model.ThrowableType;
import org.jboss.logging.processor.util.Comparison;
import org.jboss.logging.processor.util.ElementHelper;
import org.jboss.logging.processor.util.Expressions;

/**
 * Date: 29.07.2011
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
final class MessageMethodBuilder {

    private static final String MESSAGE_METHOD_SUFFIX = "$str";
    private final List<ExecutableElement> methods;
    private final ProcessingEnvironment processingEnv;
    private final Properties expressionProperties;

    private MessageMethodBuilder(final ProcessingEnvironment processingEnv, final Properties expressionProperties) {
        this.processingEnv = processingEnv;
        this.expressionProperties = expressionProperties;
        methods = new LinkedList<>();
    }

    MessageMethodBuilder add(final Collection<ExecutableElement> methods) {
        this.methods.addAll(methods);
        return this;
    }

    Set<MessageMethod> build() {
        final Elements elements = processingEnv.getElementUtils();
        final Set<MessageMethod> result = new LinkedHashSet<>();
        for (ExecutableElement elementMethod : methods) {
            final AptMessageMethod resultMethod = createMessageMethod(elements, elementMethod);
            resultMethod.inheritsMessage = inheritsMessage(methods, elementMethod);
            resultMethod.message = findMessage(methods, elementMethod);
            resultMethod.isOverloaded = isOverloaded(methods, elementMethod);
            for (TypeMirror thrownType : elementMethod.getThrownTypes()) {
                resultMethod.thrownTypes.add(ThrowableTypeFactory.of(processingEnv, thrownType));
            }

            // Create a list of parameters
            for (Parameter parameter : ParameterFactory.of(processingEnv, resultMethod.method)) {
                resultMethod.add(parameter);
            }
            // Check to see if the method is overloaded
            if (resultMethod.isOverloaded()) {
                resultMethod.messageMethodName = resultMethod.name() + resultMethod.formatParameterCount() + MESSAGE_METHOD_SUFFIX;
                resultMethod.translationKey = resultMethod.name() + "." + resultMethod.formatParameterCount();
            } else {
                resultMethod.messageMethodName = resultMethod.name() + MESSAGE_METHOD_SUFFIX;
                resultMethod.translationKey = resultMethod.name();
            }
            // Set the return type
            resultMethod.returnType = ReturnTypeFactory.of(processingEnv, elementMethod.getReturnType(), resultMethod);
            result.add(resultMethod);
        }
        return Collections.unmodifiableSet(result);
    }

    private MessageMethod.Message findMessage(final Collection<ExecutableElement> methods, final ExecutableElement method) {
        AptMessage result = null;
        Message message = method.getAnnotation(Message.class);
        if (message != null) {
            result = new AptMessage(message, expressionProperties);
            result.hasId = hasMessageId(message);
            result.inheritsId = message.id() == Message.INHERIT;
            if (result.inheritsId()) {
                result.id = findMessageId(methods, method);
                if (result.id > 0) {
                    result.hasId = true;
                }
            } else {
                result.id = message.id();
            }
        } else {
            final Collection<ExecutableElement> allMethods = findByName(methods, method);
            for (ExecutableElement m : allMethods) {
                message = m.getAnnotation(Message.class);
                if (message != null) {
                    result = new AptMessage(message, expressionProperties);
                    result.hasId = hasMessageId(message);
                    result.inheritsId = message.id() == Message.INHERIT;
                    if (result.inheritsId()) {
                        result.id = findMessageId(methods, m);
                        if (result.id > 0) {
                            result.hasId = true;
                        }
                    } else {
                        result.id = message.id();
                    }
                    break;
                }
            }
        }
        return result;
    }

    private int findMessageId(final Collection<ExecutableElement> methods, final ExecutableElement method) {
        int result = -2;
        final Collection<ExecutableElement> allMethods = findByName(methods, method.getSimpleName());
        for (ExecutableElement m : allMethods) {
            final Message message = m.getAnnotation(Message.class);
            if (message != null) {
                if (message.id() != Message.INHERIT) {
                    result = message.id();
                }
            }
        }
        return result;
    }

    private boolean hasMessageId(final Message message) {
        return message != null && (message.id() != Message.NONE && message.id() != Message.INHERIT);
    }

    private Collection<ExecutableElement> findByName(final Collection<ExecutableElement> methods, final ExecutableElement method) {
        final Name methodName = method.getSimpleName();
        final List<ExecutableElement> result = new ArrayList<>();
        final int paramCount = parameterCount(method.getParameters());
        for (ExecutableElement m : methods) {
            if (methodName.equals(m.getSimpleName()) && parameterCount(m.getParameters()) == paramCount) {
                result.add(m);
            }
        }
        return result;
    }


    /**
     * Returns a collection of methods with the same name.
     *
     * @param methods    the methods to process.
     * @param methodName the method name to findByName.
     *
     * @return a collection of methods with the same name.
     */
    private Collection<ExecutableElement> findByName(final Collection<ExecutableElement> methods, final Name methodName) {
        final List<ExecutableElement> result = new ArrayList<>();
        for (ExecutableElement method : methods) {
            if (methodName.equals(method.getSimpleName())) {
                result.add(method);
            }
        }
        return result;
    }

    /**
     * Checks to see if the method has or inherits a {@link org.jboss.logging.annotations.Message}
     * annotation.
     *
     * @param methods the method to search.
     * @param method  the method to check.
     *
     * @return {@code true} if the method has or inherits a message annotation, otherwise {@code false}.
     */

    private boolean inheritsMessage(final Collection<ExecutableElement> methods, final ExecutableElement method) {
        if (isAnnotatedWith(method, Message.class)) {
            return false;
        }
        final Collection<ExecutableElement> allMethods = findByName(methods, method.getSimpleName());
        for (ExecutableElement m : allMethods) {
            if (isAnnotatedWith(m, Message.class)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks to see if the method is overloaded. An overloaded method has a different parameter count based on the
     * format parameters only. Parameters annotated with {@link org.jboss.logging.annotations.Cause} or
     * {@link org.jboss.logging.annotations.Param}
     * are not counted.
     *
     * @param methods the method to search.
     * @param method  the method to check.
     *
     * @return {@code true} if the method is overloaded, otherwise {@code false}.
     */
    private boolean isOverloaded(final Collection<ExecutableElement> methods, final ExecutableElement method) {
        final Collection<ExecutableElement> allMethods = findByName(methods, method.getSimpleName());
        for (ExecutableElement m : allMethods) {
            if (method.getSimpleName().equals(m.getSimpleName()) && parameterCount(method.getParameters()) != parameterCount(m.getParameters())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the number of parameters excluding the {@link org.jboss.logging.annotations.Cause} parameter
     * and any {@link org.jboss.logging.annotations.Param} parameters if found.
     *
     * @param params the parameters to get the count for.
     *
     * @return the number of parameters.
     */
    private int parameterCount(final Collection<? extends VariableElement> params) {
        int result = params.size();
        boolean hasCause = false;
        for (VariableElement param : params) {
            if (isAnnotatedWith(param, Param.class) || isAnnotatedWith(param, Field.class) ||
                    isAnnotatedWith(param, Property.class) || isAnnotatedWith(param, Suppressed.class)) {
                --result;
            }
            if (isAnnotatedWith(param, Cause.class)) {
                hasCause = true;
            }
        }
        return (result - (hasCause ? 1 : 0));
    }

    static MessageMethodBuilder create(final ProcessingEnvironment processingEnv) {
        return create(processingEnv, new Properties());
    }

    static MessageMethodBuilder create(final ProcessingEnvironment processingEnv, final Properties expressionProperties) {
        return new MessageMethodBuilder(processingEnv, expressionProperties);
    }

    private static AptMessageMethod createMessageMethod(final Elements elements, final ExecutableElement elementMethod) {
        if (elementMethod.getAnnotation(LogMessage.class) == null) {
            return new AptMessageMethod(elements, elementMethod);
        }
        return new AptLoggerMessageMethod(elements, elementMethod);
    }

    /**
     * An implementation for the MessageMethod interface.
     */
    private static class AptMessageMethod implements MessageMethod {

        final Elements elements;
        final Map<TypeMirror, Set<Parameter>> parameters;
        final Set<ThrowableType> thrownTypes;
        final ExecutableElement method;
        ReturnType returnType;
        Parameter cause;
        boolean inheritsMessage;
        boolean isOverloaded;
        Message message;
        String messageMethodName;
        String translationKey;
        int formatParameterCount;

        /**
         * Private constructor for the
         *
         * @param elements the elements utility.
         * @param method   the method to describe.
         */
        AptMessageMethod(final Elements elements, final ExecutableElement method) {
            this.elements = elements;
            this.method = method;
            inheritsMessage = false;
            isOverloaded = false;
            parameters = new HashMap<>();
            thrownTypes = new LinkedHashSet<>();
            formatParameterCount = 0;
        }

        void add(final Parameter parameter) {
            if (parameter.isFormatParameter()) {
                if (parameter.isAnnotatedWith(Pos.class)) {
                    formatParameterCount += parameter.getAnnotation(Pos.class).value().length;
                } else {
                    formatParameterCount++;
                }
            }
            if (parameters.containsKey(null)) {
                parameters.get(null).add(parameter);
            } else {
                final Set<Parameter> any = new LinkedHashSet<>();
                any.add(parameter);
                parameters.put(null, any);
            }
            for (AnnotationMirror a : parameter.getAnnotationMirrors()) {
                if (parameters.containsKey(a.getAnnotationType())) {
                    parameters.get(a.getAnnotationType()).add(parameter);
                } else {
                    final Set<Parameter> set = new LinkedHashSet<>();
                    set.add(parameter);
                    parameters.put(a.getAnnotationType(), set);
                }
            }
            if (parameter.isAnnotatedWith(Cause.class)) {
                cause = parameter;
            }
        }

        @Override
        public String name() {
            return method.getSimpleName().toString();
        }

        @Override
        public Set<Parameter> parameters() {
            if (parameters.containsKey(null)) {
                return Collections.unmodifiableSet(parameters.get(null));
            }
            return Collections.emptySet();
        }

        @Override
        public Set<Parameter> parametersAnnotatedWith(final Class<? extends Annotation> annotation) {
            final TypeElement type = ElementHelper.toTypeElement(elements, annotation);
            return parameters.containsKey(type.asType()) ? Collections.unmodifiableSet(parameters.get(type.asType())) : Collections.emptySet();
        }

        @Override
        public ReturnType returnType() {
            return returnType;
        }

        @Override
        public Set<ThrowableType> thrownTypes() {
            return Collections.unmodifiableSet(thrownTypes);
        }

        @Override
        public Message message() {
            return message;
        }

        @Override
        public boolean inheritsMessage() {
            return inheritsMessage;
        }

        @Override
        public String messageMethodName() {
            return messageMethodName;
        }

        @Override
        public String translationKey() {
            return translationKey;
        }

        @Override
        public boolean hasCause() {
            return cause != null;
        }

        @Override
        public boolean isOverloaded() {
            return isOverloaded;
        }

        @Override
        public Parameter cause() {
            return cause;
        }

        @Override
        public int formatParameterCount() {
            return formatParameterCount;
        }

        @Override
        public int hashCode() {
            return HashCodeBuilder.builder()
                    .add(name())
                    .add(parameters())
                    .add(returnType()).toHashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof AptMessageMethod)) {
                return false;
            }
            final AptMessageMethod other = (AptMessageMethod) obj;
            return areEqual(name(), other.name()) &&
                    areEqual(parameters, parameters) &&
                    areEqual(returnType, other.returnType);
        }

        @Override
        public String toString() {
            return ToStringBuilder.of(this)
                    .add("name", name())
                    .add("returnType", returnType())
                    .add("parameters", parameters()).toString();
        }

        @Override
        public ExecutableElement getDelegate() {
            return method;
        }

        @Override
        public int compareTo(final MessageMethod o) {
            int result = name().compareTo(o.name());
            result = (result != Comparison.EQUAL) ? result : returnType.name().compareTo(o.returnType().name());
            // Size does matter
            result = (result != Comparison.EQUAL) ? result : parameters().size() - o.parameters().size();
            if (result == Comparison.EQUAL) {
                // Check element by element
                final Iterator<Parameter> params1 = parameters().iterator();
                final Iterator<Parameter> params2 = o.parameters().iterator();
                while (params1.hasNext()) {
                    if (params2.hasNext()) {
                        final Parameter param1 = params1.next();
                        final Parameter param2 = params2.next();
                        result = param1.compareTo(param2);
                    } else {
                        result = Comparison.GREATER;
                    }
                    // Short circuit
                    if (result != Comparison.EQUAL) break;
                }
            }
            return result;
        }

        @Override
        public String getComment() {
            return elements.getDocComment(method);
        }
    }

    private static class AptLoggerMessageMethod extends AptMessageMethod implements LoggerMessageMethod {


        /**
         * Private constructor for the
         *
         * @param elements the elements utility.
         * @param method   the method to describe.
         */
        AptLoggerMessageMethod(final Elements elements, final ExecutableElement method) {
            super(elements, method);
        }

        @Override
        public String loggerMethod() {
            switch (message.format()) {
                case MESSAGE_FORMAT:
                    return "logv";
                case NO_FORMAT:
                    return "log";
                case PRINTF:
                    return "logf";
                default:
                    // Should never be hit
                    return "log";
            }
        }

        @Override
        public String logLevel() {
            final LogMessage logMessage = method.getAnnotation(LogMessage.class);
            return logMessage.level().name();
        }

        @Override
        public boolean wrapInEnabledCheck() {
            if (!parametersAnnotatedWith(Transform.class).isEmpty()) {
                return true;
            }
            for (Parameter parameter : parameters()) {
                if (parameter.isSubtypeOf(Supplier.class)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public String toString() {
            return ToStringBuilder.of(this)
                    .add("name", name())
                    .add("returnType", returnType())
                    .add("parameters", parameters())
                    .add("loggerMethod", loggerMethod()).toString();
        }
    }

    private static class AptMessage implements MessageMethod.Message {

        private final Message message;
        private final String messageValue;
        private int id;
        private boolean hasId;
        private boolean inheritsId;

        private AptMessage(final Message message, final Properties expressionProperties) {
            this.message = message;
            if (expressionProperties.isEmpty()) {
                this.messageValue = message.value();
            } else {
                this.messageValue = Expressions.resolve(expressionProperties, message.value());
            }
        }

        @Override
        public int id() {
            return id;
        }

        @Override
        public boolean hasId() {
            return hasId;
        }

        @Override
        public boolean inheritsId() {
            return inheritsId;
        }

        @Override
        public String value() {
            return messageValue;
        }

        @Override
        public Format format() {
            return message.format();
        }

        @Override
        public String toString() {
            return ToStringBuilder.of(this)
                    .add("hasId", hasId)
                    .add("id", id())
                    .add("inheritsId", inheritsId)
                    .add("value", value())
                    .add("formatType", format()).toString();
        }
    }
}
