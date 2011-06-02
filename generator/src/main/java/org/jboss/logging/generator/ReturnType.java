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

import org.jboss.logging.generator.util.ElementHelper;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import java.util.List;

/**
 * Describes information about the return type.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class ReturnType {

    private final TypeMirror returnType;

    private boolean defaultConstructor = false;

    private boolean stringConstructor = false;

    private boolean throwableConstructor = false;

    private boolean stringAndThrowableConstructor = false;

    private boolean throwableAndStringConstructor = false;

    /**
     * Creates a new descriptor that is not primitive.
     *
     * @param returnType the class name of the return type.
     */
    private ReturnType(final TypeMirror returnType) {
        this.returnType = returnType;
    }

    /**
     * Creates a new descriptor that is not primitive.
     *
     * @param returnType the class name of the return type.
     * @param typeUtil   the type utilities.
     *
     * @return the return type descriptor.
     */
    public static ReturnType of(final TypeMirror returnType, final Types typeUtil) {
        final ReturnType result = new ReturnType(returnType);
        result.init(typeUtil);
        return result;
    }

    /**
     * Initializes the object.
     *
     * @param typeUtil the type utilities.
     */
    private void init(final Types typeUtil) {
        if (!returnType.getKind().isPrimitive() && returnType.getKind() != TypeKind.VOID) {
            final Element element = typeUtil.asElement(returnType);
            final List<ExecutableElement> constructors = ElementFilter.constructorsIn(element.getEnclosedElements());
            for (ExecutableElement constructor : constructors) {
                // Only allow public constructors
                if (!constructor.getModifiers().contains(Modifier.PUBLIC)) {
                    continue;
                }
                List<? extends VariableElement> params = constructor.getParameters();
                switch (params.size()) {
                    case 0:
                        defaultConstructor = true;
                        break;
                    case 1:
                        if (ElementHelper.isAssignableFrom(params.get(0).asType(), String.class)) {
                            stringConstructor = true;
                        } else if (ElementHelper.isAssignableFrom(Throwable.class, params.get(0).asType())) {
                            throwableConstructor = true;
                        }
                        break;
                    case 2:
                        if (ElementHelper.isAssignableFrom(params.get(0).asType(), String.class)
                                && ElementHelper.isAssignableFrom(Throwable.class, params.get(1).asType())) {
                            stringAndThrowableConstructor = true;
                        } else if (ElementHelper.isAssignableFrom(Throwable.class, params.get(0).asType())
                                && ElementHelper.isAssignableFrom(params.get(1).asType(), String.class)) {
                            throwableAndStringConstructor = true;
                        }
                        break;
                }
            }
        }
    }

    /**
     * Indicates whether or not the return type is a primitive.
     *
     * @return {@code true} if a primitive, otherwise {@code false}.
     */
    public boolean isPrimitive() {
        return returnType.getKind().isPrimitive();
    }

    /**
     * Returns a string version of the return type.
     *
     * @return a string version of the return type.
     */
    public String getReturnTypeAsString() {
        return returnType.toString();
    }

    /**
     * If the return type is a default constructor {@code true} is returned.
     * Otherwise {@code false} is returned.
     *
     * @return {@code true} if the throwable has a default constructor, otherwise {@code false}.
     */
    public boolean hasDefaultConstructor() {
        return defaultConstructor;
    }

    /**
     * If the return type is a constructor and has a {@link java.lang.String}
     * and {@link java.lang.Throwable} constructor, {@code true} is returned.
     * Otherwise {@code false} is returned.
     *
     * @return {@code true} if the throwable has both a string and throwable
     *         constructor, otherwise {@code false}.
     */
    public boolean hasStringAndThrowableConstructor() {
        return stringAndThrowableConstructor;
    }

    /**
     * If the return type is a constructor and has a {@link java.lang.String}
     * constructor, {@code true} is returned. Otherwise {@code false} is
     * returned.
     *
     * @return {@code true} if the throwable has a string constructor, otherwise
     *         {@code false}.
     */
    public boolean hasStringConstructor() {
        return stringConstructor;
    }

    /**
     * If the return type is a constructor and has a {@link java.lang.Throwable}
     * and {@link java.lang.String} constructor, {@code true} is returned.
     * Otherwise {@code false} is returned.
     *
     * @return {@code true} if the throwable has both a throwable and string
     *         constructor, otherwise {@code false}.
     */
    public boolean hasThrowableAndStringConstructor() {
        return throwableAndStringConstructor;
    }

    /**
     * If the return type is a constructor and has a {@link java.lang.Throwable}
     * constructor, {@code true} is returned. Otherwise {@code false} is
     * returned.
     *
     * @return {@code true} if the throwable has a throwable constructor,
     *         otherwise {@code false}.
     */
    public boolean hasThrowableConstructor() {
        return throwableConstructor;
    }

    /**
     * Checks to see if the return type is an exception, extends Throwable.
     *
     * @return {@code true} if the return type is an exception, otherwise
     *         {@code false}.
     */
    public boolean isException() {
        return ElementHelper.isAssignableFrom(Throwable.class, returnType);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((returnType == null) ? 0 : returnType.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ReturnType)) {
            return false;
        }
        final ReturnType other = (ReturnType) obj;
        if ((this.returnType == null) ? other.returnType != null : this.returnType.equals(other.returnType)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();
        result.append(getClass().getName()).
                append("(returnType=").
                append(returnType).
                append(", stringConstructor=").
                append(stringConstructor).
                append(", throwableConstructor=").
                append(throwableConstructor).
                append(", stringAndThrowableConstructor=").
                append(stringAndThrowableConstructor).
                append(", throwableAndStringConstructor=").
                append(throwableAndStringConstructor).
                append(")");
        return result.toString();
    }
}
