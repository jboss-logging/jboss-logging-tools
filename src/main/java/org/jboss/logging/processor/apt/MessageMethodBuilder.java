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

package org.jboss.logging.processor.apt;

import static java.util.Collections.unmodifiableSet;
import static org.jboss.logging.processor.Tools.annotations;
import static org.jboss.logging.processor.util.ElementHelper.findByName;
import static org.jboss.logging.processor.util.ElementHelper.inheritsMessage;
import static org.jboss.logging.processor.util.ElementHelper.isOverloaded;
import static org.jboss.logging.processor.util.ElementHelper.parameterCount;
import static org.jboss.logging.processor.util.Objects.HashCodeBuilder;
import static org.jboss.logging.processor.util.Objects.ToStringBuilder;
import static org.jboss.logging.processor.util.Objects.areEqual;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import org.jboss.logging.processor.apt.Annotations.FormatType;
import org.jboss.logging.processor.intf.model.MessageMethod;
import org.jboss.logging.processor.intf.model.Parameter;
import org.jboss.logging.processor.intf.model.Parameter.ParameterType;
import org.jboss.logging.processor.intf.model.ReturnType;
import org.jboss.logging.processor.intf.model.ThrowableType;
import org.jboss.logging.processor.util.Comparison;

/**
 * Date: 29.07.2011
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
final class MessageMethodBuilder {

    private static final String MESSAGE_METHOD_SUFFIX = "$str";
    private final Annotations annotations;
    private final List<ExecutableElement> methods;
    private final Elements elements;
    private final Types types;

    private MessageMethodBuilder(final Elements elements, final Types types) {
        annotations = annotations();
        this.elements = elements;
        this.types = types;
        methods = new LinkedList<ExecutableElement>();
    }

    static MessageMethodBuilder create(final Elements elements, final Types types) {
        return new MessageMethodBuilder(elements, types);
    }

    MessageMethodBuilder add(final ExecutableElement method) {
        methods.add(method);
        return this;
    }

    Set<MessageMethod> build() {
        final Set<MessageMethod> result = new LinkedHashSet<MessageMethod>();
        for (ExecutableElement elementMethod : methods) {
            final AptMessageMethod resultMethod = new AptMessageMethod(elements, elementMethod, annotations);
            resultMethod.inheritsMessage = inheritsMessage(methods, elementMethod);
            resultMethod.message = findMessage(methods, elementMethod);
            resultMethod.isOverloaded = isOverloaded(methods, elementMethod);
            for (TypeMirror thrownType : elementMethod.getThrownTypes()) {
                resultMethod.thrownTypes.add(ThrowableTypeFactory.of(elements, types, thrownType));
            }

            // Create a list of parameters
            for (Parameter parameter : ParameterFactory.of(elements, types, resultMethod.method)) {
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
            resultMethod.returnType = ReturnTypeFactory.of(elements, types, elementMethod.getReturnType(), resultMethod);
            result.add(resultMethod);
        }
        return Collections.unmodifiableSet(result);
    }

    private MessageMethod.Message findMessage(final Collection<ExecutableElement> methods, final ExecutableElement method) {
        AptMessage result = null;
        if (annotations.hasMessageAnnotation(method)) {
            result = new AptMessage();
            result.hasId = annotations.hasMessageId(method);
            result.value = annotations.messageValue(method);
            result.formatType = annotations.messageFormat(method);
            result.inheritsId = annotations.inheritsMessageId(method);
            if (result.inheritsId()) {
                result.id = findMessageId(methods, method);
                if (result.id > 0) {
                    result.hasId = true;
                }
            } else {
                result.id = annotations.messageId(method);
            }
        } else {
            final Collection<ExecutableElement> allMethods = findByName(methods, method.getSimpleName(), parameterCount(method.getParameters()));
            for (ExecutableElement m : allMethods) {
                if (annotations.hasMessageAnnotation(m)) {
                    result = new AptMessage();
                    result.hasId = annotations.hasMessageId(m);
                    result.value = annotations.messageValue(m);
                    result.formatType = annotations.messageFormat(m);
                    result.inheritsId = annotations.inheritsMessageId(m);
                    if (result.inheritsId()) {
                        result.id = findMessageId(methods, m);
                        if (result.id > 0) {
                            result.hasId = true;
                        }
                    } else {
                        result.id = annotations.messageId(m);
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
            if (annotations.hasMessageAnnotation(m)) {
                if (!annotations.inheritsMessageId(m)) {
                    result = annotations.messageId(m);
                }
            }
        }
        return result;
    }


    /**
     * An implementation for the MessageMethod interface.
     */
    private static class AptMessageMethod implements MessageMethod {

        private final Annotations annotations;
        private final Elements elements;
        private ReturnType returnType;

        private Parameter cause;
        private boolean inheritsMessage;
        private boolean isOverloaded;
        private Message message;
        private final Map<ParameterType, Set<Parameter>> parameters;
        private final Set<ThrowableType> thrownTypes;
        private String messageMethodName;
        private final ExecutableElement method;
        private String translationKey;

        /**
         * Private constructor for the
         *
         * @param elements    the elements utility.
         * @param method      the method to describe.
         * @param annotations the annotations
         */
        AptMessageMethod(final Elements elements, final ExecutableElement method, final Annotations annotations) {
            this.annotations = annotations;
            this.elements = elements;
            this.method = method;
            inheritsMessage = false;
            isOverloaded = false;
            parameters = new EnumMap<ParameterType, Set<Parameter>>(ParameterType.class);
            thrownTypes = new LinkedHashSet<ThrowableType>();
        }

        void add(final Parameter parameter) {
            if (parameters.containsKey(ParameterType.ANY)) {
                parameters.get(ParameterType.ANY).add(parameter);
            } else {
                final Set<Parameter> any = new LinkedHashSet<Parameter>();
                any.add(parameter);
                parameters.put(ParameterType.ANY, any);
            }
            if (parameters.containsKey(parameter.parameterType())) {
                parameters.get(parameter.parameterType()).add(parameter);
            } else {
                final Set<Parameter> set = new LinkedHashSet<Parameter>();
                set.add(parameter);
                parameters.put(parameter.parameterType(), set);
            }
            if (parameter.parameterType() == ParameterType.CAUSE) {
                cause = parameter;
            }
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
        public String name() {
            return method.getSimpleName().toString();
        }

        @Override
        public Set<Parameter> parameters(final ParameterType parameterType) {
            if (parameters.containsKey(parameterType)) {
                return parameters.get(parameterType);
            }
            return Collections.emptySet();
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
        public ReturnType returnType() {
            return returnType;
        }

        @Override
        public String loggerMethod() {
            return annotations.loggerMethod(message.format());
        }

        @Override
        public String logLevel() {
            return annotations.logLevel(method);
        }

        @Override
        public int formatParameterCount() {
            if (parameters.containsKey(ParameterType.FORMAT)) {
                return parameters.get(ParameterType.FORMAT).size();
            }
            return 0;
        }

        @Override
        public boolean isLoggerMethod() {
            return annotations.isLoggerMethod(method);
        }

        @Override
        public Set<ThrowableType> thrownTypes() {
            return unmodifiableSet(thrownTypes);
        }

        @Override
        public int hashCode() {
            return HashCodeBuilder.builder()
                    .add(name())
                    .add(parameters(ParameterType.ANY))
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
                    .add("parameters", parameters(ParameterType.ANY))
                    .add("loggerMethod", loggerMethod()).toString();
        }

        @Override
        public ExecutableElement reference() {
            return method;
        }

        @Override
        public int compareTo(final MessageMethod o) {
            int result = name().compareTo(o.name());
            result = (result != Comparison.EQUAL) ? result : returnType.name().compareTo(o.returnType().name());
            // Size does matter
            result = (result != Comparison.EQUAL) ? result : parameters(ParameterType.ANY).size() - o.parameters(ParameterType.ANY).size();
            if (result == Comparison.EQUAL) {
                // Check element by element
                final Iterator<Parameter> params1 = parameters(ParameterType.ANY).iterator();
                final Iterator<Parameter> params2 = o.parameters(ParameterType.ANY).iterator();
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

    private static class AptMessage implements MessageMethod.Message {

        private boolean hasId;
        private int id;
        private boolean inheritsId;
        private String value;
        private FormatType formatType;

        private AptMessage() {
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
            return value;
        }

        @Override
        public FormatType format() {
            return formatType;
        }

        @Override
        public String toString() {
            return ToStringBuilder.of(this)
                    .add("hasId", hasId)
                    .add("id", id)
                    .add("inheritsId", inheritsId)
                    .add("value", value)
                    .add("formatType", formatType).toString();
        }
    }
}
