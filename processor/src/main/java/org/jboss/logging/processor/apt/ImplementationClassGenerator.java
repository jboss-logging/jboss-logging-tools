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
package org.jboss.logging.processor.apt;

import java.io.IOException;
import java.util.Map;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;

import org.jboss.logging.processor.generator.model.ClassModel;
import org.jboss.logging.processor.generator.model.ClassModelFactory;
import org.jboss.logging.processor.model.MessageInterface;
import org.jboss.logging.processor.util.VersionComparator;

/**
 * A generator for creating implementations of message bundle and logging
 * interfaces.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
final class ImplementationClassGenerator extends AbstractGenerator {

    private static final String LOGGING_VERSION = "loggingVersion";
    private final boolean useLogging31;
    private final boolean annotateOutput;

    /**
     * @param processingEnv the processing environment.
     */
    public ImplementationClassGenerator(ProcessingEnvironment processingEnv) {
        super(processingEnv);
        final Map<String, String> options = processingEnv.getOptions();
        if (options.containsKey(LOGGING_VERSION)) {
            final String loggingVersion = options.get(LOGGING_VERSION);
            useLogging31 = (VersionComparator.compareVersion(loggingVersion, "3.1") >= 0);
        } else {
            useLogging31 = true;
        }
        if (options.containsKey(LoggingToolsProcessor.SKIP_GENERATED_ANNOTATION)) {
            final String skipTheGeneratedAnnotation = options.get(LoggingToolsProcessor.SKIP_GENERATED_ANNOTATION);
            annotateOutput = ! "true".equalsIgnoreCase(skipTheGeneratedAnnotation);
        } else {
            annotateOutput = true;
        }
    }

    @Override
    public void processTypeElement(final TypeElement annotation, final TypeElement element, final MessageInterface messageInterface) {
        try {
            final ClassModel classModel = ClassModelFactory.implementation(filer(), messageInterface, useLogging31, annotateOutput);
            classModel.generateAndWrite();
        } catch (IllegalStateException | IOException e) {
            logger().error(element, e);
        }
    }
}
