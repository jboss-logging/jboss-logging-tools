/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2023 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.logging.processor.apt;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.element.TypeElement;

import org.jboss.logging.processor.model.MessageInterface;

/**
 * An abstract processor used process annotations.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 */
public abstract class AbstractGenerator {

    private final ToolLogger logger;

    final ProcessingEnvironment processingEnv;

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
    public abstract void processTypeElement(final TypeElement annotation, final TypeElement element, final MessageInterface messageInterface, final RoundEnvironment roundEnv);

    /**
     * Returns the logger to log messages with.
     *
     * @return the logger to log messages with.
     */
    final ToolLogger logger() {
        return logger;
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
            return new HashSet<>(Arrays.asList(options.value()));
        }

        return Collections.emptySet();
    }

}
