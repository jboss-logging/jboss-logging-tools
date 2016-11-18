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

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.jboss.logging.annotations.ValidIdRange;

/**
 * Date: 28.07.2011
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public interface MessageInterface extends Comparable<MessageInterface>, MessageObject, MessageObjectType, JavaDocComment {


    public enum AnnotatedType {
        /**
         * Indicates the interface is annotated with {@code @MessageBundle}
         */
        MESSAGE_BUNDLE,
        /**
         * Indicates the interface is annotated with {@code @MessageLogger}
         */
        MESSAGE_LOGGER,
        /**
         * Indicates the interface is not annotated with {@code MessageBundle} or {@code @MessageLogger}
         */
        NONE,
    }

    /**
     * Checks the interface to see if the {@link org.jboss.logging.BasicLogger logger interface} is being extended in
     * this interface.
     *
     * @return {@code true} if this interface extends the logger interface, otherwise {@code false}.
     */
    boolean extendsLoggerInterface();

    /**
     * A set of qualified interface names this interface extends or an empty set.
     *
     * @return a set of interface names or an empty set.
     */
    Set<MessageInterface> extendedInterfaces();

    /**
     * A collection of all the methods this interface needs to implement.
     *
     * @return a collection of methods.
     */
    Collection<MessageMethod> methods();

    /**
     * The project code for the message interface or {@code null} if {@link #getAnnotatedType()} returns {@link
     * AnnotatedType#NONE}.
     *
     * @return the project code or {@code null} if {@link #getAnnotatedType()} returns {@link AnnotatedType#NONE}
     */
    String projectCode();

    /**
     * The qualified name of the message interface.
     *
     * @return the qualified name.
     */
    @Override
    String name();

    /**
     * The package name of the message interface.
     *
     * @return the package name.
     */
    String packageName();

    /**
     * The name of the interface without the package.
     *
     * @return the simple interface name.
     */
    String simpleName();

    /**
     * The fully qualified class name to use for log methods. This will generally be the same result as {@link
     * #name()}.
     *
     * @return the fully qualified class name to use for logging.
     */
    String loggingFQCN();

    /**
     * Returns the annotation type on the interface.
     *
     * @return the annotated type
     */
    AnnotatedType getAnnotatedType();

    /**
     * Returns a list of {@link ValidIdRange valid id ranges}.
     *
     * @return a list of valid id ranges or an empty list
     */
    List<ValidIdRange> validIdRanges();

    /**
     * The length to pad the id with. A value of less than 0 indicates no padding.
     *
     * @return the length to pad the id with
     */
    int getIdLength();

    /**
     * Indicates whether or not an annotation is present on the message interface.
     *
     * @param annotation the annotation to check for
     *
     * @return {@code true} if the annotation is present, otherwise {@code false}
     */
    boolean isAnnotatedWith(Class<? extends Annotation> annotation);

    /**
     * Returns the annotation present on this interface. If the annotation is not present {@code null} is returned.
     *
     * @param annotation the annotation to check for
     * @param <A>        the annotation type
     *
     * @return the annotation or {@code null} if the annotation was not present on the interface
     */
    <A extends Annotation> A getAnnotation(final Class<A> annotation);
}
