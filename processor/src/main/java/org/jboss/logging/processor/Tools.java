/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

package org.jboss.logging.processor;

import java.util.ServiceLoader;

import org.jboss.logging.processor.apt.Annotations;
import org.jboss.logging.processor.apt.AnnotationsImpl;

/**
 * A helper class that uses services loaders to load implementations for the {@link Annotations} and the {@link
 * Loggers} interfaces. If the service loader did not find an implementation a default implementation is used.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a> - 21.Feb.2011
 */
public class Tools {

    private static final Tools INSTANCE = new Tools();

    private final Annotations annotations;
    private final Loggers loggers;

    private Tools() {
        final ServiceLoader<Annotations> annotationsLoader = ServiceLoader.load(Annotations.class, Tools.class.getClassLoader());
        final ServiceLoader<Loggers> loggersLoader = ServiceLoader.load(Loggers.class, Tools.class.getClassLoader());
        if (annotationsLoader.iterator().hasNext()) {
            annotations = annotationsLoader.iterator().next();
        } else {
            annotations = new AnnotationsImpl();
        }
        if (loggersLoader.iterator().hasNext()) {
            loggers = loggersLoader.iterator().next();
        } else {
            loggers = new JBossLoggers();
        }
    }


    /**
     * Locates the first implementation of {@link Annotations}.
     *
     * @return the annotations
     *
     * @throws IllegalStateException if the implementation could not be found.
     */
    public static Annotations annotations() {
        return INSTANCE.annotations;
    }


    /**
     * Locates the first implementation of {@link Loggers}.
     *
     * @return the loggers to use.
     *
     * @throws IllegalStateException if the implementation could not be found.
     */
    public static Loggers loggers() {
        return INSTANCE.loggers;
    }
}
