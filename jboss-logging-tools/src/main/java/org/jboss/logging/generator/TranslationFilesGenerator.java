package org.jboss.logging.generator;

import org.jboss.logging.AbstractTool;
import org.jboss.logging.util.ElementHelper;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * The generator of skeletal
 * translations files.
 *
 * @author Kevin Pollet
 */
@SupportedOptions(TranslationFilesGenerator.GENERATED_FILES_PATH)
public final class TranslationFilesGenerator extends AbstractTool {

    public static final String GENERATED_FILES_PATH = "generated.translation.files.path";

    public static final String GENERATED_FILE_EXTENSION = ".i18n_locale_COUNTRY_VARIANT.properties";

    private final String generatedFilesPath;

    /**
     * The constructor.
     *
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
    public void processTypeElement(final TypeElement annotation, final TypeElement element, final Collection<ExecutableElement> methods) {

        if (generatedFilesPath != null) {

            if (element.getKind().isInterface()) {
                String packageName = elementUtils().getPackageOf(element).getQualifiedName().toString();
                String className = element.getSimpleName().toString();
                String path = packageName.replaceAll("\\.", System.getProperty("file.separator"));
                String fileName = className + GENERATED_FILE_EXTENSION;

                //Check if it's an inner bundle
                Element enclosingElement = element.getEnclosingElement();
                while (enclosingElement != null && enclosingElement instanceof TypeElement) {
                    fileName = enclosingElement.getSimpleName().toString() + "$" + fileName;
                    enclosingElement = enclosingElement.getEnclosingElement();
                }

                Map<String, String> translationMessages = ElementHelper.getAllMessageMethods(methods);
                this.generateSkeletalTranslationFile(path, fileName, translationMessages);
            }

        }

    }

    /**
     * Generate the translation file containing the given
     * translations.
     *
     * @param fileName     the file name
     * @param translations the translations
     */
    public void generateSkeletalTranslationFile(final String path, final String fileName, final Map<String, String> translations) {
        if (translations == null) {
            throw new NullPointerException("The translations parameter cannot be null");
        }

        File pathFile = new File(generatedFilesPath, path);
        pathFile.mkdirs();

        File file = new File(pathFile, fileName);
        BufferedWriter writer = null;

        try {

            writer = new BufferedWriter(new FileWriter(file));

            for (String key : translations.keySet()) {
                String property = translations.get(key);
                writer.write(String.format("# %s", property));
                writer.newLine();
                writer.write(String.format("%s=", key));
                writer.newLine();
            }

        }
        catch (IOException e) {
            logger().error(e, "Cannot write generated skeletal translation file %s", fileName);
        } finally {
            try {

                writer.close();

            } catch (IOException e) {
                logger().error(e, "Cannot close generated skeletal translation file %s", fileName);
            }
        }


    }

}
