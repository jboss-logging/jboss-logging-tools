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
