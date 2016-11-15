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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import org.jboss.logging.processor.model.MessageInterface;

/**
 * An abstract processor used process annotations.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 */
public abstract class AbstractGenerator {

    private final ToolLogger logger;

    private final ProcessingEnvironment processingEnv;

    /**
     * Constructs a new processor.
     *
     * @param processingEnv the processing environment.
     */
    AbstractGenerator(final ProcessingEnvironment processingEnv) {
        this.logger = ToolLogger.getLogger(processingEnv);
        this.processingEnv = processingEnv;
    }

    /**
     * Processes a type element.
     *
     * @param annotation       the annotation who trigger the processing
     * @param element          the element that contains the methods.
     * @param messageInterface the message interface to implement.
     */
    public abstract void processTypeElement(final TypeElement annotation, final TypeElement element, final MessageInterface messageInterface);


    /**
     * Returns the logger to log messages with.
     *
     * @return the logger to log messages with.
     */
    final ToolLogger logger() {
        return logger;
    }

    /**
     * Returns the filer.
     *
     * @return the filer
     */
    final Filer filer() {
        return processingEnv.getFiler();
    }

    /**
     * Returns the element utils.
     *
     * @return the utils
     */
    final Elements elementUtils() {
        return processingEnv.getElementUtils();
    }

    /**
     * Returns the type utils.
     *
     * @return the utils
     */
    public final Types typeUtils() {
        return processingEnv.getTypeUtils();
    }

    /**
     * Returns the processing environment.
     *
     * @return the processing environment being used.
     */
    public final ProcessingEnvironment processingEnv() {
        return processingEnv;
    }

    /**
     * Returns the name of the processor.
     *
     * @return the name of the processor.
     */
    public final String getName() {
        return this.getClass().getSimpleName();
    }

    /**
     * Returns the supported options set.
     *
     * @return the supported options set or empty set if none
     */
    public final Set<String> getSupportedOptions() {
        SupportedOptions options = this.getClass().getAnnotation(SupportedOptions.class);
        if (options != null) {
            return new HashSet<String>(Arrays.asList(options.value()));
        }

        return Collections.emptySet();
    }

}
