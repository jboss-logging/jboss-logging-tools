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
package org.jboss.logging.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author James R. Perkins Jr. (jrp)
 * 
 */
final class TransformationUtil {

    /**
     * Constructor for singleton.
     */
    private TransformationUtil() {
    }

    /**
     * Returns the package name from a qualified object name.
     * 
     * @param qualifiedName
     *            the qualified object name.
     * @return the package name.
     */
    public static String toPackage(final String qualifiedName) {
        String result = null;
        int index = qualifiedName.lastIndexOf(".");
        if (index != -1) {
            result = qualifiedName.substring(0, index);
        }
        return result;
    }

    /**
     * Removes the package name from the qualified object name and returns the
     * class name.
     * 
     * @param qualifiedClassName
     *            the qualified object name.
     * @return the class name minus the package.
     */
    public static String toSimpleClassName(final String qualifiedClassName) {
        String result = null;
        int index = qualifiedClassName.lastIndexOf(".");
        if (index != -1) {
            result = qualifiedClassName.substring(index + 1);
        }
        return result;
    }

    /**
     * Converts a stack trace to string output.
     * 
     * @param t
     *            the stack trace to convert.
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

}
