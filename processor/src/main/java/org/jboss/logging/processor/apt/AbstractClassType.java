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

package org.jboss.logging.processor.apt;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import org.jboss.logging.processor.model.ClassType;
import org.jboss.logging.processor.util.ElementHelper;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
abstract class AbstractClassType implements ClassType {
    protected final ProcessingEnvironment processingEnv;
    protected final Elements elements;
    protected final Types types;
    protected final TypeMirror typeMirror;

    AbstractClassType(final ProcessingEnvironment processingEnv, final TypeMirror typeMirror) {
        this.processingEnv = processingEnv;
        this.elements = processingEnv.getElementUtils();
        this.types = processingEnv.getTypeUtils();
        this.typeMirror = typeMirror;
    }

    AbstractClassType(final ProcessingEnvironment processingEnv, final Element element) {
        this.processingEnv = processingEnv;
        this.elements = processingEnv.getElementUtils();
        this.types = processingEnv.getTypeUtils();
        this.typeMirror = element.asType();
    }

    @Override
    public final boolean isAssignableFrom(final Class<?> type) {
        return types.isAssignable(types.erasure(toType(type)), types.erasure(this.typeMirror));
    }

    @Override
    public final boolean isSubtypeOf(final Class<?> type) {
        return types.isSubtype(types.erasure(this.typeMirror), toType(type));
    }

    @Override
    public final boolean isSameAs(final Class<?> type) {
        return types.isSameType(types.erasure(this.typeMirror), toType(type));
    }

    /**
     * Creates a {@link TypeMirror} from a class type.
     *
     * @param type the type to create the {@link TypeMirror} for
     *
     * @return the {@code TypeMirror} to represent the type
     */
    private TypeMirror toType(final Class<?> type) {
        return types.erasure(ElementHelper.toType(elements, type));
    }
}
