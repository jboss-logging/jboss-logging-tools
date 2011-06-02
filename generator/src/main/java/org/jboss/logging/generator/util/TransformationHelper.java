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
package org.jboss.logging.generator.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Various transformation utilities.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 */
public final class TransformationHelper {

    /**
     * Constructor for singleton.
     */
    private TransformationHelper() {
    }

    /**
     * Get the qualified name of a class in the given
     * package.
     *
     * @param packageName the package name
     * @param className   the class name
     *
     * @return the class qualified name
     */
    public static String toQualifiedClassName(final String packageName, final String className) {
        String qualifiedClassName;
        if (!packageName.isEmpty() && !packageName.endsWith(".")) {
            qualifiedClassName = String.format("%s.%s", packageName, className);
        } else {
            qualifiedClassName = packageName.concat(className);
        }
        return qualifiedClassName;
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

}
