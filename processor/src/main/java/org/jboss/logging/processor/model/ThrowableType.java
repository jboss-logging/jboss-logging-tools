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

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * Date: 27.09.2011
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public interface ThrowableType extends ClassType, Comparable<ThrowableType>, DelegatingElement {

    /**
     * Checks to see the throwable has a default constructor.
     *
     * @return {@code true} if the throwable has a default constructor, otherwise {@code false}.
     */
    boolean hasDefaultConstructor();

    /**
     * Checks to see if the throwable has a string and throwable ({@code Throwable(String, Throwable)}) constructor.
     *
     * @return {@code true} if the throwable has both a string and throwable constructor, otherwise {@code false}.
     */
    boolean hasStringAndThrowableConstructor();

    /**
     * Checks to see if the throwable has a string ({@code Throwable(String)}) constructor.
     * <p/>
     * If {@code true}, {@link Throwable#initCause(Throwable)} can be used to set the throwable.
     *
     * @return {@code true} if the throwable has a string constructor, otherwise {@code false}.
     */
    boolean hasStringConstructor();

    /**
     * Checks to see if the throwable has a throwable and string ({@code Throwable(Throwable, String)}) constructor.
     *
     * @return {@code true} if the throwable has both a throwable and string constructor, otherwise {@code false}.
     */
    boolean hasThrowableAndStringConstructor();

    /**
     * Checks to see if the throwable has a string and throwable ({@code Throwable(String, Throwable)}) constructor.
     *
     * @return {@code true} if the throwable has a throwable constructor, otherwise {@code false}.
     */
    boolean hasThrowableConstructor();

    /**
     * Checks to see if the throwable has and can use a custom constructor.
     * <p/>
     * If {@code true}, the constructor parameters can be retrieved from the {@link #constructionParameters()} method.
     *
     * @return {@code true} if the throwable has a custom constructor that can be used, otherwise {@code false}.
     */
    boolean useConstructionParameters();

    /**
     * Indicates whether or not the {@linkplain org.jboss.logging.annotations.Cause cause} was set in the constructor.
     *
     * @return {@code true} if the cause was set in the constructor, {@code false} if the
     *         {@link Throwable#initCause(Throwable)} should be executed
     */
    default boolean causeSetInConstructor() {
        return false;
    }

    /**
     * The parameters needed to construct the throwable, if not using the default constructor. If the default
     * constructor should be used an empty set should be returned.
     * <p/>
     * The order the set is returned is the order in which the parameters must be in for the constructor.
     *
     * @return a set of construction parameters or an empty set.
     */
    Set<Parameter> constructionParameters();

    /**
     * Checks if the throwable is a checked exception. If the throwable is a checked exception, {@code true} is
     * returned, otherwise {@code false}.
     *
     * @return {@code true} if the throwable is a checked exception, otherwise {@code false}.
     */
    boolean isChecked();

    /**
     * Returns the qualified class name of the return type.
     *
     * @return the qualified class name fo the return type.
     */
    String name();

    /**
     * The suggested types to create for the throwable type created.
     *
     * @return the suggested types to be created, or an empty set if this type itself should be created
     */
    default Collection<ThrowableType> suggestions() {
        return Collections.emptySet();
    }
}
