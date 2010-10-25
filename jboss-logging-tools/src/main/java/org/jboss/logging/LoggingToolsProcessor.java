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

import org.jboss.logging.generator.ClassImplementorGenerator;
import org.jboss.logging.generator.TranslationClassGenerator;
import org.jboss.logging.generator.TranslationFilesGenerator;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.jboss.logging.util.TransformationUtil.stackTraceToString;

/**
 * The main annotation processor for JBoss Logging Tooling.
 *
 * @author James R. Perkins Jr. (jrp)
 * @author Kevin Pollet
 */
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class LoggingToolsProcessor extends AbstractProcessor {

    private final List<Generator> generators;

    /**
     * Default constructor.
     */
    public LoggingToolsProcessor() {
        this.generators = new ArrayList<Generator>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(final ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        //Tools generator
        generators.add(new ClassImplementorGenerator(processingEnv));
        generators.add(new TranslationClassGenerator(processingEnv));
        generators.add(new TranslationFilesGenerator(processingEnv));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getSupportedOptions() {
        Set<String> supportedOptions = new HashSet<String>();
        for (Generator generator : generators) {
            supportedOptions.addAll(generator.getSupportedOptions());
        }

        return supportedOptions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {

        try {

            for (Generator generator : generators) {
                processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "Executing " + generator.getName());
                generator.generate(annotations, roundEnv);
            }

        } catch (Exception e) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Error during invocation of LoggingToolsGenerator cause :" + stackTraceToString(e));
        }

        return false;
    }

}
