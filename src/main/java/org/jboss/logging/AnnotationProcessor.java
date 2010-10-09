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

import org.jboss.logging.traduction.PropertyClassGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;

/**
 * 
 * 
 * @author James R. Perkins Jr. (jrp)
 * 
 */
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class AnnotationProcessor extends AbstractProcessor {

    /**
     * The generators.
     */
    private final List<Generator> generators = new ArrayList<Generator>();

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        for (Generator generator : generators) {
            // Catch all exceptions
            try {

                generator.generate(annotations, roundEnv);

            } catch (Throwable t) {
                processingEnv.getMessager().printMessage(Kind.ERROR, TransformationUtil.stackTraceToString(t));
            }
        }
        
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(final ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        
        generators.add(new ClassGenerator(processingEnv));
        generators.add(new PropertyClassGenerator(processingEnv));
    }

}
