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

    private final Messager messager;

    private ToolLogger(final Messager messager) {
        this.messager = messager;
    }

    /**
     * Creates a new tool logger.
     *
     * @param messager the messager used to log messages with.
     * 
     * @return a new tool logger.
     */
    public static ToolLogger getLogger(final Messager messager) {
        return new ToolLogger(messager);
    }

    /**
     * Prints a note message.
     *
     * @param message   the message to print.
     */
    public void note(final String message) {
        messager.printMessage(Kind.NOTE, message);
    }

    /**
     * Prints a formatted note message.
     *
     * @param messageFormat the message format.
     * @param args          the format arguments.
     */
    public void note(final String messageFormat, Object... args) {
        messager.printMessage(Kind.NOTE, String.format(messageFormat, args));
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
        messager.printMessage(Kind.NOTE, String.format(messageFormat, args),
                element);
    }

    /**
     * Prints a warning message.
     *
     * @param message   the message to print.
     */
    public void warn(final String message) {
        messager.printMessage(Kind.WARNING, message);
    }

    /**
     * Prints a formatted warning message.
     *
     * @param messageFormat the message format.
     * @param args          the format arguments.
     */
    public void warn(final String messageFormat, Object... args) {
        messager.printMessage(Kind.WARNING, String.format(messageFormat, args));
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
        messager.printMessage(Kind.WARNING, String.format(messageFormat, args),
                element);
    }

    /**
     * Prints a warning message.
     *
     * @param message   the message to print.
     */
    public void mandatoryWarning(final String message) {
        messager.printMessage(Kind.MANDATORY_WARNING, message);
    }

    /**
     * Prints a formatted warning message.
     *
     * @param messageFormat the message format.
     * @param args          the format arguments.
     */
    public void mandatoryWarning(final String messageFormat, Object... args) {
        messager.printMessage(Kind.MANDATORY_WARNING, String.format(
                messageFormat, args));
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
        messager.printMessage(Kind.MANDATORY_WARNING, String.format(
                messageFormat, args),
                element);
    }

    /**
     * Prints an error message.
     *
     * @param message   the message to print.
     */
    public void error(final String message) {
        messager.printMessage(Kind.ERROR, message);
    }

    /**
     * Prints a formatted error message.
     *
     * @param messageFormat the message format.
     * @param args          the format arguments.
     */
    public void error(final String messageFormat, Object... args) {
        messager.printMessage(Kind.ERROR, String.format(messageFormat, args));
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
        messager.printMessage(Kind.ERROR, String.format(messageFormat, args),
                element);
    }

    /**
     * Prints an error message.
     *
     * @param cause the cause of the error.
     */
    public void error(final Throwable cause) {
        messager.printMessage(Kind.ERROR, TransformationUtil.stackTraceToString(
                cause));
    }

    /**
     * Prints an error message.
     *
     * @param cause   the cause of the error.
     * @param element the element that caused the error.
     */
    public void error(final Throwable cause, final Element element) {
        messager.printMessage(Kind.ERROR, TransformationUtil.stackTraceToString(
                cause), element);
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
        messager.printMessage(Kind.ERROR, TransformationUtil.stackTraceToString(
                cause));
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
        messager.printMessage(Kind.ERROR, TransformationUtil.stackTraceToString(
                cause), element);
    }

    /**
     * Prints a message that does not fit the other types.
     *
     * @param message   the message to print.
     */
    public void other(final String message) {
        messager.printMessage(Kind.OTHER, message);
    }

    /**
     * Prints a formatted message that does not fit the other types.
     *
     * @param messageFormat the message format.
     * @param args          the format arguments.
     */
    public void other(final String messageFormat, Object... args) {
        messager.printMessage(Kind.OTHER, String.format(messageFormat, args));
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
        messager.printMessage(Kind.OTHER, String.format(messageFormat, args),
                element);
    }
}
