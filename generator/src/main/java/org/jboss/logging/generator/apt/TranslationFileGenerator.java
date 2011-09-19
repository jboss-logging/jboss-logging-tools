package org.jboss.logging.generator.apt;

import org.jboss.logging.generator.intf.model.MessageInterface;
import org.jboss.logging.generator.intf.model.Method;
import org.jboss.logging.generator.util.Strings;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.jboss.logging.generator.util.ElementHelper.getPrimaryClassNamePrefix;

/**
 * The generator of skeletal
 * translations files.
 *
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@SupportedOptions(TranslationFileGenerator.GENERATED_FILES_PATH_OPTION)
final class TranslationFileGenerator extends AbstractGenerator {

    public static final String GENERATED_FILES_PATH_OPTION = "generatedTranslationFilesPath";

    public static final String GENERATED_FILE_EXTENSION = ".i18n_locale_COUNTRY_VARIANT.properties";

    public static final String DEFAULT_FILE_EXTENSION = ".i18n.properties";

    private static final String DEFAULT_FILE_COMMENT = "# This file is for reference only, changes have no effect on the generated interface implementations.";

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

    @Override
    public void processTypeElement(final TypeElement annotation, final TypeElement element, final MessageInterface messageInterface) {
        if (generatedFilesPath != null) {
            if (element.getKind().isInterface()) {
                String packageName = elementUtils().getPackageOf(element).getQualifiedName().toString();
                String relativePath = packageName.replace('.', File.separatorChar);
                String fileName = getPrimaryClassNamePrefix(element) + GENERATED_FILE_EXTENSION;

                this.generateSkeletalTranslationFile(relativePath, fileName, messageInterface);
            }
        }
        // Always generate an Interface.i18n.properties file.
        generateDefaultTranslationFile(messageInterface);
    }

    /**
     * Generate the translation file containing the given
     * translations.
     *
     * @param relativePath     the relative path
     * @param fileName         the file name
     * @param messageInterface the message interface
     */
    void generateSkeletalTranslationFile(final String relativePath, final String fileName, final MessageInterface messageInterface) {
        if (messageInterface == null) {
            throw new NullPointerException("The translations parameter cannot be null");
        }

        File pathFile = new File(generatedFilesPath, relativePath);
        pathFile.mkdirs();

        File file = new File(pathFile, fileName);
        BufferedWriter writer = null;

        try {

            writer = new BufferedWriter(new FileWriter(file));
            final Set<String> processed = new HashSet<String>();

            for (Method method : messageInterface.methods()) {
                if (processed.add(method.translationKey())) {
                    writer.write(String.format("# %s", method.message().value()));
                    writer.newLine();
                    writer.write(String.format("%s=", method.translationKey()));
                    writer.newLine();
                }
            }

        } catch (IOException e) {
            logger().error(e, "Cannot write generated skeletal translation file %s", fileName);
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                logger().error(e, "Cannot close generated skeletal translation file %s", fileName);
            }
        }


    }

    /**
     * Generates a default i18n properties file.
     *
     * @param messageInterface the message interface
     */
    private void generateDefaultTranslationFile(final MessageInterface messageInterface) {
        final String fileName = messageInterface.simpleName() + DEFAULT_FILE_EXTENSION;
        BufferedWriter writer = null;

        try {
            final FileObject fileObject = filer().createResource(StandardLocation.CLASS_OUTPUT, messageInterface.packageName(), fileName);
            writer = new BufferedWriter(new OutputStreamWriter(fileObject.openOutputStream()));
            // Write comments
            writer.write(Strings.fill("#", DEFAULT_FILE_COMMENT.length()));
            writer.newLine();
            writer.write("#");
            writer.newLine();
            writer.write(DEFAULT_FILE_COMMENT);
            writer.newLine();
            writer.write("#");
            writer.newLine();
            writer.write(Strings.fill("#", DEFAULT_FILE_COMMENT.length()));
            writer.newLine();
            writer.newLine();
            final Set<String> processed = new HashSet<String>();

            for (Method method : messageInterface.methods()) {
                if (processed.add(method.translationKey())) {
                    final Method.Message msg = method.message();
                    writer.write(String.format("# Id: %s", (msg.hasId() ? msg.id() : "none")));
                    writer.newLine();
                    writer.write(String.format("# Message: %s", msg.value()));
                    writer.newLine();
                    writer.write(String.format("%s=", method.translationKey()));
                    writer.write(method.message().value());
                    writer.newLine();
                }
            }

        } catch (IOException e) {
            logger().error(e, "Cannot write generated default translation file %s", fileName);
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                logger().error(e, "Cannot write generated default translation file %s", fileName);
            }
        }


    }

}
