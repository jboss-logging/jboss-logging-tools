/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016, Red Hat, Inc., and individual contributors
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

import java.util.List;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

import org.jboss.logging.processor.util.ElementHelper;

/**
 * Date: 29.07.2011
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public interface ReturnType extends ClassType, DelegatingElement {

    /**
     * Checks to see if the return type is an exception, extends Throwable or the value of a
     * {@link java.util.function.Supplier} is a Throwable type.
     *
     * @return {@code true} if the return type is an exception, otherwise {@code false}.
     *
     * @see #resolvedType()
     */
    boolean isThrowable();

    /**
     * Returns the qualified class name of the return type.
     *
     * @return the qualified class name fo the return type.
     */
    String name();

    /**
     * Returns the exception return type if {@link #isThrowable()} returns {@code true}. Otherwise {@code null} is
     * returned.
     *
     * @return an exception return type, otherwise {@code null}.
     */
    ThrowableType throwableReturnType();

    /**
     * Checks this {@linkplain #asType() type} to see if there are any
     * {@linkplain DeclaredType#getTypeArguments() type arguments}. If any type arguments are found the first type is
     * returned and assumed to be the resolved type. Otherwise this {@linkplain #asType() type} is returned.
     * <p>
     * This is useful for the {@link java.util.function.Supplier Supplier} return type.
     * </p>
     *
     * @return the resolved return type
     */
    default TypeMirror resolvedType() {
        final TypeMirror type = asType();
        final List<? extends TypeMirror> typeArgs = ElementHelper.getTypeArguments(type);
        if (typeArgs.isEmpty()) {
            return type;
        }
        // Assume the first type only
        return typeArgs.get(0);
    }
}
