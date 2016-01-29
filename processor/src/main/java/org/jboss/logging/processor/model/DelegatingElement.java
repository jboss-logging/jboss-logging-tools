/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General default License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General default License for more details.
 *
 * You should have received a copy of the GNU Lesser General default
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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
}
