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

import static org.jboss.logging.processor.util.ElementHelper.typeToString;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import org.jboss.logging.processor.model.MessageObjectType;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
abstract class AbstractMessageObjectType implements MessageObjectType {
    protected final Elements elements;
    protected final Types types;
    protected final TypeMirror typeMirror;

    protected AbstractMessageObjectType(final Elements elements, final Types types, final TypeMirror typeMirror) {
        this.elements = elements;
        this.types = types;
        this.typeMirror = typeMirror;
    }

    protected AbstractMessageObjectType(final Elements elements, final Types types, final Element element) {
        this.elements = elements;
        this.types = types;
        this.typeMirror = element.asType();
    }

    @Override
    public String type() {
        return name();
    }

    @Override
    public final boolean isAssignableFrom(final Class<?> type) {
        final TypeMirror typeMirror = elements.getTypeElement(typeToString(type)).asType();
        return types.isAssignable(types.erasure(typeMirror), types.erasure(this.typeMirror));
    }

    @Override
    public final boolean isSubtypeOf(final Class<?> type) {
        final TypeMirror typeMirror = elements.getTypeElement(typeToString(type)).asType();
        return types.isSubtype(types.erasure(this.typeMirror), types.erasure(typeMirror));
    }

    @Override
    public final boolean isSameAs(final Class<?> type) {
        return type().equals(typeToString(type));
    }
}
