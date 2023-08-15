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

import static javax.lang.model.util.ElementFilter.typesIn;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.ConstructType;
import org.jboss.logging.annotations.Field;
import org.jboss.logging.annotations.FormatWith;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.LoggingClass;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageBundle;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.logging.annotations.Once;
import org.jboss.logging.annotations.Param;
import org.jboss.logging.annotations.Pos;
import org.jboss.logging.annotations.Property;
import org.jboss.logging.annotations.Signature;
import org.jboss.logging.annotations.Transform;
import org.jboss.logging.annotations.ValidIdRange;
import org.jboss.logging.annotations.ValidIdRanges;
import org.jboss.logging.processor.model.DelegatingElement;
import org.jboss.logging.processor.model.MessageInterface;
import org.jboss.logging.processor.validation.ValidationMessage;
import org.jboss.logging.processor.validation.Validator;

/**
 * The main annotation processor for JBoss Logging Tooling.
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 */
@SupportedOptions({
        LoggingToolsProcessor.DEBUG_OPTION,
        LoggingToolsProcessor.EXPRESSION_PROPERTIES,
        LoggingToolsProcessor.ADD_GENERATED_ANNOTATION,
})
public class LoggingToolsProcessor extends AbstractProcessor {

    public static final String DEBUG_OPTION = "debug";
    static final String EXPRESSION_PROPERTIES = "org.jboss.logging.tools.expressionProperties";
    static final String ADD_GENERATED_ANNOTATION = "org.jboss.logging.tools.addGeneratedAnnotation";
    private final List<String> interfaceAnnotations = Arrays.asList(MessageBundle.class.getName(), MessageLogger.class.getName());
    private final List<AbstractGenerator> generators;
    private final Set<String> supportedAnnotations;
    private ToolLogger logger;

    /**
     * Default constructor.
     */
    public LoggingToolsProcessor() {
        this.generators = new ArrayList<>();
        this.supportedAnnotations = createSupportedAnnotations(
                Cause.class,
                ConstructType.class,
                Field.class,
                FormatWith.class,
                LoggingClass.class,
                LogMessage.class,
                Message.class,
                MessageBundle.class,
                MessageLogger.class,
                Once.class,
                Param.class,
                Pos.class,
                Property.class,
                Signature.class,
                Transform.class,
                ValidIdRange.class,
                ValidIdRanges.class
        );
    }

    @Override
    public Set<String> getSupportedOptions() {
        Set<String> supportedOptions = new HashSet<>();

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
        generators.add(new ReportFileGenerator(processingEnv));
    }

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        if (!roundEnv.processingOver() && !annotations.isEmpty()) {
            doProcess(annotations, roundEnv);
        }
        return true;
    }

    private void doProcess(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
        final String propertiesPath = processingEnv.getOptions().getOrDefault(EXPRESSION_PROPERTIES, "");
        final Properties expressionProperties = new Properties();
        if (!propertiesPath.isEmpty()) {
            final Path path = Paths.get(propertiesPath);
            if (Files.notExists(path)) {
                logger.error("Expression properties file %s does not exist.", propertiesPath);
                return;
            } else {
                try (final BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
                    expressionProperties.load(reader);
                } catch (IOException e) {
                    logger.error(e, "Error reading expression properties file %s", propertiesPath);
                    return;
                }
            }
        }
        final boolean addGeneratedAnnotation = Boolean.parseBoolean(processingEnv.getOptions().getOrDefault(ADD_GENERATED_ANNOTATION, "true"));
        boolean generate = true;
        final Validator validator = new Validator(processingEnv);

        //Call jboss logging tools
        for (TypeElement annotation : annotations) {
            // We only want to process the interface annotations
            if (interfaceAnnotations.contains(annotation.getQualifiedName().toString())) {
                try {
                    final Set<? extends TypeElement> interfaces = typesIn(roundEnv.getElementsAnnotatedWith(annotation));
                    for (TypeElement interfaceElement : interfaces) {
                        try {
                            final MessageInterface messageInterface = MessageInterfaceFactory.of(processingEnv, interfaceElement, expressionProperties, addGeneratedAnnotation);
                            final Collection<ValidationMessage> validationMessages = validator.validate(messageInterface);
                            for (ValidationMessage message : validationMessages) {
                                if (message.printMessage(processingEnv.getMessager())) {
                                    generate = false;
                                }
                            }
                            if (generate) {
                                if (interfaceElement.getKind().isInterface()
                                        && !interfaceElement.getModifiers().contains(Modifier.PRIVATE)) {
                                    for (AbstractGenerator processor : generators) {
                                        logger.debug("Executing processor %s", processor.getName());
                                        processor.processTypeElement(annotation, interfaceElement, messageInterface);
                                    }
                                }
                            }
                        } catch (ProcessingException e) {
                            final AnnotationMirror a = e.getAnnotation();
                            final AnnotationValue value = e.getAnnotationValue();
                            final Element element = resolveElement(e.getElement());
                            if (a == null) {
                                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage(), element);
                            } else if (value == null) {
                                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage(), element, a);
                            } else {
                                processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, e.getMessage(), element, a, value);
                            }
                        }
                    }
                } catch (Throwable t) {
                    logger.error(annotation, t);
                }
            }
        }
    }

    @SafeVarargs
    private static Set<String> createSupportedAnnotations(final Class<? extends Annotation>... annotations) {
        final Set<String> supportedAnnotations = new HashSet<>(annotations.length);
        for (Class<?> c : annotations) {
            supportedAnnotations.add(c.getName());
        }
        return Collections.unmodifiableSet(supportedAnnotations);
    }

    private static Element resolveElement(final Element element) {
        if (element instanceof DelegatingElement) {
            return ((DelegatingElement) element).getDelegate();
        }
        return element;
    }
}
