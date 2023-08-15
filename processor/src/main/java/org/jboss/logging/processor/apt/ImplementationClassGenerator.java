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

import java.io.IOException;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;

import org.jboss.logging.processor.generator.model.ClassModel;
import org.jboss.logging.processor.generator.model.ClassModelFactory;
import org.jboss.logging.processor.model.MessageInterface;

/**
 * A generator for creating implementations of message bundle and logging
 * interfaces.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
final class ImplementationClassGenerator extends AbstractGenerator {

    private static final String LOGGING_VERSION = "loggingVersion";

    /**
     * @param processingEnv the processing environment.
     */
    public ImplementationClassGenerator(ProcessingEnvironment processingEnv) {
        super(processingEnv);
        final Map<String, String> options = processingEnv.getOptions();
        if (options.containsKey(LOGGING_VERSION)) {
            logger().warn(null, "The option %s has been deprecated and is no longer used.", LOGGING_VERSION);
        }
    }

    @Override
    public void processTypeElement(final TypeElement annotation, final TypeElement element,
            final MessageInterface messageInterface) {
        try {
            final ClassModel classModel = ClassModelFactory.implementation(processingEnv, messageInterface);
            classModel.generateAndWrite();
        } catch (IllegalStateException | IOException e) {
            logger().error(element, e);
        }
    }
}
