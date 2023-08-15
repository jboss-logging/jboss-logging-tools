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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.Transform;
import org.jboss.logging.annotations.Transform.TransformType;
import org.jboss.logging.processor.model.LoggerMessageMethod;
import org.jboss.logging.processor.model.MessageInterface;
import org.jboss.logging.processor.model.MessageMethod;
import org.jboss.logging.processor.model.Parameter;
import org.jboss.logging.processor.validation.StringFormatValidator;

/**
 * The generator of skeletal
 * translations files.
 *
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@SuppressWarnings("MagicNumber")
@SupportedOptions({ TranslationFileGenerator.GENERATED_FILES_PATH_OPTION, TranslationFileGenerator.LEVEL_OPTION,
        TranslationFileGenerator.SKIP_INDEX })
final class TranslationFileGenerator extends AbstractGenerator {
    private static final Map<String, Integer> levels = new HashMap<>();

    private static final Pattern PATTERN = Pattern
            .compile("((@[a-zA-Z_0-9]+)\\s+([a-zA-Z_][a-zA-Z_0-9]*)\\s+([a-zA-Z_][a-zA-Z_0-9].*)\\s*)");

    private static final String EMPTY_STRING = "";
    private static final String JAVA_DOC_PARAM = "@param";

    private static final String DEFAULT_FILE_EXTENSION = ".i18n.properties";

    private static final String DEFAULT_FILE_COMMENT = "# This file is for reference only, changes have no effect on the generated interface implementations.";

    static final String GENERATED_FILES_PATH_OPTION = "generatedTranslationFilesPath";

    static final String GENERATED_FILE_EXTENSION = ".i18n_locale_COUNTRY_VARIANT.properties";

    static final String LEVEL_OPTION = "org.jboss.logging.tools.level";

    static final String SKIP_INDEX = "org.jboss.logging.tools.generated.skip.index";

    static {

        levels.put("ALL", Integer.MIN_VALUE);
        levels.put("CONFIG", 700);
        levels.put("DEBUG", 500);
        levels.put("ERROR", 1000);
        levels.put("FATAL", 1100);
        levels.put("FINE", 500);
        levels.put("FINER", 400);
        levels.put("FINEST", 300);
        levels.put("INFO", 800);
        levels.put("OFF", Integer.MAX_VALUE);
        levels.put("SEVERE", 1000);
        levels.put("TRACE", 400);
        levels.put("WARN", 900);
        levels.put("WARNING", 900);
    }

    private final String generatedFilesPath;
    private final LevelComparator comparator;
    private final boolean skipIndex;

    /**
     * The constructor.
     *
     * @param processingEnv the processing env
     */
    TranslationFileGenerator(final ProcessingEnvironment processingEnv) {
        super(processingEnv);
        Map<String, String> options = processingEnv.getOptions();
        this.generatedFilesPath = options.get(GENERATED_FILES_PATH_OPTION);
        String highLevel = options.get(LEVEL_OPTION);
        if (highLevel == null) {
            // Check for a system property
            highLevel = AccessController.doPrivileged(new PrivilegedAction<String>() {
                public String run() {
                    return System.getProperty(LEVEL_OPTION);
                }
            });
        }
        if (highLevel != null) {
            if (!levels.containsKey(highLevel)) {
                logger().error("Invalid property '%s' defined. The value %s is invalid.", LEVEL_OPTION, highLevel);
            }
            comparator = new LevelComparator(highLevel);
        } else {
            comparator = null;
        }
        final String value = options.get(SKIP_INDEX);
        this.skipIndex = options.containsKey(SKIP_INDEX) && (value == null || value.isEmpty() || Boolean.parseBoolean(value));
    }

    @Override
    public void processTypeElement(final TypeElement annotation, final TypeElement element,
            final MessageInterface messageInterface) {
        if (generatedFilesPath != null) {
            if (element.getKind().isInterface()) {
                String packageName = processingEnv.getElementUtils().getPackageOf(element).getQualifiedName().toString();
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
    private void generateSkeletalTranslationFile(final String relativePath, final String fileName,
            final MessageInterface messageInterface) {
        if (messageInterface == null) {
            throw new IllegalArgumentException("The translations parameter cannot be null");
        }

        File pathFile = new File(generatedFilesPath, relativePath);
        pathFile.mkdirs();

        File file = new File(pathFile, fileName);
        BufferedWriter writer = null;

        try {

            writer = new BufferedWriter(new FileWriter(file));
            final Set<String> processed = new HashSet<>();

            for (MessageMethod messageMethod : messageInterface.methods()) {
                if (isMethodWritable(messageMethod)) {
                    if (processed.add(messageMethod.translationKey())) {
                        writeSkeletonMessageMethod(writer, messageMethod);
                    }
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
            if (generatedFilesPath == null) {
                final FileObject fileObject = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT,
                        messageInterface.packageName(), fileName);
                // Note the FileObject#openWriter() is used here. The FileObject#openOutputStream() returns an output stream
                // that writes each byte separately which results in poor performance.
                writer = new BufferedWriter(fileObject.openWriter());
            } else {
                String relativePath = messageInterface.packageName().replace('.', File.separatorChar);
                final File path = new File(generatedFilesPath, relativePath);
                path.mkdirs();
                final File file = new File(path, fileName);
                writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "utf-8"));
            }
            // Write comments
            writeSeparatorLine(writer);
            writer.write("#");
            writer.newLine();
            writer.write(DEFAULT_FILE_COMMENT);
            writer.newLine();
            writer.write("#");
            writer.newLine();
            writeSeparatorLine(writer);
            writer.newLine();
            final Set<String> processed = new HashSet<>();

            for (MessageMethod messageMethod : messageInterface.methods()) {
                if (isMethodWritable(messageMethod)) {
                    if (processed.add(messageMethod.translationKey())) {
                        writeSkeletonMessageMethod(writer, messageMethod);
                    }
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

    private void writeSkeletonMessageMethod(final BufferedWriter writer, final MessageMethod messageMethod) throws IOException {
        final MessageMethod.Message msg = messageMethod.message();
        writer.write(String.format("# Id: %s", (msg.hasId() ? msg.id() : "none")));
        writer.newLine();
        if (messageMethod instanceof LoggerMessageMethod) {
            writer.write(String.format("# Level: %s", ((LoggerMessageMethod) messageMethod).logLevel()));
            writer.newLine();
        }
        writer.write(String.format("# Message: %s", msg.value()));
        writer.newLine();
        final Map<String, String> parameterComments = parseParameterComments(messageMethod);
        int i = 0;
        for (Parameter parameter : messageMethod.parameters()) {
            final String name = parameter.name();
            final String comment = (parameterComments.containsKey(name) ? parameterComments.get(name) : EMPTY_STRING);
            if (parameter.isAnnotatedWith(Transform.class)) {
                final List<TransformType> transformTypes = Arrays.asList(parameter.getAnnotation(Transform.class).value());
                if (transformTypes.contains(TransformType.GET_CLASS)) {
                    if (transformTypes.size() == 1) {
                        writer.write(String.format("# @param class of %s - %s", name, comment));
                    } else if (transformTypes.contains(TransformType.HASH_CODE)) {
                        writer.write(String.format("# @param hashCode of class of %s - %s", name, comment));
                    } else if (transformTypes.contains(TransformType.IDENTITY_HASH_CODE)) {
                        writer.write(String.format("# @param identityHashCode of class of %s - %s", name, comment));
                    }
                } else if (transformTypes.contains(TransformType.HASH_CODE)) {
                    writer.write(String.format("# @param hashCode of %s - %s", name, comment));
                } else if (transformTypes.contains(TransformType.IDENTITY_HASH_CODE)) {
                    writer.write(String.format("# @param identityHashCode of %s - %s", name, comment));
                } else if (transformTypes.contains(TransformType.SIZE)) {
                    if (parameter.isArray() || parameter.isVarArgs() || parameter.isSubtypeOf(String.class)) {
                        writer.write(String.format("# @param length of %s - %s", name, comment));
                    } else {
                        writer.write(String.format("# @param size of %s - %s", name, comment));
                    }
                }
                writer.newLine();
            } else if (parameter.isFormatParameter()) {
                writer.write(String.format("# @param %d: %s - %s", ++i, name, comment));
                writer.newLine();
            }
        }
        writer.write(String.format("%s=", messageMethod.translationKey()));
        if (!skipIndex && messageMethod.message().format() == Message.Format.PRINTF) {
            writer.write(addIndexesToFormat(messageMethod));
        } else {
            writer.write(messageMethod.message().value());
        }
        writer.newLine();
    }

    private Map<String, String> parseParameterComments(final MessageMethod messageMethod) throws IOException {
        final Map<String, String> result = new LinkedHashMap<>();
        final String comment = messageMethod.getComment();
        if (comment != null) {
            final Matcher matcher = PATTERN.matcher(comment);
            while (matcher.find()) {
                if (matcher.groupCount() > 3) {
                    final String annotation = matcher.group(2);
                    if (annotation != null && annotation.trim().equals(JAVA_DOC_PARAM))
                        result.put(matcher.group(3), matcher.group(4));
                }
            }
        }
        return result;
    }

    private boolean isMethodWritable(final MessageMethod method) {
        if (method instanceof LoggerMessageMethod) {
            if (comparator != null) {
                return comparator.compareTo(((LoggerMessageMethod) method).logLevel()) >= 0;
            }
        }
        return true;
    }

    private static void writeSeparatorLine(final BufferedWriter writer) throws IOException {
        final int len = DEFAULT_FILE_COMMENT.length();
        for (int i = 0; i < len; i++) {
            writer.append('#');
        }
        writer.newLine();
    }

    /**
     * Returns the primary class simple name prefix for an element
     * who represents a MessageBundle or MessageLogger interface.
     *
     * @param element the element
     *
     * @return the translation file name prefix
     *
     * @throws IllegalArgumentException if element is null or the element is not an interface
     */
    private static String getPrimaryClassNamePrefix(final TypeElement element) {
        if (element == null) {
            throw new IllegalArgumentException("The element parameter cannot be null");
        }
        if (!element.getKind().isInterface()) {
            throw new IllegalArgumentException("The element parameter is not an interface");
        }

        String translationFileName = element.getSimpleName().toString();

        //Check if it's an inner interface
        Element enclosingElt = element.getEnclosingElement();
        while (enclosingElt != null && enclosingElt instanceof TypeElement) {
            translationFileName = String.format("%s$%s", enclosingElt.getSimpleName().toString(), translationFileName);
            enclosingElt = enclosingElt.getEnclosingElement();
        }

        return translationFileName;
    }

    private String addIndexesToFormat(final MessageMethod method) {
        final String format = method.message().value();
        int pos = 0;
        int i = 0;
        final Matcher matcher = StringFormatValidator.PATTERN.matcher(format);
        final StringBuilder newFormat = new StringBuilder();
        while (i < format.length()) {
            if (matcher.find(i)) {
                if (matcher.start() != i) {
                    newFormat.append(format, i, matcher.start());
                }
                // Pattern should produce 6 groups.
                if (matcher.groupCount() != 6) {
                    logger().warn(method, "Invalid format using \"%s\" for the skeleton value.", format);
                    return format;
                }
                // The % is stripped so we need to add it back
                newFormat.append('%');
                // Initialize the parts so they can be added if they're not null
                final String index = matcher.group(1);
                final String flags = matcher.group(2);
                final String width = matcher.group(3);
                final String precision = matcher.group(4);
                final String t = matcher.group(5);
                final char conversion = matcher.group(6).charAt(0);

                if (index == null) {
                    // If the flags contain < that's a previous index identifier so we should not replace it. Also new
                    // line (%n) and an escaped percent (%%) do not allow index arguments.
                    if (flags != null && !flags.contains("<") && conversion != 'n' && conversion != '%') {
                        newFormat.append(++pos).append('$');
                    }
                } else {
                    // The index already exists so we'll use that
                    newFormat.append(index);
                }
                if (flags != null) {
                    newFormat.append(flags);
                }
                if (width != null) {
                    newFormat.append(width);
                }
                if (precision != null) {
                    newFormat.append(precision);
                }
                if (t != null) {
                    newFormat.append(t);
                }
                newFormat.append(conversion);
                i = matcher.end();
            } else {
                // No more formats found, but validate for invalid remaining characters.
                newFormat.append(format, i, format.length());
                break;
            }
        }

        final String result = newFormat.toString();
        // Validate if the changed format is valid and if not fallback to the default format from the message annotation
        final StringFormatValidator validator = StringFormatValidator.of(result);
        if (!validator.isValid()) {
            logger().warn(method, "Could not properly use indexes in the format %s.", format);
            return format;
        }
        return result;
    }

    private static final class LevelComparator implements Comparable<String> {
        private final Integer levelIntValue;

        private LevelComparator(final String level) {
            levelIntValue = levels.get(level);
            if (levelIntValue == null) {
                throw new IllegalArgumentException(String.format("Level %s is invalid.", level));
            }
        }

        @Override
        public int compareTo(final String o) {
            String cmpLevel = o;
            // Get the actual level
            final int lastDot = o.lastIndexOf(".");
            if (lastDot > -1) {
                cmpLevel = o.substring(lastDot + 1);
            }
            final Integer level = levels.get(cmpLevel);
            if (level == null) {
                throw new IllegalArgumentException(String.format("Level %s is invalid.", cmpLevel));
            }
            return level.compareTo(levelIntValue);
        }
    }

}
