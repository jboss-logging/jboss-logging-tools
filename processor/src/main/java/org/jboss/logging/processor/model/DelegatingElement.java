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

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.type.TypeMirror;

/**
 * A delegating {@link Element} interface. All methods are invoked on the {@linkplain #getDelegate() delegate element}
 * by default.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public interface DelegatingElement extends Element {

    /**
     * The element to delegate the default methods to.
     *
     * @return the delegate
     */
    Element getDelegate();

    @Override
    default TypeMirror asType() {
        return getDelegate().asType();
    }

    @Override
    default ElementKind getKind() {
        return getDelegate().getKind();
    }

    @Override
    default Set<Modifier> getModifiers() {
        return getDelegate().getModifiers();
    }

    @Override
    default Name getSimpleName() {
        return getDelegate().getSimpleName();
    }

    @Override
    default Element getEnclosingElement() {
        return getDelegate().getEnclosingElement();
    }

    @Override
    default List<? extends Element> getEnclosedElements() {
        return getDelegate().getEnclosedElements();
    }

    @Override
    default <A extends Annotation> A getAnnotation(final Class<A> annotationType) {
        return getDelegate().getAnnotation(annotationType);
    }

    @Override
    default List<? extends AnnotationMirror> getAnnotationMirrors() {
        return getDelegate().getAnnotationMirrors();
    }

    @Override
    default <R, P> R accept(final ElementVisitor<R, P> v, final P p) {
        return getDelegate().accept(v, p);
    }

    @Override
    default <A extends Annotation> A[] getAnnotationsByType(final Class<A> annotationType) {
        return getDelegate().getAnnotationsByType(annotationType);
    }

    /**
     * Checks whether or not the annotation is present on the element.
     *
     * @param annotation the annotation to check for
     *
     * @return {@code true} if the annotation is present, otherwise {@code false}
     */
    default boolean isAnnotatedWith(Class<? extends Annotation> annotation) {
        return getAnnotation(annotation) != null;
    }
}
