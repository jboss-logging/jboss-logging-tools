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
