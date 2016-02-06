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
import java.util.Set;

import org.jboss.logging.annotations.Message.Format;

/**
 * Date: 29.07.2011
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public interface MessageMethod extends Comparable<MessageMethod>, JavaDocComment, DelegatingExecutableElement {

    /**
     * Returns the method name.
     *
     * @return the method name.
     */
    String name();

    /**
     * Returns the parameters for the method.
     *
     * @return the parameters for the method
     */
    Set<Parameter> parameters();

    /**
     * Returns an unmodifiable collection of the parameters specified by the parameter type or an empty set.
     *
     * @param annotation the annotation to get the parameters for
     *
     * @return a collection of the parameters or an empty set.
     */
    Set<Parameter> parametersAnnotatedWith(Class<? extends Annotation> annotation);

    /**
     * Returns the return type for the method.
     *
     * @return the return type for the method.
     */
    ReturnType returnType();

    /**
     * Returns a collection of throwable types the method throws. If the method throws no exceptions an empty
     * collection is returned.
     *
     * @return a collection of throwable types or an empty collection.
     */
    Set<ThrowableType> thrownTypes();

    /**
     * The {@link Message} to be used for the method.
     *
     * @return the message.
     */
    Message message();

    /**
     * Indicates whether the message was inherited from another message or not. If {@code true} is returned the
     * {@link Message} was inherited from a different method, otherwise {@code false}.
     * <p/>
     * <b>Note:</b> {@code false} does not indicate the method has a {@link org.jboss.logging.annotations.Message}
     * annotation.
     *
     * @return {@code true} if the message was inherited from a different method, otherwise {@code false}.
     */
    boolean inheritsMessage();

    /**
     * Returns the name of the method used to retrieve the message.
     *
     * @return the name of the message method.
     */
    String messageMethodName();

    /**
     * Returns the name of the key used in the translation files for the message translation.
     *
     * @return the name of the key in the translation files.
     */
    String translationKey();

    /**
     * Returns {@code true} if there is a cause element, otherwise {@code false}.
     *
     * @return {@code true} if there is a cause element, otherwise {@code false}
     */
    boolean hasCause();

    /**
     * Returns {@code true} if the method is overloaded, otherwise {@code false}
     * .
     *
     * @return {@code true} if the method is overloaded, otherwise {@code false}
     */
    boolean isOverloaded();

    /**
     * Returns the cause element if {@link #hasCause()} returns {@code true}, otherwise {@code null}.
     *
     * @return the cause element, otherwise {@code null}.
     */
    Parameter cause();

    /**
     * Returns the LogMessage annotation associated with this method only if {@link #isLoggerMethod()} returns
     * {@code true}.
     *
     * @return the log message annotation
     */
    String loggerMethod();

    /**
     * Returns the log level parameter associated with the method only if {@link #isLoggerMethod()} returns
     * {@code true}.
     *
     * @return the enum name of the {@linkplain org.jboss.logging.Logger.Level log level}
     */
    String logLevel();

    /**
     * Returns the number of parameters minus the cause parameter count for the method.
     *
     * @return the number of parameters minus the cause parameter count for the method.
     */
    int formatParameterCount();

    /**
     * Returns {@code true} if this is a logger method, otherwise {@code false}.
     *
     * @return {@code true} if this is a logger method, otherwise {@code false}.
     */
    boolean isLoggerMethod();

    /**
     * Represents a {@link org.jboss.logging.annotations.Message} annotation on a method.
     */
    interface Message {

        /**
         * The message id for the message to use. Any id less than 0 will be ignored.
         *
         * @return the message id.
         */
        int id();

        /**
         * Checks if the message has an id that was provided. Returns {@code true} if the message id was specified or
         * inherited, otherwise returns {@code false}.
         *
         * @return {@code true} if the message id was provided, otherwise {@code false}.
         */
        boolean hasId();

        /**
         * Checks if the message id was inherited. Returns {@code true} only if the message id is inherited, otherwise
         * {@code false} is returned.
         *
         * @return {@code true} if the message id was inherited, otherwise {@code false}.
         */
        boolean inheritsId();

        /**
         * A format string that can be used with the {@link #format()}.
         *
         * @return a format string.
         */
        String value();

        /**
         * The message format type for the message.
         *
         * @return the format type.
         */
        Format format();
    }
}
