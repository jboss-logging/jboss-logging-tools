/*
 *  JBoss, Home of Professional Open Source Copyright 2011, Red Hat, Inc., and
 *  individual contributors by the @authors tag. See the copyright.txt in the
 *  distribution for a full listing of individual contributors.
 *
 *  This is free software; you can redistribute it and/or modify it under the
 *  terms of the GNU Lesser General Public License as published by the Free
 *  Software Foundation; either version 2.1 of the License, or (at your option)
 *  any later version.
 *
 *  This software is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 *  details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this software; if not, write to the Free Software Foundation,
 *  Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 *  site: http://www.fsf.org.
 */
package org.jboss.logging.generator;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a> - 20.Feb.2011
 */
public interface MethodParameter extends Comparable<MethodParameter>, MessageObject {

    /**
     * The full type name of the parameter. For example
     * {@code java.lang.String} if the parameter is a string. If the
     * parameter is a primitive, the primitive name is returned.
     *
     * @return the qualified type of the parameter.
     */
    String type();

    /**
     * The variable name of the parameter.
     *
     * @return the variable name of the parameter.
     */
    @Override
    String name();

    /**
     * Returns {@code true} if the type is an array, otherwise {@code false}.
     *
     * @return {@code true} if an array, otherwise {@code false}
     */
    boolean isArray();

    /**
     * Returns {@code true} if the type is a primitive type, otherwise {@code false}.
     *
     * @return {@code true} if primitive type, otherwise {@code false}
     */
    boolean isPrimitive();

    /**
     * Returns {@code true} if the parameter is a var args parameter, otherwise {@code false}.
     *
     * @return {@code true} if var args parameter, otherwise {@code false}.
     */
    boolean isVarArgs();

    /**
     * Checks the parameter and returns {@code true} if this is a cause parameter, otherwise {@code false}.
     *
     * @return {@code true} if the parameter is annotated with
     *         {@link org.jboss.logging.generator.Annotations#cause()}, otherwise {@code false}.
     */
    boolean isCause();

    /**
     * Checks the parameter and returns {@code true} if the parameter is a message parameter, e.g. the message that will
     * be logged or returned from a bundle. If it's not the message parameter {@code false} is returned.
     *
     * @return {@code true} if this is the message parameter (the message from the method), otherwise {@code false}.
     */
    boolean isMessage();

    /**
     * Checks the parameter and returns {@code true} if the parameter is to be used in the construction of the
     * exception, otherwise {@code false}.
     *
     * @return {@code true} if the parameter is annotated with {@link org.jboss.logging.generator.Annotations#param()},
     *         otherwise {@code false}.
     */
    boolean isParam();

    /**
     * The formatter class, or {@code null} if there is none.
     *
     * @return the formatter class
     */
    String getFormatterClass();

    /**
     * Returns the class if the parameter is annotated with {@link org.jboss.logging.generator.Annotations#param()}.
     * If the annotation is not present, {@code null} is returned.
     *
     * @return the parameter class or {@code null}.
     */
    Class<?> paramClass();

    /**
     * A convenience method for returning the type used to extract the information. This is not used internally and can
     * have any valid return type including {@code null}.
     * <p/>
     * For example, in an annotation processor implementation an {@link javax.lang.model.element.ExecutableElement}
     * might be returned.
     *
     * @return the raw object used to extract information.
     */
    //Object getRawType();
}
