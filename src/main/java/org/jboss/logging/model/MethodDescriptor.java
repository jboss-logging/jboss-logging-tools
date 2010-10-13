/*
 * JBoss, Home of Professional Open Source Copyright 2010, Red Hat, Inc., and
 * individual contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 * 
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.jboss.logging.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

import org.jboss.logging.Cause;
import org.jboss.logging.LogMessage;
import org.jboss.logging.Message;

/**
 * Stores information about methods.
 * 
 * @author James R. Perkins Jr. (jrp)
 * 
 */
public class MethodDescriptor implements Comparable<MethodDescriptor>,
        Iterable<MethodDescriptor> {

    private final Set<MethodDescriptor> descriptors;
    private VariableElement cause;
    private LogMessage logMessage;
    private Message message;
    private final ExecutableElement method;
    private final String name;
    private final List<VariableElement> parameters;
    private TypeMirror returnType;

    /**
     * Class constructor.
     */
    public MethodDescriptor() {
        this(null, null);
    }

    /**
     * Class constructor for singleton
     * 
     * @param methodDesc
     *            the current method descriptor.
     * @param method
     *            the method to add.
     */
    private MethodDescriptor(final MethodDescriptor methodDesc,
            final ExecutableElement method) {
        descriptors = new LinkedHashSet<MethodDescriptor>();
        this.method = method;
        name = (method == null) ? null : method.getSimpleName().toString();
        parameters = new ArrayList<VariableElement>();
        if (methodDesc != null) {
            descriptors.addAll(methodDesc.descriptors);
        }
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
     * Returns the method.
     * 
     * @return the method.
     */
    public ExecutableElement method() {
        return method;
    }

    /**
     * Returns the method name.
     * 
     * @return the method name.
     */
    public String name() {
        return name;
    }

    /**
     * Returns {@code true} if there is a cause element, otherwise {@code false}
     * .
     * 
     * @return {@code true} if there is a cause element, otherwise {@code false}
     */
    public boolean hasClause() {
        return cause != null;
    }

    /**
     * Returns the cause element if there is one, otherwise {@code null}.
     * 
     * @return the cause element, otherwise {@code null}.
     */
    public VariableElement cause() {
        return cause;
    }

    /**
     * Returns the the cause variable name as a string.
     * 
     * @return the cause variable name.
     */
    public String causeVarName() {
        return cause().getSimpleName().toString();
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
     * Returns a collections of the parameters.
     * 
     * @return a collection of the parameters.
     */
    public Collection<VariableElement> parameters() {
        return Collections.unmodifiableCollection(parameters);
    }

    /**
     * Returns the return type for the method.
     * 
     * @return the return type for the method.
     */
    public TypeMirror returnType() {
        return returnType;
    }

    /**
     * Returns the return type for the method in a string format.
     * 
     * @return the return type for the method.
     */
    public String returnTypeAsString() {
        return returnType.toString();
    }

    /**
     * Transforms the {@code method} into a method descriptor and creates a new
     * method descriptor.
     * 
     * @param method
     *            the method to process.
     * @return the method descriptor that was created.
     */
    public MethodDescriptor add(final ExecutableElement method) {
        final MethodDescriptor result = new MethodDescriptor(this, method);

        // Find the annotations
        Message message = method.getAnnotation(Message.class);
        LogMessage logMessage = method.getAnnotation(LogMessage.class);
        result.returnType = method.getReturnType();
        final Collection<MethodDescriptor> methodDescriptors = find(result
                .name());
        // Locate the first message with a non-null message
        for (MethodDescriptor methodDesc : methodDescriptors) {
            if (methodDesc.message() != null && message == null) {
                message = methodDesc.message();
            }
            if (methodDesc.logMessage() != null && logMessage == null) {
                logMessage = methodDesc.logMessage();
            }
            // If both the message and the log message are not null, we are
            // complete.
            if (message != null && logMessage != null) {
                break;
            }
        }
        // Process through the collection and update any currently null
        // message or log messages
        for (MethodDescriptor methodDesc : methodDescriptors) {
            boolean changed = false;
            if (methodDesc.logMessage() == null) {
                methodDesc.logMessage = logMessage;
                changed = true;
            }
            if (methodDesc.message() == null) {
                methodDesc.message = message;
                changed = true;
            }
            if (changed) {
                descriptors.remove(methodDesc);
                descriptors.add(methodDesc);
            }
        }
        // Create a list of parameters
        for (VariableElement param : method.getParameters()) {
            if (param.getAnnotation(Cause.class) != null) {
                result.cause = param;
            }
            result.parameters.add(param);
        }
        // Setup the global variables for the result
        result.logMessage = logMessage;
        result.message = message;
        // The new method descriptor must be added to itself
        result.descriptors.add(result);
        return result;
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
     * Returns a collection of all method descriptors.
     * 
     * @return a collections of all method descriptors.
     */
    public Collection<MethodDescriptor> allMethods() {
        return Collections.unmodifiableCollection(descriptors);
    }

    /**
     * Clears the iterator of all values.
     */
    public void clear() {
        descriptors.clear();
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
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof MethodDescriptor)) {
            return false;
        }
        MethodDescriptor other = (MethodDescriptor) obj;
        if (method == null) {
            if (other.method != null)
                return false;
        } else if (!method.equals(other.method))
            return false;
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
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<MethodDescriptor> iterator() {
        return descriptors.iterator();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(final MethodDescriptor o) {
        int c = this.method.getSimpleName().toString()
                .compareTo(o.method.getSimpleName().toString());
        c = (c != 0) ? c : this.method.getKind().compareTo(o.method.getKind());
        c = (c != 0) ? c : (this.method.getParameters().size() - o.method
                .getParameters().size());
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
}
