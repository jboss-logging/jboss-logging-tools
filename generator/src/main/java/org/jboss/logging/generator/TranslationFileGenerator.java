package org.jboss.logging.generator;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.element.TypeElement;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.jboss.logging.generator.util.ElementHelper.getPrimaryClassNamePrefix;

/**
 * The generator of skeletal
 * translations files.
 *
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 */
@SupportedOptions(TranslationFileGenerator.GENERATED_FILES_PATH_OPTION)
public final class TranslationFileGenerator extends AbstractTool {

    public static final String GENERATED_FILES_PATH_OPTION = "generatedTranslationFilesPath";

    public static final String GENERATED_FILE_EXTENSION = ".i18n_locale_COUNTRY_VARIANT.properties";

    private static final String FILE_SEPARATOR = System.getProperty("file.separator");

    private final String generatedFilesPath;

    /**
     * The constructor.
     *
     * @param processingEnv the processing env
     */
    public TranslationFileGenerator(final ProcessingEnvironment processingEnv) {
        super(processingEnv);

        Map<String, String> options = processingEnv.getOptions();
        this.generatedFilesPath = options.get(GENERATED_FILES_PATH_OPTION);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processTypeElement(final TypeElement annotation, final TypeElement element, final MethodDescriptors methodDescriptors) {

        if (generatedFilesPath != null) {

            if (element.getKind().isInterface()) {
                String packageName = elementUtils().getPackageOf(element).getQualifiedName().toString();
                String relativePath = packageName.replaceAll("\\.", FILE_SEPARATOR);
                String fileName = getPrimaryClassNamePrefix(element) + GENERATED_FILE_EXTENSION;

                this.generateSkeletalTranslationFile(relativePath, fileName, methodDescriptors);
            }

        }

    }

    /**
     * Generate the translation file containing the given
     * translations.
     *
     * @param relativePath the relative path
     * @param fileName     the file name
     * @param translations the translations
     */
    public void generateSkeletalTranslationFile(final String relativePath, final String fileName, final MethodDescriptors methodDescriptors) {
        if (methodDescriptors == null) {
            throw new NullPointerException("The translations parameter cannot be null");
        }

        File pathFile = new File(generatedFilesPath, relativePath);
        pathFile.mkdirs();

        File file = new File(pathFile, fileName);
        BufferedWriter writer = null;

        try {

            writer = new BufferedWriter(new FileWriter(file));
            final Set<String> processed = new HashSet<String>();

            for (MethodDescriptor methodDescriptor : methodDescriptors) {
                if (processed.add(methodDescriptor.translationKey())) {
                    writer.write(String.format("# %s", methodDescriptor.messageValue()));
                    writer.newLine();
                    writer.write(String.format("%s=", methodDescriptor.translationKey()));
                    writer.newLine();
                }
            }

        } catch (IOException e) {
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
