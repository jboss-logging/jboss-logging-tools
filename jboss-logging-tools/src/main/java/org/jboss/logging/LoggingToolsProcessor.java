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

import org.jboss.logging.generator.ImplementorClassGenerator;
import org.jboss.logging.generator.TranslationClassGenerator;
import org.jboss.logging.generator.TranslationFilesGenerator;
import org.jboss.logging.validation.ValidationException;
import org.jboss.logging.validation.ValidationProcessor;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static javax.lang.model.util.ElementFilter.typesIn;
import static org.jboss.logging.util.ElementUtil.getInterfaceMethods;


/**
 * The main annotation processor for JBoss Logging Tooling.
 *
 * @author James R. Perkins Jr. (jrp)
 * @author Kevin Pollet
 */
@SupportedAnnotationTypes({
        "org.jboss.logging.MessageBundle",
        "org.jboss.logging.MessageLogger"
})
@SupportedOptions({
        LoggingToolsProcessor.DEBUG_OPTION
})
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class LoggingToolsProcessor extends AbstractProcessor {

    public static final String DEBUG_OPTION = "debug";

    private final List<AbstractToolProcessor> processors;

    private ToolLogger logger;

    /**
     * Default constructor.
     */
    public LoggingToolsProcessor() {
        this.processors = new ArrayList<AbstractToolProcessor>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(final ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        logger = ToolLogger.getLogger(processingEnv);

        // Process validation first.
        processors.add(new ValidationProcessor(processingEnv));

        //Tools generator
        processors.add(new ImplementorClassGenerator(processingEnv));
        processors.add(new TranslationClassGenerator(processingEnv));
        processors.add(new TranslationFilesGenerator(processingEnv));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<String> getSupportedOptions() {
        Set<String> supportedOptions = new HashSet<String>();

        //Add global options
        SupportedOptions globalOptions = this.getClass().getAnnotation(SupportedOptions.class);
        if (globalOptions != null) {
            supportedOptions.addAll(Arrays.asList(globalOptions.value()));            
        }

        //Add tool processors options
        for (AbstractToolProcessor generator : processors) {
            supportedOptions.addAll(generator.getSupportedOptions());
        }

        return supportedOptions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {

        Types typesUtil = processingEnv.getTypeUtils();

        try {

            //Call abstract processor to process type elements
            for (TypeElement annotation : annotations) {

                Set<? extends TypeElement> elements = typesIn(roundEnv.getElementsAnnotatedWith(annotation));

                for (TypeElement element : elements) {

                   if (element.getKind().isInterface()
                       && !element.getModifiers().contains(Modifier.PRIVATE)) {

                       Collection<ExecutableElement> methods = getInterfaceMethods(element, typesUtil);
                       
                       for (AbstractToolProcessor processor : processors) {
                            logger.debug("Executing processor %s", processor.getName());
                            processor.processTypeElement(annotation, element, methods);
                       }
                   }
                }
            }
            
        }
        catch (ValidationException e) {
            logger.error(e, "Error during validation");
        }

        return true;
    }
}
