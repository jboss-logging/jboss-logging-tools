/*
 *  JBoss, Home of Professional Open Source Copyright 2010, Red Hat, Inc., and
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
package org.jboss.logging;

import java.util.Map;
import org.jboss.logging.util.TransformationUtil;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic.Kind;

/**
 * A logger for logging messages for annotation processors.
 *
 * @author James R. Perkins (jrp)
 */
public class ToolLogger {

    private static final String DEBUG_SWITCH_1 = "--debug";

    private static final String DEBUG_SWITCH_2 = "-D";

    private final Messager messager;

    private final String className;

    private final boolean debug;

    private ToolLogger(final String className, final Messager messager,
            final Map<String, String> options) {
        this.messager = messager;
        this.className = className;
        debug = (options.containsKey(DEBUG_SWITCH_1) || options.containsKey(DEBUG_SWITCH_2));
    }

    /**
     * Creates a new tool logger.
     *
     * @param generator the generator to create the logger for.
     * 
     * @return a new tool logger.
     */
    public static ToolLogger getLogger(final Class<?> clazz,
            final Messager messager, final Map<String, String> options) {
        final String className = clazz.getName();
        ToolLogger result = new ToolLogger(className, messager, options);
        return result;
    }

    private void log(final Kind kind, final Element element,
            final String messageFormat, Object... args) {
        final String message = className + ": " + String.format(messageFormat,
                args);
        if (element == null) {
            messager.printMessage(kind, message);
        } else {
            messager.printMessage(kind, message, element);
        }
    }

    private void log(final Kind kind, final Element element,
            final Throwable cause) {
        if (element == null) {
            messager.printMessage(kind, TransformationUtil.stackTraceToString(
                    cause));
        } else {
            messager.printMessage(kind, TransformationUtil.stackTraceToString(
                    cause), element);
        }
    }

    /**
     * Returns {@code true} if debugging is enabled, otherwise {@code false}.
     * <p>
     * It is not necessary to invoke this method before invoking {@code debug}
     * methods. The debug methods will only log messages if debugging is
     * enabled.
     * </p>
     *
     * @return {@code true} if debugging is enabled, otherwise {@code false}.
     */
    public boolean isDebugEnabled() {
        return debug;
    }

    /**
     * Prints a note message.
     *
     * @param message   the message to print.
     */
    public void note(final String message) {
        log(Kind.NOTE, null, message);
    }

    /**
     * Prints a formatted note message.
     *
     * @param messageFormat the message format.
     * @param args          the format arguments.
     */
    public void note(final String messageFormat, Object... args) {
        log(Kind.NOTE, null, messageFormat, args);
    }

    /**
     * Prints a formatted note message.
     *
     * @param messageFormat the message format.
     * @param element       the element to print with the note.
     * @param args          the format arguments.
     */
    public void note(final String messageFormat, final Element element,
            Object... args) {
        log(Kind.NOTE, element, messageFormat, args);
    }

    /**
     * Prints a debug message if debugging is enabled.
     *
     * @param message   the message to print.
     */
    public void debug(final String message) {
        if (debug) {
            other(message);
        }
    }

    /**
     * Prints a formatted debug message if debugging is enabled.
     *
     * @param messageFormat the message format.
     * @param args          the format arguments.
     */
    public void debug(final String messageFormat, Object... args) {
        if (debug) {
            other(messageFormat, args);
        }
    }

    /**
     * Prints a formatted debug message if debugging is enabled.
     *
     * @param messageFormat the message format.
     * @param element       the element to print with the note.
     * @param args          the format arguments.
     */
    public void debug(final String messageFormat, final Element element,
            Object... args) {
        if (debug) {
            other(messageFormat, element, args);
        }
    }

    /**
     * Prints a warning message.
     *
     * @param message   the message to print.
     */
    public void warn(final String message) {
        log(Kind.WARNING, null, message);
    }

    /**
     * Prints a formatted warning message.
     *
     * @param messageFormat the message format.
     * @param args          the format arguments.
     */
    public void warn(final String messageFormat, Object... args) {
        log(Kind.WARNING, null, messageFormat, args);
    }

    /**
     * Prints a formatted warning message.
     *
     * @param messageFormat the message format.
     * @param element       the element that caused the warning.
     * @param args          the format arguments.
     */
    public void warn(final String messageFormat, final Element element,
            Object... args) {
        log(Kind.WARNING, element, messageFormat, args);
    }

    /**
     * Prints a warning message.
     *
     * @param message   the message to print.
     */
    public void mandatoryWarning(final String message) {
        log(Kind.MANDATORY_WARNING, null, message);
    }

    /**
     * Prints a formatted warning message.
     *
     * @param messageFormat the message format.
     * @param args          the format arguments.
     */
    public void mandatoryWarning(final String messageFormat, Object... args) {
        log(Kind.MANDATORY_WARNING, null, messageFormat, args);
    }

    /**
     * Prints a formatted warning message.
     *
     * @param messageFormat the message format.
     * @param element       the element that caused the warning.
     * @param args          the format arguments.
     */
    public void mandatoryWarning(final String messageFormat,
            final Element element, Object... args) {
        log(Kind.MANDATORY_WARNING, element, messageFormat, args);
    }

    /**
     * Prints an error message.
     *
     * @param message   the message to print.
     */
    public void error(final String message) {
        log(Kind.ERROR, null, message);
    }

    /**
     * Prints a formatted error message.
     *
     * @param messageFormat the message format.
     * @param args          the format arguments.
     */
    public void error(final String messageFormat, Object... args) {
        log(Kind.ERROR, null, messageFormat, args);
    }

    /**
     * Prints a formatted error message.
     *
     * @param messageFormat the message format.
     * @param element       the element that caused the warning.
     * @param args          the format arguments.
     */
    public void error(final String messageFormat, final Element element,
            Object... args) {
        log(Kind.ERROR, element, messageFormat, args);
    }

    /**
     * Prints an error message.
     *
     * @param cause the cause of the error.
     */
    public void error(final Throwable cause) {
        log(Kind.ERROR, null, cause);
    }

    /**
     * Prints an error message.
     *
     * @param cause   the cause of the error.
     * @param element the element that caused the error.
     */
    public void error(final Throwable cause, final Element element) {
        log(Kind.ERROR, element, cause);
    }

    /**
     * Prints an error message.
     *
     * @param cause         the cause of the error.
     * @param messageFormat the message format.
     * @param args          the format arguments.
     */
    public void error(final Throwable cause, final String messageFormat,
            final Object... args) {
        warn(messageFormat, args);
        log(Kind.ERROR, null, cause);
    }

    /**
     * Prints an error message.
     *
     * @param cause         the cause of the error.
     * @param messageFormat the message format.
     * @param element       the element that caused the warning.
     * @param args          the format arguments.
     */
    public void error(final Throwable cause, final String messageFormat,
            final Element element, final Object... args) {
        warn(messageFormat, args);
        log(Kind.ERROR, element, cause);
    }

    /**
     * Prints a message that does not fit the other types.
     *
     * @param message   the message to print.
     */
    public void other(final String message) {
        log(Kind.OTHER, null, message);
    }

    /**
     * Prints a formatted message that does not fit the other types.
     *
     * @param messageFormat the message format.
     * @param args          the format arguments.
     */
    public void other(final String messageFormat, Object... args) {
        log(Kind.OTHER, null, messageFormat, args);
    }

    /**
     * Prints a formatted message that does not fit the other types.
     *
     * @param messageFormat the message format.
     * @param element       the element to print with the note.
     * @param args          the format arguments.
     */
    public void other(final String messageFormat, final Element element,
            Object... args) {
        log(Kind.OTHER, element, messageFormat, args);
    }
}
