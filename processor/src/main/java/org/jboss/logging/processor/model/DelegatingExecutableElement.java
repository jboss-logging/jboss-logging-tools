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
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;

/**
 * A delegating {@link ExecutableElement} interface. All methods are invoked on the {@linkplain #getDelegate() delegate element}
 * by default.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public interface DelegatingExecutableElement extends ExecutableElement, DelegatingElement {

    @Override
    ExecutableElement getDelegate();

    @Override
    default TypeMirror asType() {
        return getDelegate().asType();
    }

    @Override
    default List<? extends TypeParameterElement> getTypeParameters() {
        return getDelegate().getTypeParameters();
    }

    @Override
    default TypeMirror getReturnType() {
        return getDelegate().getReturnType();
    }

    @Override
    default List<? extends VariableElement> getParameters() {
        return getDelegate().getParameters();
    }

    @Override
    default TypeMirror getReceiverType() {
        return getDelegate().getReceiverType();
    }

    @Override
    default boolean isVarArgs() {
        return getDelegate().isVarArgs();
    }

    @Override
    default boolean isDefault() {
        return getDelegate().isDefault();
    }

    @Override
    default List<? extends TypeMirror> getThrownTypes() {
        return getDelegate().getThrownTypes();
    }

    @Override
    default AnnotationValue getDefaultValue() {
        return getDelegate().getDefaultValue();
    }

    @Override
    default Name getSimpleName() {
        return getDelegate().getSimpleName();
    }
}
