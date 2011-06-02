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
package org.jboss.logging.generator;

import org.jboss.logging.generator.util.TransformationHelper;

import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic.Kind;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
     * Prints a formatted note message.
     *
     * @param messageFormat the message format.
     * @param args          the format arguments.
     */
    public void note(final String messageFormat, final Object... args) {
        note(null, messageFormat, args);
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
     * Prints a formatted debug message if debugging is enabled.
     *
     * @param element       the element to print with the note.
     * @param messageFormat the message format.
     * @param args          the format arguments.
     */
    public void debug(final Element element, final String messageFormat, final Object... args) {
        if (isDebugEnabled) {
            other(messageFormat, element, args);
        }
    }

    /**
     * Prints a formatted warning message.
     *
     * @param messageFormat the message format.
     * @param args          the format arguments.
     */
    public void warn(final String messageFormat, final Object... args) {
        warn(null, messageFormat, args);
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
     * Prints a formatted warning message.
     *
     * @param messageFormat the message format.
     * @param args          the format arguments.
     */
    public void mandatoryWarning(final String messageFormat, final Object... args) {
        mandatoryWarning(null, messageFormat, args);
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
     * @param exception the cause of the error.
     */
    public void error(final Exception exception) {
        error(null, exception);
    }

    /**
     * Prints an error message.
     *
     * @param exception     the cause of the error.
     * @param messageFormat the message format.
     * @param args          the format arguments.
     */
    public void error(final Exception exception, final String messageFormat, final Object... args) {
        error(null, exception, messageFormat, args);
    }

    /**
     * Prints an error message.
     *
     * @param exception the cause of the error.
     * @param element   the element that caused the error.
     */
    public void error(final Element element, final Exception exception) {
        log(Kind.ERROR, element, exception, null);
    }

    /**
     * Prints an error message.
     *
     * @param exception     the cause of the error.
     * @param messageFormat the message format.
     * @param element       the element that caused the warning.
     * @param args          the format arguments.
     */
    public void error(final Element element, final Exception exception, final String messageFormat, final Object... args) {
        log(Kind.ERROR, element, exception, messageFormat, args);
    }

    /**
     * Prints a formatted message that does not fit the other types.
     *
     * @param messageFormat the message format.
     * @param args          the format arguments.
     */
    public void other(final String messageFormat, final Object... args) {
        log(Kind.OTHER, null, messageFormat, args);
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

    private void log(final Kind kind, final Element element, final String messageFormat, final Object... args) {

        String message = String.format(messageFormat, args);

        if (element == null) {
            messager.printMessage(kind, message);
        } else {
            messager.printMessage(kind, message, element);
        }
    }

    private void log(final Kind kind, final Element element, final Exception exception, final String messageFormat, final Object... args) {

        String stringCause = TransformationHelper.stackTraceToString(exception);

        if (messageFormat == null) {
            log(kind, element, stringCause);
        } else {
            String messageWithCause = messageFormat.concat(", cause : %s");
            List<Object> newArgs = new ArrayList<Object>();
            newArgs.addAll(Arrays.asList(args));
            newArgs.add(stringCause);

            //Add cause to error message logging
            log(kind, element, messageWithCause, newArgs.toArray());
        }

    }

}
