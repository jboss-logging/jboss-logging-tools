/*
 * JBoss, Home of Professional Open Source Copyright 2010, Red Hat, Inc., and
 * individual contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.jboss.logging;

import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;

import org.jboss.logging.util.TransformationUtil;

/**
 * @author James R. Perkins Jr. (jrp)
 *
 */
public abstract class Generator {
    private final ProcessingEnvironment processingEnv;

    /**
     * Constructs a new generator.
     *
     * @param processingEnv
     *            the processing environment.
     */
    public Generator(final ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
    }

    /**
     * Returns the name of the generator.
     *
     * @return the name of the generator.
     */
    public abstract String getName();

    /**
     *
     *
     * @param annotations
     *            the to process.
     * @param roundEnv
     *            the round environment.
     */
    public abstract void generate(final Set<? extends TypeElement> annotations,
            final RoundEnvironment roundEnv);

    /**
     * Returns the processing environment.
     *
     * @return the processing environment being used.
     */
    public final ProcessingEnvironment processingEnv() {
        return processingEnv;
    }

    /**
     * Convenience method for printing informational messages.
     *
     * @param message
     *            the message to print.
     */
    public final void printInfoMessage(final String message) {
        processingEnv.getMessager().printMessage(Kind.NOTE, message);
    }

    /**
     * Convenience method for printing an error messages.
     *
     * @param message
     *            the error message to print.
     */
    public final void printErrorMessage(final String message) {
        processingEnv.getMessager().printMessage(Kind.ERROR, message);
    }

    /**
     * Prints the stack trace to error message.
     *
     * @param throwable
     *            the stack trace to print.
     */
    public final void printErrorMessage(final Throwable throwable) {
        processingEnv.getMessager().printMessage(Kind.ERROR,
                TransformationUtil.stackTraceToString(throwable));
    }

    /**
     * Convenience method for printing an error messages.
     *
     * @param message
     *            the error message to print.
     * @param element
     *            the element that caused the error.
     */
    public final void printErrorMessage(final String message,
            final Element element) {
        processingEnv.getMessager().printMessage(Kind.ERROR, message, element);
    }

    /**
     * Prints the stack trace to error message.
     *
     * @param throwable
     *            the stack trace to print.
     * @param element
     *            the element that caused the error.
     */
    public final void printErrorMessage(final Throwable throwable,
            final Element element) {
        processingEnv.getMessager().printMessage(Kind.ERROR,
                TransformationUtil.stackTraceToString(throwable), element);
    }

    /**
     * Convenience method for printing a warning message.
     *
     * @param message
     *            the warning message to print.
     */
    public final void printWarningMessage(final String message) {
        processingEnv.getMessager().printMessage(Kind.WARNING, message);
    }

    /**
     * Prints the initial message as informational message then the stack trace
     * as an error message.
     *
     * @param initialMessage
     *            the initial message to print.
     * @param throwable
     *            the stack trace to print.
     */
    public final void printErrorMessage(final String initialMessage,
            final Throwable throwable) {
        printInfoMessage(initialMessage);
        printErrorMessage(throwable);
    }

}
