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
package org.jboss.logging.generator;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * An abstract processor used process annotations.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 */
public abstract class AbstractTool {

    private final Elements elementUtils;

    private final Filer filer;

    private final ToolLogger logger;

    private final Types typeUtils;

    private final ProcessingEnvironment processingEnv;

    /**
     * Constructs a new processor.
     *
     * @param processingEnv the processing environment.
     */
    public AbstractTool(final ProcessingEnvironment processingEnv) {
        this.elementUtils = processingEnv.getElementUtils();
        this.filer = processingEnv.getFiler();
        this.logger = ToolLogger.getLogger(processingEnv);
        this.typeUtils = processingEnv.getTypeUtils();
        this.processingEnv = processingEnv;
    }

    /**
     * Processes a type element.
     *
     * @param annotation       the annotation who trigger the processing
     * @param element          the element that contains the methods.
     * @param methodDescriptor the method descriptors.
     */
    public abstract void processTypeElement(final TypeElement annotation, final TypeElement element, final MethodDescriptors methodDescriptor);


    /**
     * Returns the logger to log messages with.
     *
     * @return the logger to log messages with.
     */
    public final ToolLogger logger() {
        return logger;
    }

    /**
     * Returns the filer.
     *
     * @return the filer
     */
    public final Filer filer() {
        return this.filer;
    }

    /**
     * Returns the element utils.
     *
     * @return the utils
     */
    public final Elements elementUtils() {
        return this.elementUtils;
    }

    /**
     * Returns the type utils.
     *
     * @return the utils
     */
    public final Types typeUtils() {
        return this.typeUtils;
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
