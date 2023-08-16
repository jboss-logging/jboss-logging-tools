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

import javax.lang.model.element.Element;
import javax.lang.model.element.Name;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;

/**
 * A delegating {@link TypeElement} interface. All methods are invoked on the {@linkplain #getDelegate() delegate element}
 * by default.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public interface DelegatingTypeElement extends TypeElement, DelegatingElement {

    @Override
    TypeElement getDelegate();

    @Override
    default TypeMirror asType() {
        return getDelegate().asType();
    }

    @Override
    default List<? extends Element> getEnclosedElements() {
        return getDelegate().getEnclosedElements();
    }

    @Override
    default NestingKind getNestingKind() {
        return getDelegate().getNestingKind();
    }

    @Override
    default Name getQualifiedName() {
        return getDelegate().getQualifiedName();
    }

    @Override
    default Name getSimpleName() {
        return getDelegate().getSimpleName();
    }

    @Override
    default TypeMirror getSuperclass() {
        return getDelegate().getSuperclass();
    }

    @Override
    default List<? extends TypeMirror> getInterfaces() {
        return getDelegate().getInterfaces();
    }

    @Override
    default List<? extends TypeParameterElement> getTypeParameters() {
        return getDelegate().getTypeParameters();
    }

    @Override
    default Element getEnclosingElement() {
        return getDelegate().getEnclosingElement();
    }
}
