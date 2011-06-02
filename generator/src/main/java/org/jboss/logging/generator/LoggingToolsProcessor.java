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

import org.jboss.logging.generator.validation.ValidationMessage;
import org.jboss.logging.generator.validation.Validator;

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
import static org.jboss.logging.generator.util.ElementHelper.getInterfaceMethods;

/**
 * The main annotation processor for JBoss Logging Tooling.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 */
@SupportedAnnotationTypes("*")
@SupportedOptions({
        LoggingToolsProcessor.DEBUG_OPTION
})
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class LoggingToolsProcessor extends AbstractProcessor {

    public static final String DEBUG_OPTION = "debug";
    private final List<AbstractTool> processors;
    private Annotations annotations;
    private Loggers loggers;
    private ToolLogger logger;

    /**
     * Default constructor.
     */
    public LoggingToolsProcessor() {
        this.processors = new ArrayList<AbstractTool>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(final ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        logger = ToolLogger.getLogger(processingEnv);
        annotations = LoggingTools.annotations();
        loggers = LoggingTools.loggers();

        //Tools generator -  Note the order these are excuted in.
        processors.add(new ImplementorClassGenerator(processingEnv));
        processors.add(new TranslationClassGenerator(processingEnv));
        processors.add(new TranslationFileGenerator(processingEnv));
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
        for (AbstractTool generator : processors) {
            supportedOptions.addAll(generator.getSupportedOptions());
        }

        return supportedOptions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        boolean process = true;

        Types typesUtil = processingEnv.getTypeUtils();
        Validator validator = Validator.buildValidator(processingEnv);

        //Call jboss logging tools
        for (TypeElement annotation : annotations) {
            if (isValidAnnotation(annotation)) {
                Set<? extends TypeElement> elements = typesIn(roundEnv.getElementsAnnotatedWith(annotation));
                Collection<ValidationMessage> errorMessages = validator.validate(elements);
                for (ValidationMessage error : errorMessages) {
                    if (error.type() == ValidationMessage.MessageType.ERROR) {
                        logger.error(error.getElement(), error.getMessage());
                        process = false;
                    } else {
                        logger.warn(error.getElement(), error.getMessage());
                    }
                }
                if (process) {
                    for (TypeElement element : elements) {

                        if (element.getKind().isInterface()
                                && !element.getModifiers().contains(Modifier.PRIVATE)) {

                            Collection<ExecutableElement> methods = getInterfaceMethods(element, typesUtil);
                            final MethodDescriptors methodDescriptors = MethodDescriptors.of(processingEnv.getElementUtils(), typesUtil, methods);

                            for (AbstractTool processor : processors) {
                                logger.debug("Executing processor %s", processor.getName());
                                processor.processTypeElement(annotation, element, methodDescriptors);
                            }
                        }
                    }

                }
            }

        }

        return process;
    }

    private boolean isValidAnnotation(final TypeElement annotation) {
        final String name = annotation.getQualifiedName().toString();
        return (name.equals(annotations.messageBundle().getName()) || name.equals(annotations.messageLogger().getName()));
    }
}
