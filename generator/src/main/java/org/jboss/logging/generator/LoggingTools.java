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

import java.util.ServiceLoader;

/**
 * This class is not thread safe. The static methods use lazy loading for static
 * variables.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a> - 21.Feb.2011
 */
public class LoggingTools {

    private static Annotations annotations;
    private static Loggers loggers;
    private static ServiceLoader<Annotations> annotationsLoader = ServiceLoader.load(Annotations.class, LoggingTools.class.getClassLoader());
    private static ServiceLoader<Loggers> loggersLoader = ServiceLoader.load(Loggers.class, LoggingTools.class.getClassLoader());

    private LoggingTools() {
    }

    /**
     * Locates the first implementation of {@link Annotations}.
     *
     * @return the annotations to use.
     * @throws IllegalStateException if the implementation could not be found.
     */
    public static Annotations annotations() {
        if (annotations == null) {
            for (Annotations a : annotationsLoader)
                if (annotationsLoader.iterator().hasNext()) {
                    annotations = annotationsLoader.iterator().next();
                } else {
                    throw new IllegalStateException("Annotations not found.");
                }
        }
        return annotations;
    }


    /**
     * Locates the first implementation of {@link Loggers}.
     *
     * @return the loggers to use.
     * @throws IllegalStateException if the implementation could not be found.
     */
    public static Loggers loggers() {
        if (loggers == null) {
            if (loggersLoader.iterator().hasNext()) {
                loggers = loggersLoader.iterator().next();
            } else {
                throw new IllegalStateException("Loggers not found.");
            }
        }
        return loggers;
    }
}
