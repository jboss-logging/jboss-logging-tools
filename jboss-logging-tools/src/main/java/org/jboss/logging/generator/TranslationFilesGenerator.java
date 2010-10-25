package org.jboss.logging.generator;

import org.jboss.logging.Generator;
import org.jboss.logging.Message;
import org.jboss.logging.MessageBundle;
import org.jboss.logging.MessageLogger;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The generator of skeletal
 * translations files.
 * 
 * @author Kevin Pollet
 */
@SupportedOptions(TranslationFilesGenerator.GENERATED_FILES_PATH)
public final class TranslationFilesGenerator extends Generator {

    public static final String GENERATED_FILES_PATH = "generated.translation.files.path";

    private static final String GENERATED_FILE_EXTENSION = ".i18n_locale_COUNTRY_VARIANT.properties";

    private final String generatedFilesPath;

    /**
     * The constructor.
     * @param processingEnv the processing env
     */
    public TranslationFilesGenerator(final ProcessingEnvironment processingEnv) {
        super(processingEnv);

        Map<String, String> options = processingEnv.getOptions();
        this.generatedFilesPath = options.get(GENERATED_FILES_PATH);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void generate(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {

        if (generatedFilesPath != null) {
            logger().note("Generate skeletal translation files.");

            Set<TypeElement> elementsToProcess = new HashSet<TypeElement>();

            //Process @MessageBundle
            Set<? extends Element> bundles = roundEnv.getElementsAnnotatedWith(MessageBundle.class);
            elementsToProcess.addAll(ElementFilter.typesIn(bundles));

            //Process @MessageLogger
            Set<? extends Element> loggers = roundEnv.getElementsAnnotatedWith(MessageLogger.class);
            elementsToProcess.addAll(ElementFilter.typesIn(loggers));

            for (TypeElement element : elementsToProcess) {

                

                
            }

        }
        
    }

    /**
     * Returns all the translations messages for
     * the given type element. A translation message value is
     * defined by the {@link org.jboss.logging.Message} annotation
     * value and the annotated method is the translation key.
     *
     * @param element the element
     * @return all the translations messages
     */
    private Map<String, String> getTranslationMessages(final TypeElement element) {

        Map<String, String> translationsMessage = new HashMap<String, String>();

        if (element.getKind().isInterface()) {

            //Get super interfaces class translations messages
            List<? extends TypeMirror> superInterfaces = element.getInterfaces();
            for (TypeMirror intf : superInterfaces) {
                translationsMessage.putAll(this.getTranslationMessages((TypeElement) this.typeUtils().asElement(intf)));
            }

            //Get current class translation message
            List<ExecutableElement> methods = ElementFilter.methodsIn(element.getEnclosedElements());
            for (ExecutableElement method : methods) {
                Message annotation = method.getAnnotation(Message.class);
                if (annotation != null) {
                    translationsMessage.put(method.getSimpleName().toString(), annotation.value());
                }
            }

        }

        return translationsMessage;
    }
    
    /**
     * Generate the translation file containing the given
     * translations.
     *
     * @param fileName the file name
     * @param translations the translations
     */
    public void generateSkeletalTranslationFile(final String fileName, final Map<String, String> translations) {




        
    }

}
