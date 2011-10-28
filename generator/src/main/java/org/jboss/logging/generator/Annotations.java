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

import java.lang.annotation.Annotation;
import java.text.MessageFormat;

/**
 * Defines the annotations and annotation values used to generate the concrete classes from the annotated interfaces.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a> - 19.Feb.2011
 */
public interface Annotations {

    /**
     * A simple annotation to allow an annotation to be ignored.
     */
    public @interface Void {
    }

    /**
     * The message format type.
     */
    public static enum FormatType {

        MESSAGE_FORMAT('v', MessageFormat.class, "format"),
        PRINTF('f', String.class, "format");
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
     * Returns the cause annotation class.
     *
     * @return the cause annotation.
     */
    Class<? extends Annotation> cause();

    /**
     * Returns the field annotation class.
     *
     * @return the field annotation.
     */
    Class<? extends Annotation> field();

    /**
     * Returns the format with annotation class.
     *
     * @return the format with annotation.
     */
    Class<? extends Annotation> formatWith();

    /**
     * Returns the logging class annotation class.
     *
     * @return the logging class annotation.
     */
    Class<? extends Annotation> loggingClass();

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
     * Returns the parameter annotation class.
     *
     * @return the parameter annotation.
     */
    Class<? extends Annotation> param();

    /**
     * Returns the property annotation class.
     *
     * @return the property annotation.
     */
    Class<? extends Annotation> property();

}
