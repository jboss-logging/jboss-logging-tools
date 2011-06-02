/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat Inc., and individual contributors
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
package org.jboss.logging.generator;

import org.jboss.logging.generator.Annotations.FormatType;
import org.jboss.logging.generator.util.ElementHelper;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.jboss.logging.generator.LoggingTools.annotations;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class MethodDescriptor implements Comparable<MethodDescriptor> {

    private static final String MESSAGE_METHOD_SUFFIX = "$str";

    private MethodParameter cause;
    private boolean isOverloaded;
    private ReturnType returnType;
    private Message message;
    private String messageMethodName;
    private final ExecutableElement method;
    private final List<MethodParameter> parameters;
    private String translationKey;

    /**
     * Private constructor for the
     *
     * @param method the method to describe.
     */
    private MethodDescriptor(final ExecutableElement method) {
        this.method = method;
        parameters = new ArrayList<MethodParameter>();
    }

    /**
     * Create the method descriptor.
     *
     * @param parent the method descriptor collection.
     * @param method the method to describe.
     *
     * @return the method descriptor created for the methods.
     */
    protected static MethodDescriptor of(final MethodDescriptors parent, final ExecutableElement method) {
        final MethodDescriptor result = new MethodDescriptor(method);
        result.init(parent);
        // Check to see if the method is overloaded
        result.isOverloaded = parent.isOverloaded(method);
        if (result.isOverloaded) {
            result.messageMethodName = result.name() + result.relativeParameterCount() + MESSAGE_METHOD_SUFFIX;
            result.translationKey = result.name() + "." + result.relativeParameterCount();
        } else {
            result.messageMethodName = result.name() + MESSAGE_METHOD_SUFFIX;
            result.translationKey = result.name();
        }
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((method == null) ? 0 : method.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof MethodDescriptor)) {
            return false;
        }
        final MethodDescriptor other = (MethodDescriptor) obj;
        if (method == null) {
            if (other.method != null) {
                return false;
            }
        } else if (!method.equals(other.method)) {
            return false;
        }
        return true;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(getClass().getSimpleName());
        stringBuilder.append("(name=");
        stringBuilder.append(name());
        stringBuilder.append(",message=");
        stringBuilder.append(message);
        stringBuilder.append(",loggerMethod=");
        stringBuilder.append(loggerMethod());
        stringBuilder.append(")");
        return stringBuilder.toString();
    }

    /**
     * This comparison does not directly adhere to the general contract in that it does not honor the {@code equals()}
     * and {@code hashCode()} contracts.
     * <p/>
     * The intention of this is for sorting only. Probably better suited for {@code java.lang.Comparator}, but for state
     * safety comparing is safer internally.
     *
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(final MethodDescriptor o) {
        int c = method.getSimpleName().toString().compareTo(o.method.getSimpleName().toString());
        c = (c != 0) ? c : method.getKind().compareTo(o.method.getKind());
        c = (c != 0) ? c : (method.getParameters().size() - o.method.getParameters().size());
        // Compare the parameters
        if (c == 0) {
            final List<? extends VariableElement> params = method.getParameters();
            for (int i = 0; i < params.size(); i++) {
                final VariableElement var1 = params.get(i);
                final VariableElement var2 = o.method.getParameters().get(i);
                // TypeMirror.toString() should return the qualified type, example java.lang.String
                c = var1.asType().toString().compareTo(var2.asType().toString());
                if (c != 0) {
                    break;
                }
            }
        }
        return c;
    }


    /**
     * Returns {@code true} if the method has a message id, otherwise {@code false},
     *
     * @return {@code true} if the method has a message id, otherwise {@code false},
     */
    public boolean hasMessageId() {
        return message.hasId();
    }

    /**
     * Returns the message format.
     *
     * @return the message format.
     */
    public FormatType messageFormat() {
        return message.format();
    }

    /**
     * Returns the Message annotation associated with this method.
     *
     * @return the message annotation.
     */
    public String messageValue() {
        return message.value();
    }

    /**
     * Returns the id of the message.
     *
     * @return the id of the message.
     */
    public int messageId() {
        return message.id();
    }

    /**
     * Returns the name of the method used to retrieve the message.
     *
     * @return the name of the message method.
     */
    public String messageMethodName() {
        return messageMethodName;
    }

    /**
     * Returns the name of the key used in the translation files for the message translation.
     *
     * @return the name of the key in the translation files.
     */
    public String translationKey() {
        return translationKey;
    }

    /**
     * Returns the method name.
     *
     * @return the method name.
     */
    public String name() {
        return method.getSimpleName().toString();
    }

    /**
     * Returns {@code true} if there is a cause element, otherwise {@code false}
     * .
     *
     * @return {@code true} if there is a cause element, otherwise {@code false}
     */
    public boolean hasCause() {
        return cause != null;
    }

    /**
     * Returns {@code true} if the method is overloaded, otherwise {@code false}
     * .
     *
     * @return {@code true} if the method is overloaded, otherwise {@code false}
     */
    public boolean isOverloaded() {
        return isOverloaded;
    }

    /**
     * Returns the cause element if there is one, otherwise {@code null}.
     *
     * @return the cause element, otherwise {@code null}.
     */
    public MethodParameter cause() {
        return cause;
    }

    /**
     * Returns the return type descriptor.
     *
     * @return the return type descriptor.
     */
    public ReturnType returnType() {
        return returnType;
    }

    /**
     * Returns the LogMessage annotation associated with this method.
     *
     * @return the log message annotation
     */
    public String loggerMethod() {
        return LoggingTools.annotations().loggerMethod(method, message.format());
    }

    /**
     * Returns the log level parameter associated with the method.
     *
     * @return the log level annotation
     */
    public String logLevelParameter() {
        return LoggingTools.annotations().logLevel(method);
    }

    /**
     * Returns an unmodifiable collection of the parameters.
     *
     * @return a collection of the parameters.
     */
    public Collection<MethodParameter> parameters() {
        return Collections.unmodifiableCollection(parameters);
    }

    /**
     * Returns the number of all parameters for the method.
     *
     * @return the number of all parameters for the method.
     */
    public int parameterCount() {
        return parameters.size();
    }

    /**
     * Returns the number of parameters minus the cause parameter count for the method.
     *
     * @return the number of parameters minus the cause parameter count for the method.
     */
    public int relativeParameterCount() {
        return (hasCause() ? (parameters.size() - 1) : parameters.size());
    }

    /**
     * Returns {@code true} if this is a logger method, otherwise {@code false}.
     *
     * @return {@code true} if this is a logger method, otherwise {@code false}.
     */
    public boolean isLoggerMethod() {
        return ElementHelper.isAnnotatedWith(method, LoggingTools.annotations().logMessage());
    }

    /**
     * Initializes the instance.
     *
     * @param parent the parent collection of all method descriptors.
     */
    private void init(final MethodDescriptors parent) {
        // Find the annotations
        Message message = Message.of(annotations().messageId(method), annotations().hasMessageId(method),
                annotations().messageValue(method), annotations().messageFormat(method));
        this.returnType = ReturnType.of(method.getReturnType(), parent.typeUtil);

        final Collection<MethodDescriptor> methodDescriptors = parent.find(name());
        // Locate the first message with a non-null message
        for (MethodDescriptor methodDesc : methodDescriptors) {
            // Check for inherited message id's
            if (annotations().inheritsMessageId(method) && methodDesc.message.hasId()) {
                final Message current = message;
                message = Message.of(message.id(), message.hasId(), current.value(), current.format());
            }
            // If the message is not null, no need to process further.
            if (message.value() != null) {
                continue;
            }

            if (methodDesc.message.value() != null && message.value() == null &&
                    ElementHelper.parameterCount(method.getParameters()) == ElementHelper.parameterCount(methodDesc.method.getParameters())) {
                message = methodDesc.message;
            }
        }
        // Process through the collection and update any currently null
        // messages
        for (MethodDescriptor methodDesc : methodDescriptors) {
            // Check for inherited message id's
            if (annotations().inheritsMessageId(methodDesc.method) && message.hasId()) {
                final Message old = methodDesc.message;
                methodDesc.message = Message.of(message.id(), message.hasId(), old.value(), old.format());
            }
            if (methodDesc.message.value() == null) {
                methodDesc.message = message;
                parent.update(methodDesc);
            }
        }
        // Create a list of parameters
        for (VariableElement param : method.getParameters()) {
            if (param.getAnnotation(annotations().cause()) != null) {
                cause = new MethodParameter(parent.typeUtil.asElement(param.asType()).toString(), param);
            }
            String formatClass = null;
            // Format class may not yet be compiled, so get it in a roundabout way
            for (AnnotationMirror mirror : param.getAnnotationMirrors()) {
                final DeclaredType annotationType = mirror.getAnnotationType();
                if (annotationType.equals(parent.typeUtil.getDeclaredType(parent.elementUtil.getTypeElement(annotations().formatWith().getName())))) {
                    final AnnotationValue value = mirror.getElementValues().values().iterator().next();
                    formatClass = ((TypeElement) (((DeclaredType) value.getValue()).asElement())).getQualifiedName().toString();
                }
            }
            if (param.asType().getKind().isPrimitive()) {
                parameters.add(new MethodParameter(param.asType().toString(), param, formatClass));
            } else {
                parameters.add(new MethodParameter(parent.typeUtil.asElement(param.asType()).toString(), param, formatClass));
            }
        }
        // Setup the global variables for the result
        // this.logMessage = logMessage;
        this.message = message;
    }
}
