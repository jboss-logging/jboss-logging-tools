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

import javax.lang.model.AnnotatedConstruct;

/**
 * Date: 23.08.2011
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public interface ClassType extends AnnotatedConstruct {

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
