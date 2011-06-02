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

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.text.MessageFormat;

/**
 * Defines the annotations and annotation values used to generate the concrete classes from the annotated interfaces.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a> - 19.Feb.2011
 */
public interface Annotations {

    /**
     * The message format type.
     */
    public static enum FormatType {

        MESSAGE_FORMAT('v', MessageFormat.class, "format"),
        PRINTF('f', String.class, "format");
        private final char logType;
        private final Class<?> formatClass;
        private final String staticMethod;

        FormatType(final char logType, final Class<?> formatClass, final String staticMethod) {
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
     * Returns the cause annotation class.
     *
     * @return the cause annotation.
     */
    Class<? extends Annotation> cause();

    /**
     * Returns the format with annotation class.
     *
     * @return the format with annotation.
     */
    Class<? extends Annotation> formatWith();

    /**
     * Returns the log message annotation class.
     *
     * @return the log message annotation.
     */
    Class<? extends Annotation> logMessage();

    /**
     * Returns the message annotation class.
     *
     * @return the message annotation.
     */
    Class<? extends Annotation> message();

    /**
     * Returns the message bundle annotation class.
     *
     * @return the message bundle annotation.
     */
    Class<? extends Annotation> messageBundle();

    /**
     * Returns the message logger annotation class.
     *
     * @return the message logger annotation.
     */
    Class<? extends Annotation> messageLogger();

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
     * Checks to see if the method has a message id.
     *
     * @param method the method to check.
     *
     * @return {@code true} if the message id was found, otherwise {@code false}.
     */
    boolean hasMessageId(ExecutableElement method);

    /**
     * Checks to see if the method should inherit the message id from a different method if applicable.
     *
     * @param method the method to check.
     *
     * @return {@code true} if the message id should be inherited, otherwise {@code false}.
     */
    boolean inheritsMessageId(ExecutableElement method);


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
     * Returns the logger method name to use.
     *
     * @param method     the method used to determine the log method.
     * @param formatType the format type for the method.
     *
     * @return the name of the logger method.
     */
    String loggerMethod(ExecutableElement method, FormatType formatType);

    /**
     * Returns the log level enum. For example Logger.Level.INFO.
     *
     * @param method the method used to determine the log method.
     *
     * @return the log level.
     */
    String logLevel(final ExecutableElement method);

}
