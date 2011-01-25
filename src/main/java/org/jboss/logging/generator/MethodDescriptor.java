/*
 *  JBoss, Home of Professional Open Source Copyright 2010, Red Hat, Inc., and
 *  individual contributors by the @authors tag. See the copyright.txt in the
 *  distribution for a full listing of individual contributors.
 * 
 *  This is free software; you can redistribute it and/or modify it under the
 *  terms of the GNU Lesser General Public License as published by the Free
 *  Software Foundation; either version 2.1 of the License, or (at your option)
 *  any later version.
 * 
 *  This software is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 *  details.
 * 
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this software; if not, write to the Free Software Foundation,
 *  Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 */
package org.jboss.logging.generator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.jboss.logging.Cause;
import org.jboss.logging.LogMessage;
import org.jboss.logging.Message;
import org.jboss.logging.util.ElementHelper;

/**
 *
 * @author James R. Perkins (jrp)
 */
public class MethodDescriptor implements Iterable<MethodDescriptor>,
        Comparable<MethodDescriptor> {

    /**
     * A descriptor for the method parameters.
     */
    public static final class MethodParameter implements
            Comparable<MethodParameter> {

        private final VariableElement param;

        private final String fullType;

        /**
         * Only allow construction from within the parent class.
         * 
         * @param fullType the full type name.
         * @param param    the parameter.
         */
        private MethodParameter(final String fullType, final VariableElement param) {
            this.fullType = fullType;
            this.param = param;
        }

        /**
         * Checks the parameter and returns {@code true} if this is a cause
         * parameter, otherwise {@code false}.
         * 
         * @return {@code true} if the parameter is annotated with 
         *         {@link org.jboss.logging.Cause}, otherwise {@code false}.
         */
        public boolean isCause() {
            return ElementHelper.isAnnotatedWith(param, ElementHelper.CAUSE_ANNOTATION);
        }

        /**
         * The full type name of the parameter. For example 
         * {@code java.lang.String} if the parameter is a string. If the
         * parameter is a primitive, the primitive name is returned.
         * 
         * @return the qualified type of the parameter.
         */
        public String fullType() {
            return fullType;
        }

        /**
         * The variable name of the parameter.
         * 
         * @return the variable name of the parameter.
         */
        public String name() {
            return param.getSimpleName().toString();
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int hash = 1;
            hash = prime * hash + ((fullType == null) ? 0 : fullType.hashCode());
            hash = prime * hash + ((param == null) ? 0 : param.hashCode());
            return hash;
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof MethodParameter)) {
                return false;
            }
            final MethodParameter other = (MethodParameter) obj;
            if ((this.param == null) ? (other.param != null) : !this.param.equals(other.param)) {
                return false;
            }
            if ((this.fullType == null) ? (other.fullType != null) : !this.fullType.equals(other.fullType)) {
                return false;
            }
            return true;
        }

        @Override
        public int compareTo(final MethodParameter other) {
            int result = this.fullType.compareTo(other.fullType);
            result = (result != 0) ? result : this.name().compareTo(other.name());
            return result;
        }
    }

    private static List<MethodDescriptor> descriptors;

    private MethodParameter cause;

    private ReturnType returnType;

    private LogMessage logMessage;

    private Message message;

    private ExecutableElement method;

    private final List<MethodParameter> parameters;

    /**
     * Private constructor for the 
     */
    private MethodDescriptor() {
        this.parameters = new ArrayList<MethodParameter>();
    }

    protected static MethodDescriptor create(final Elements elementUtil, final Types typeUtil, Collection<ExecutableElement> methods) {
        final MethodDescriptor result = new MethodDescriptor();
        descriptors = new ArrayList<MethodDescriptor>();
        boolean first = true;
        for (ExecutableElement method : methods) {
            MethodDescriptor current = null;
            if (first) {
                current = result;
                first = false;
            } else {
                current = new MethodDescriptor();
            }
            current.init(typeUtil, method);
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
        stringBuilder.append(message());
        stringBuilder.append(",logMessae=");
        stringBuilder.append(logMessage());
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

    /**
     * Returns the Message annotation associated with this method.
     * 
     * @return the message annotation.
     */
    public Message message() {
        return message;
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
    public LogMessage logMessage() {
        return logMessage;
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
        return ElementHelper.isLoggerMethod(method);
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
    private void init(final Types typeUtil, ExecutableElement method) {
        this.method = method;
        // Find the annotations
        Message message = method.getAnnotation(Message.class);
        LogMessage logMessage = method.getAnnotation(LogMessage.class);
        this.returnType = ReturnType.of(method.getReturnType(), typeUtil);

        final Collection<MethodDescriptor> methodDescriptors = find(this.name());
        // Locate the first message with a non-null message
        for (MethodDescriptor methodDesc : methodDescriptors) {
            if (methodDesc.message() != null && message == null) {
                message = methodDesc.message();
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
            if (param.getAnnotation(Cause.class) != null) {
                this.cause = new MethodParameter(typeUtil.asElement(param.asType()).toString(), param);
            }
            if (param.asType().getKind().isPrimitive()) {
                this.parameters.add(new MethodParameter(param.asType().toString(), param));
            } else {
                this.parameters.add(new MethodParameter(typeUtil.asElement(param.asType()).toString(), param));
            }
        }
        // Setup the global variables for the result
        this.logMessage = logMessage;
        this.message = message;
    }
}
