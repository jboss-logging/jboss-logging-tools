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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.jboss.logging.Annotations;
import org.jboss.logging.Annotations.FormatType;
import org.jboss.logging.util.ElementHelper;

/**
 *
 * @author James R. Perkins (jrp)
 */
public class MethodDescriptor implements Iterable<MethodDescriptor>,
        Comparable<MethodDescriptor> {

    private static List<MethodDescriptor> descriptors;
    private MethodParameter cause;
    private ReturnType returnType;
    private final Annotations annotations;
    private Message message;
    private String loggerMethod;
    private ExecutableElement method;
    private final List<MethodParameter> parameters;

    /**
     * Private constructor for the
     */
    private MethodDescriptor(final Annotations annotations) {
        this.parameters = new ArrayList<MethodParameter>();
        this.annotations = annotations;
    }

    protected static MethodDescriptor create(final Elements elementUtil, final Types typeUtil, Collection<ExecutableElement> methods,
            final Annotations annotations) {
        final MethodDescriptor result = new MethodDescriptor(annotations);
        descriptors = new ArrayList<MethodDescriptor>();
        boolean first = true;
        for (ExecutableElement method : methods) {
            MethodDescriptor current = null;
            if (first) {
                current = result;
                first = false;
            } else {
                current = new MethodDescriptor(annotations);
            }
            current.init(elementUtil, typeUtil, method);
            descriptors.add(current);
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
        stringBuilder.append(loggerMethod);
        stringBuilder.append(")");
        return stringBuilder.toString();
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(final MethodDescriptor o) {
        int c = this.method.getSimpleName().toString().compareTo(o.method.getSimpleName().toString());
        c = (c != 0) ? c : this.method.getKind().compareTo(o.method.getKind());
        c = (c != 0) ? c : (this.method.getParameters().size() - o.method.getParameters().size());
        // Compare the parameters
        if (c == 0) {
            List<? extends VariableElement> parms = this.method.getParameters();
            for (int i = 0; i < parms.size(); i++) {
                final VariableElement var1 = parms.get(i);
                final VariableElement var2 = o.method.getParameters().get(i);
                c = var1.getKind().compareTo(var2.getKind());
            }
        }
        return c;
    }

    @Override
    public Iterator<MethodDescriptor> iterator() {
        Collection<MethodDescriptor> result = null;
        if (descriptors == null) {
            result = Collections.emptyList();
        } else {
            result = Collections.unmodifiableCollection(descriptors);
        }
        return result.iterator();
    }

    public boolean hasMessageId() {
        return message.hasId();
    }

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
        return loggerMethod;
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
     * Returns {@code true} if this is a logger method, otherwise {@code false}.
     *
     * @return {@code true} if this is a logger method, otherwise {@code false}.
     */
    public boolean isLoggerMethod() {
        return ElementHelper.isAnnotatedWith(method, annotations.messageLogger());
    }

    /**
     * Returns a collection of method descriptors that match the method name.
     *
     * @param methodName
     *            the method name to search for.
     * @return a collection of method descriptors that match the method name.
     */
    public Collection<MethodDescriptor> find(final String methodName) {
        final Set<MethodDescriptor> result = new LinkedHashSet<MethodDescriptor>();
        for (MethodDescriptor methodDesc : descriptors) {
            if (methodName.equals(methodDesc.name())) {
                result.add(methodDesc);
            }
        }
        return result;
    }

    /**
     * Initializes the instance.
     *
     * @param typeUtil the type utilities for internal usage.
     * @param method   the method to process.
     */
    private void init(final Elements elementUtil, final Types typeUtil, final ExecutableElement method) {
        this.method = method;
        // Find the annotations
        Message message = Message.of(annotations.messageId(method), annotations.hasMessageId(method),
                annotations.messageValue(method), annotations.messageFormat(method));
        this.returnType = ReturnType.of(method.getReturnType(), typeUtil);

        final Collection<MethodDescriptor> methodDescriptors = find(this.name());
        // Locate the first message with a non-null message
        for (MethodDescriptor methodDesc : methodDescriptors) {
            if (methodDesc.message != null && message == null) {
                message = methodDesc.message;
            }
            // If both the message and the log message are not null, we are
            // complete.
            if (message != null) {
                break;
            }
        }
        // Process through the collection and update any currently null
        // messages
        for (MethodDescriptor methodDesc : methodDescriptors) {
            if (methodDesc.message == null) {
                methodDesc.message = message;
                descriptors.remove(methodDesc);
                descriptors.add(methodDesc);
            }
        }
        // Create a list of parameters
        for (VariableElement param : method.getParameters()) {
            if (param.getAnnotation(annotations.cause()) != null) {
                this.cause = new MethodParameter(annotations, typeUtil.asElement(param.asType()).toString(), param);
            }
            String formatClass = null;
            // Format class may not yet be compiled, so get it in a roundabout way
            for (AnnotationMirror mirror : param.getAnnotationMirrors()) {
                final DeclaredType annotationType = mirror.getAnnotationType();
                if (annotationType.equals(typeUtil.getDeclaredType(elementUtil.getTypeElement(annotations.formatWith().getName())))) {
                    final AnnotationValue value = mirror.getElementValues().values().iterator().next();
                    formatClass = ((TypeElement) (((DeclaredType) value.getValue()).asElement())).getQualifiedName().toString();
                }
            }
            if (param.asType().getKind().isPrimitive()) {
                this.parameters.add(new MethodParameter(annotations, param.asType().toString(), param, formatClass));
            } else {
                this.parameters.add(new MethodParameter(annotations, typeUtil.asElement(param.asType()).toString(), param, formatClass));
            }
        }
        // Setup the global variables for the result
        // this.logMessage = logMessage;
        this.message = message;
    }
}
