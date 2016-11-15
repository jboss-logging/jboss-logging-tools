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

package org.jboss.logging.processor.model;

import java.util.Set;

/**
 * Date: 27.09.2011
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public interface ThrowableType extends MessageObject, MessageObjectType, Comparable<ThrowableType> {

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
    @Override
    String name();
}
