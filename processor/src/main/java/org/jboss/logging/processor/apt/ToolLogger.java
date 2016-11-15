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

package org.jboss.logging.processor.apt;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic.Kind;

import org.jboss.logging.processor.model.DelegatingElement;

/**
 * A logger for logging messages for annotation processors.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 */
public final class ToolLogger {

    private final Messager messager;

    private final boolean isDebugEnabled;

    private ToolLogger(final Messager messager, final boolean isDebugEnabled) {
        this.messager = messager;
        this.isDebugEnabled = isDebugEnabled;
    }

    /**
     * Creates a new tool logger.
     *
     * @param processingEnv the processing environment
     *
     * @return a new tool logger
     */
    public static ToolLogger getLogger(final ProcessingEnvironment processingEnv) {
        String debug = processingEnv.getOptions().get(LoggingToolsProcessor.DEBUG_OPTION);
        boolean isDebugEnabled = Boolean.parseBoolean(debug);

        return new ToolLogger(processingEnv.getMessager(), isDebugEnabled);
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
        return isDebugEnabled;
    }

    /**
     * Prints a note message.
     *
     * @param element the element to print with the note.
     * @param message the message.
     */
    public void note(final Element element, final String message) {
        log(Kind.NOTE, element, message);
    }

    /**
     * Prints a formatted note message.
     *
     * @param element       the element to print with the note.
     * @param messageFormat the message format.
     * @param args          the format arguments.
     */
    public void note(final Element element, final String messageFormat, final Object... args) {
        log(Kind.NOTE, element, messageFormat, args);
    }

    /**
     * Prints a formatted debug message if debugging is enabled.
     *
     * @param messageFormat the message format.
     * @param args          the format arguments.
     */
    public void debug(final String messageFormat, final Object... args) {
        if (isDebugEnabled) {
            debug(null, messageFormat, args);
        }
    }

    /**
     * Prints a debug message.
     *
     * @param element the element to print with the note.
     * @param message the message.
     */
    public void debug(final Element element, final String message) {
        if (isDebugEnabled) {
            other(element, message);
        }
    }

    /**
     * Prints a formatted debug message if debugging is enabled.
     *
     * @param element       the element to print with the note.
     * @param messageFormat the message format.
     * @param args          the format arguments.
     */
    public void debug(final Element element, final String messageFormat, final Object... args) {
        if (isDebugEnabled) {
            other(null, messageFormat, element, args);
        }
    }

    /**
     * Prints a warning message.
     *
     * @param element the element to print with the message.
     * @param message the message.
     */
    public void warn(final Element element, final String message) {
        log(Kind.WARNING, element, message);
    }

    /**
     * Prints a formatted warning message.
     *
     * @param element       the element that caused the warning.
     * @param messageFormat the message format.
     * @param args          the format arguments.
     */
    public void warn(final Element element, final String messageFormat, final Object... args) {
        log(Kind.WARNING, element, messageFormat, args);
    }

    /**
     * Prints a warning message.
     *
     * @param element the element to print with the message.
     * @param message the message.
     */
    public void mandatoryWarning(final Element element, final String message) {
        log(Kind.MANDATORY_WARNING, element, message);
    }

    /**
     * Prints a formatted warning message.
     *
     * @param element       the element that caused the warning.
     * @param messageFormat the message format.
     * @param args          the format arguments.
     */
    public void mandatoryWarning(final Element element, final String messageFormat, final Object... args) {
        log(Kind.MANDATORY_WARNING, element, messageFormat, args);
    }

    /**
     * Prints a formatted error message.
     *
     * @param messageFormat the message format.
     * @param args          the format arguments.
     */
    public void error(final String messageFormat, final Object... args) {
        log(Kind.ERROR, null, messageFormat, args);
    }

    /**
     * Prints a error message.
     *
     * @param element the element to print with the message.
     * @param message the message.
     */
    public void error(final Element element, final String message) {
        log(Kind.ERROR, element, message);
    }

    /**
     * Prints a formatted error message.
     *
     * @param messageFormat the message format.
     * @param element       the element that caused the warning.
     * @param args          the format arguments.
     */
    public void error(final Element element, final String messageFormat, final Object... args) {
        log(Kind.ERROR, element, messageFormat, args);
    }

    /**
     * Prints an error message.
     *
     * @param cause the cause of the error.
     */
    public void error(final Throwable cause) {
        error(null, cause);
    }

    /**
     * Prints a error message.
     *
     * @param cause   the cause of the error.
     * @param element the element to print with the message.
     * @param message the message.
     */
    public void error(final Throwable cause, final Element element, final String message) {
        log(Kind.ERROR, element, cause, message);
    }

    /**
     * Prints an error message.
     *
     * @param cause         the cause of the error.
     * @param messageFormat the message format.
     * @param args          the format arguments.
     */
    public void error(final Throwable cause, final String messageFormat, final Object... args) {
        error(null, cause, messageFormat, args);
    }

    /**
     * Prints an error message.
     *
     * @param cause   the cause of the error.
     * @param element the element that caused the error.
     */
    public void error(final Element element, final Throwable cause) {
        log(Kind.ERROR, element, cause, null);
    }

    /**
     * Prints an error message.
     *
     * @param cause         the cause of the error.
     * @param messageFormat the message format.
     * @param element       the element that caused the warning.
     * @param args          the format arguments.
     */
    public void error(final Element element, final Throwable cause, final String messageFormat, final Object... args) {
        log(Kind.ERROR, element, cause, messageFormat, args);
    }

    /**
     * Prints a message that does not fit the other types.
     *
     * @param element the element to print with the message.
     * @param message the message.
     */
    public void other(final Element element, final String message) {
        log(Kind.OTHER, element, message);
    }

    /**
     * Prints a formatted message that does not fit the other types.
     *
     * @param element       the element to print with the note.
     * @param messageFormat the message format.
     * @param args          the format arguments.
     */
    public void other(final Element element, final String messageFormat, final Object... args) {
        log(Kind.OTHER, element, messageFormat, args);
    }

    private void log(final Kind kind, final Element element, final String message) {
        if (element == null) {
            messager.printMessage(kind, message);
        } else {
            messager.printMessage(kind, message, getElement(element));
        }
    }


    private void log(final Kind kind, final Element element, final String messageFormat, final Object... args) {
        try {
            String message = ((args == null || args.length == 0) ? messageFormat : String.format(messageFormat, args));

            if (element == null) {
                messager.printMessage(kind, message);
            } else {
                messager.printMessage(kind, message, getElement(element));
            }
            // Fail gracefully
        } catch (Throwable t) {
            if (element == null) {
                messager.printMessage(Kind.ERROR, "Error logging original message: " + messageFormat);
            } else {
                messager.printMessage(Kind.ERROR, "Error logging original message: " + messageFormat, getElement(element));
            }
        }
    }

    private void log(final Kind kind, final Element element, final Throwable cause, final String messageFormat, final Object... args) {

        String stringCause = stackTraceToString(cause);

        if (messageFormat == null) {
            log(kind, element, stringCause);
        } else {
            String messageWithCause = messageFormat.concat(", cause : %s");
            List<Object> newArgs = new ArrayList<>();
            newArgs.addAll(Arrays.asList(args));
            newArgs.add(stringCause);

            //Add cause to error message logging
            log(kind, element, messageWithCause, newArgs.toArray());
        }

    }

    private void log(final Kind kind, final Element element, final Throwable cause, final String message) {

        String stringCause = stackTraceToString(cause);

        if (message == null) {
            log(kind, element, stringCause);
        } else {
            String messageWithCause = message.concat(", cause : %s");
            //Add cause to error message logging
            log(kind, element, messageWithCause);
        }

    }

    /**
     * Converts a stack trace to string output.
     *
     * @param t the stack trace to convert.
     *
     * @return a string version of the stack trace.
     */
    public static String stackTraceToString(final Throwable t) {
        final StringWriter stringWriter = new StringWriter();
        final PrintWriter printWriter = new PrintWriter(stringWriter, true);
        t.printStackTrace(printWriter);
        printWriter.flush();
        stringWriter.flush();
        printWriter.close();
        try {
            stringWriter.close();
        } catch (IOException e) {
            // Do nothing
        }
        return stringWriter.toString();
    }

    private static Element getElement(final Element element) {
        // We need to the delegate element as some implementations rely on private types
        return (element instanceof DelegatingElement ? ((DelegatingElement) element).getDelegate() : element);
    }

}
