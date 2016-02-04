/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

import java.lang.annotation.Annotation;
import javax.lang.model.AnnotatedConstruct;

/**
 * Date: 23.08.2011
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public interface ClassType extends AnnotatedConstruct {

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

    /**
     * Determines if this type is either the same as, or is a supertype of, the class represented by the {@code type}
     * parameter. If this type is assignable from the class {@code true} is returned, otherwise {@code false}.
     *
     * @param type the class type to check.
     *
     * @return {@code true} if this type is the same as or a superclass of the class, otherwise {@code false}.
     */
    boolean isAssignableFrom(Class<?> type);

    /**
     * Determines if this type is a subtype of the class represented by the {@code type} parameter. If this type is a
     * subtype of the class {@code true} is returned, otherwise {@code false}.
     *
     * @param type the class type to check.
     *
     * @return {@code true} if this type is a subtype of the class, otherwise {@code false}.
     */
    boolean isSubtypeOf(Class<?> type);

    /**
     * Determines if this type is the same type as the class represented by the {@code type} parameter. If this type is
     * the same type as the class {@code true} is returned, otherwise {@code false}.
     *
     * @param type the class type to check.
     *
     * @return {@code true} if this type is the same type as the class, otherwise {@code false}.
     */
    boolean isSameAs(Class<?> type);
}
