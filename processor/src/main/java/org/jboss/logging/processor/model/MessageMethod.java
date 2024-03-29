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
     * Returns the number of parameters minus the cause parameter count for the method.
     *
     * @return the number of parameters minus the cause parameter count for the method.
     */
    int formatParameterCount();

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
