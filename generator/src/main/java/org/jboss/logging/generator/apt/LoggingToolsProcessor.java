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
package org.jboss.logging.generator.apt;

import org.jboss.logging.generator.intf.model.MessageInterface;
import org.jboss.logging.generator.validation.ValidationMessage;
import org.jboss.logging.generator.validation.Validator;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static javax.lang.model.util.ElementFilter.typesIn;
import static org.jboss.logging.generator.Tools.annotations;

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
    private final List<AbstractGenerator> processors;
    private ToolLogger logger;

    /**
     * Default constructor.
     */
    public LoggingToolsProcessor() {
        this.processors = new ArrayList<AbstractGenerator>();
    }

    @Override
    public void init(final ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        logger = ToolLogger.getLogger(processingEnv);

        //Tools generator -  Note the order these are executed in.
        processors.add(new ImplementationClassGenerator(processingEnv));
        processors.add(new TranslationClassGenerator(processingEnv));
        processors.add(new TranslationFileGenerator(processingEnv));
    }

    @Override
    public Set<String> getSupportedOptions() {
        Set<String> supportedOptions = new HashSet<String>();

        //Add global options
        SupportedOptions globalOptions = this.getClass().getAnnotation(SupportedOptions.class);
        if (globalOptions != null) {
            supportedOptions.addAll(Arrays.asList(globalOptions.value()));
        }

        //Add tool processors options
        for (AbstractGenerator generator : processors) {
            supportedOptions.addAll(generator.getSupportedOptions());
        }

        return supportedOptions;
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        boolean process = true;

        //Call jboss logging tools
        for (TypeElement annotation : annotations) {
            if (isValidAnnotation(annotation)) {
                final Set<? extends TypeElement> interfaces = typesIn(roundEnv.getElementsAnnotatedWith(annotation));
                for (TypeElement interfaceElement : interfaces) {
                    final MessageInterface messageInterface = MessageInterfaceFactory.of(processingEnv, interfaceElement);
                    final Collection<ValidationMessage> validationMessages = Validator.INSTANCE.validate(messageInterface);
                    for (ValidationMessage message : validationMessages) {
                        final Object reference = message.getMessageObject().reference();
                        if (reference instanceof Element) {
                            final Element element = Element.class.cast(reference);
                            switch (message.type()) {
                                case ERROR: {
                                    logger.error(element, message.getMessage());
                                    process = false;
                                    break;
                                }
                                case WARN: {
                                    logger.warn(element, message.getMessage());
                                    break;
                                }
                                default: {
                                    logger.note(element, message.getMessage());
                                }
                            }
                        } else {
                            switch (message.type()) {
                                case ERROR: {
                                    logger.error(message.getMessage());
                                    process = false;
                                    break;
                                }
                                case WARN: {
                                    logger.warn(message.getMessage());
                                    break;
                                }
                                default: {
                                    logger.note(message.getMessage());
                                }
                            }
                        }
                    }
                    if (process) {
                        if (interfaceElement.getKind().isInterface()
                                && !interfaceElement.getModifiers().contains(Modifier.PRIVATE)) {
                            for (AbstractGenerator processor : processors) {
                                logger.debug("Executing processor %s", processor.getName());
                                processor.processTypeElement(annotation, interfaceElement, messageInterface);
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
        return (name.equals(annotations().messageBundle().getName()) || name.equals(annotations().messageLogger().getName()));
    }
}
