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
