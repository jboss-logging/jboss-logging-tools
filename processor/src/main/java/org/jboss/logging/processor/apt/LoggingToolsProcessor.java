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

import static javax.lang.model.util.ElementFilter.typesIn;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.Field;
import org.jboss.logging.annotations.FormatWith;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.LoggingClass;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageBundle;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.logging.annotations.Param;
import org.jboss.logging.annotations.Pos;
import org.jboss.logging.annotations.Property;
import org.jboss.logging.annotations.Transform;
import org.jboss.logging.annotations.ValidIdRange;
import org.jboss.logging.annotations.ValidIdRanges;
import org.jboss.logging.processor.model.MessageInterface;
import org.jboss.logging.processor.util.ElementHelper;
import org.jboss.logging.processor.validation.ValidationMessage;
import org.jboss.logging.processor.validation.Validator;

/**
 * The main annotation processor for JBoss Logging Tooling.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 */
@SupportedOptions({
        LoggingToolsProcessor.DEBUG_OPTION
})
public class LoggingToolsProcessor extends AbstractProcessor {

    public static final String DEBUG_OPTION = "debug";
    private final List<String> interfaceAnnotations = Arrays.asList(MessageBundle.class.getName(), MessageLogger.class.getName());
    private final List<AbstractGenerator> generators;
    private final Set<String> supportedAnnotations;
    private ToolLogger logger;

    /**
     * Default constructor.
     */
    public LoggingToolsProcessor() {
        this.generators = new ArrayList<>();
        final Set<String> annotations = new HashSet<>();
        // Interface annotations
        annotations.addAll(interfaceAnnotations);
        // Other annotations
        annotations.add(Cause.class.getName());
        annotations.add(Field.class.getName());
        annotations.add(FormatWith.class.getName());
        annotations.add(LoggingClass.class.getName());
        annotations.add(LogMessage.class.getName());
        annotations.add(Message.class.getName());
        annotations.add(Param.class.getName());
        annotations.add(Pos.class.getName());
        annotations.add(Property.class.getName());
        annotations.add(Transform.class.getName());
        annotations.add(ValidIdRange.class.getName());
        annotations.add(ValidIdRanges.class.getName());
        this.supportedAnnotations = Collections.unmodifiableSet(annotations);
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
        for (AbstractGenerator generator : generators) {
            supportedOptions.addAll(generator.getSupportedOptions());
        }

        return supportedOptions;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return supportedAnnotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public synchronized void init(final ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        logger = ToolLogger.getLogger(processingEnv);

        //Tools generator -  Note the order these are executed in.
        generators.add(new ImplementationClassGenerator(processingEnv));
        generators.add(new TranslationClassGenerator(processingEnv));
        generators.add(new TranslationFileGenerator(processingEnv));
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        boolean process = true;
        final Validator validator = new Validator();

        //Call jboss logging tools
        for (TypeElement annotation : annotations) {
            // We only want to process the interface annotations
            if (interfaceAnnotations.contains(annotation.getQualifiedName().toString())) {
                try {
                    final Set<? extends TypeElement> interfaces = typesIn(roundEnv.getElementsAnnotatedWith(annotation));
                    for (TypeElement interfaceElement : interfaces) {
                        try {
                            final MessageInterface messageInterface = MessageInterfaceFactory.of(processingEnv, interfaceElement);
                            final Collection<ValidationMessage> validationMessages = validator.validate(messageInterface);
                            for (ValidationMessage message : validationMessages) {
                                final Element element = ElementHelper.fromMessageObject(message.getMessageObject());
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
                            }
                            if (process) {
                                if (interfaceElement.getKind().isInterface()
                                        && !interfaceElement.getModifiers().contains(Modifier.PRIVATE)) {
                                    for (AbstractGenerator processor : generators) {
                                        logger.debug("Executing processor %s", processor.getName());
                                        processor.processTypeElement(annotation, interfaceElement, messageInterface);
                                    }
                                }
                            }
                        } catch (ProcessingException e) {
                            logger.error(e.getElement(), e.getMessage());
                        }
                    }
                } catch (Throwable t) {
                    logger.error(annotation, t);
                }
            }
        }
        return process;
    }
}
