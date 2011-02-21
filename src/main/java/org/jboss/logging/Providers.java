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
package org.jboss.logging;

import java.util.ServiceLoader;

/**
 * This class is not thread safe.
 *
 * @author James R. Perkins (jrp) - 21.Feb.2011
 */
public class Providers {

    private static Annotations annotations;
    private static Loggers loggers;
    private static ServiceLoader<Annotations> annotationsLoader = ServiceLoader.load(Annotations.class);
    private static ServiceLoader<Loggers> loggersLoader = ServiceLoader.load(Loggers.class);

    private Providers() {
    }

    public static Annotations findAnnotations() {
        if (annotations == null) {
            if (annotationsLoader.iterator().hasNext()) {
                annotations = annotationsLoader.iterator().next();
            } else {
                throw new IllegalStateException("Annotations not found.");
            }
        }
        return annotations;
    }

    public static Loggers findLoggers() {
        if (loggers == null) {
            if (loggersLoader.iterator().hasNext()) {
                loggers = loggersLoader.iterator().next();
            } else {
                throw new IllegalStateException("Annotations not found.");
            }
        }
        return loggers;
    }
}
