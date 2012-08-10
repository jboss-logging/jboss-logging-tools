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

package org.jboss.logging.processor.apt;

import java.text.MessageFormat;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

/**
 * An interface used to extract information about the logging annotations.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public interface Annotations {

    /**
     * A simple annotation to allow an annotation to be ignored.
     */
    @interface Void {
    }

    /**
     * The message format type.
     */
    enum FormatType {

        MESSAGE_FORMAT('v', MessageFormat.class, "format"),
        PRINTF('f', String.class, "format"),
        NO_FORMAT(Character.MIN_VALUE, Void.class, null);
        private final char logType;
        private final Class<?> formatClass;
        private final String staticMethod;

        private FormatType(final char logType, final Class<?> formatClass, final String staticMethod) {
            this.logType = logType;
            this.formatClass = formatClass;
            this.staticMethod = staticMethod;
        }

        public char logType() {
            return logType;
        }

        public Class<?> formatClass() {
            return formatClass;
        }

        public String staticMethod() {
            return staticMethod;
        }
    }

    /**
     * Returns the method format type.
     *
     * @param method the method with the Message annotation.
     *
     * @return the format type of the message or {@code null} if the format type
     *         was not found.
     */
    FormatType messageFormat(ExecutableElement method);

    /**
     * The project code from the interface.
     *
     * @param intf the interface to find the project code on.
     *
     * @return the project code or {@code null} if one was not found.
     */
    String projectCode(TypeElement intf);

    /**
     * Checks to see if the parameter has a cause annotation.
     *
     * @param param the parameter to check
     *
     * @return {@code true} if the method has a cause annotation, otherwise {@code false}
     */
    boolean hasCauseAnnotation(VariableElement param);

    /**
     * Checks to see if the parameter has a field annotation.
     *
     * @param param the parameter to check
     *
     * @return {@code true} if the method has a field annotation, otherwise {@code false}
     */
    boolean hasFieldAnnotation(VariableElement param);

    /**
     * Checks to see if the parameter has a logging class annotation.
     *
     * @param param the parameter to check
     *
     * @return {@code true} if the method has a logging class annotation, otherwise {@code false}
     */
    boolean hasLoggingClassAnnotation(VariableElement param);


    /**
     * Checks to see if the method is annotated with the message annotation.
     *
     * @param method the method to check
     *
     * @return {@code true} if the annotation was found, otherwise {@code false}
     */
    boolean hasMessageAnnotation(ExecutableElement method);

    /**
     * Checks to see if the method has a message id.
     *
     * @param method the method to check.
     *
     * @return {@code true} if the message id was found, otherwise {@code false}.
     */
    boolean hasMessageId(ExecutableElement method);

    /**
     * Checks to see if the parameter has a parameter annotation.
     *
     * @param param the parameter to check
     *
     * @return {@code true} if the method has a parameter annotation, otherwise {@code false}
     */
    boolean hasParamAnnotation(VariableElement param);

    /**
     * Checks to see if the parameter has a property annotation.
     *
     * @param param the parameter to check
     *
     * @return {@code true} if the method has a property annotation, otherwise {@code false}
     */
    boolean hasPropertyAnnotation(VariableElement param);


    /**
     * Checks to see if the method should inherit the message id from a different method if applicable.
     *
     * @param method the method to check.
     *
     * @return {@code true} if the message id should be inherited, otherwise {@code false}.
     */
    boolean inheritsMessageId(ExecutableElement method);

    /**
     * Checks to see if the method is a logger method.
     *
     * @param method the method to check
     *
     * @return {@code true} if the method is a logger method, otherwise {@code false}
     */
    boolean isLoggerMethod(ExecutableElement method);

    /**
     * Checks the {@link TypeElement element} to see it's a message bundle.
     *
     * @param element the interface element
     *
     * @return {@code true} if the interface is a message bundle
     */
    boolean isMessageBundle(TypeElement element);

    /**
     * Checks the {@link TypeElement element} to see it's a message logger.
     *
     * @param element the interface element
     *
     * @return {@code true} if the interface is a message logger
     */
    boolean isMessageLogger(TypeElement element);

    /**
     * Checks the annotation to see if it's valid to be processed.
     * <p/>
     * By default all annotations are processed.
     *
     * @param annotation the annotation to check
     *
     * @return {@code true} if the annotation should be processed, otherwise {@code false}
     */
    boolean isValidInterfaceAnnotation(TypeElement annotation);

    /**
     * Returns the name of the {@code @FormatWith} class.
     *
     * @param param the parameter element
     *
     * @return the name of the class
     */
    String getFormatWithAnnotationName(VariableElement param);

    /**
     * Returns the name of the {@code @MessageLogger} class.
     *
     * @param element the interface element
     *
     * @return the name of the class
     */
    String getMessageLoggerAnnotationName(TypeElement element);

    /**
     * Returns the message id.
     *
     * @param method the method to check.
     *
     * @return the message id or 0 if one was not found.
     */
    int messageId(ExecutableElement method);

    /**
     * Returns the message value for the method.
     *
     * @param method the method to check.
     *
     * @return the message for the method, if no method found {@code null} is
     *         returned.
     */
    String messageValue(ExecutableElement method);

    /**
     * Returns the logger method name to use or an empty string if the method is not a logger method.
     *
     * @param formatType the format type for the method.
     *
     * @return the name of the logger method or an empty string.
     */
    String loggerMethod(FormatType formatType);

    /**
     * Returns the log level enum. For example Logger.Level.INFO.
     *
     * @param method the method used to determine the log method.
     *
     * @return the log level.
     */
    String logLevel(ExecutableElement method);

    /**
     * Returns the target field or method name for the annotated parameter. If the parameter is not annotated with
     * either {@link org.jboss.logging.annotations.Field} or
     * {@link org.jboss.logging.annotations.Property} an empty string should be returned.
     * <p/>
     * If the parameter is annotated with {@link org.jboss.logging.annotations.Property}, the name should
     * be prepended with {@code set}. For example a property name of {@code value} should return {@code setValue}.
     * <p/>
     * If the annotation does not have a defined value, the parameter name should be returned.
     *
     * @param param the parameter to check for the annotation.
     *
     * @return the field, method name or an empty string.
     */
    String targetName(VariableElement param);
}
