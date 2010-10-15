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
import javax.lang.model.element.TypeElement;

/**
 * @author James R. Perkins Jr. (jrp)
 * @author Kevin Pollet
 */
public abstract class Generator {

    private final ToolLogger logger;

    /**
     * The processing environment.
     */
    private final ProcessingEnvironment processingEnv;

    /**
     * Constructs a new generator.
     *
     * @param processingEnv
     *            the processing environment.
     */
    public Generator(final ProcessingEnvironment processingEnv) {
        this.processingEnv = processingEnv;
        this.logger = ToolLogger.getLogger(processingEnv.getMessager());
    }

    /**
     * Returns the logger to log messages with.
     *
     * @return the logger to log messages with.
     */
    public final ToolLogger logger() {
        return logger;
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
}
