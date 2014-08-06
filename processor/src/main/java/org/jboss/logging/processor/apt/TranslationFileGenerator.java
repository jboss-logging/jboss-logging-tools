/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

import static org.jboss.logging.processor.util.ElementHelper.getPrimaryClassNamePrefix;

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
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import org.jboss.logging.annotations.Transform.TransformType;
import org.jboss.logging.processor.model.MessageInterface;
import org.jboss.logging.processor.model.MessageMethod;
import org.jboss.logging.processor.model.Parameter;
import org.jboss.logging.processor.model.Parameter.ParameterType;
import org.jboss.logging.processor.util.Strings;

/**
 * The generator of skeletal
 * translations files.
 *
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@SupportedOptions(TranslationFileGenerator.GENERATED_FILES_PATH_OPTION)
final class TranslationFileGenerator extends AbstractGenerator {
    private static final Map<String, Integer> levels = new HashMap<String, Integer>();

    private static final Pattern PATTERN = Pattern.compile("((@[a-zA-Z_0-9]+)\\s+([a-zA-Z_][a-zA-Z_0-9]*)\\s+([a-zA-Z_][a-zA-Z_0-9].*)\\s*)");

    public static final String EMPTY_STRING = "";
    public static final String JAVA_DOC_PARAM = "@param";

    public static final String GENERATED_FILES_PATH_OPTION = "generatedTranslationFilesPath";

    public static final String LEVEL_OPTION = "org.jboss.logging.tools.level";

    public static final String GENERATED_FILE_EXTENSION = ".i18n_locale_COUNTRY_VARIANT.properties";

    public static final String DEFAULT_FILE_EXTENSION = ".i18n.properties";

    private static final String DEFAULT_FILE_COMMENT = "# This file is for reference only, changes have no effect on the generated interface implementations.";

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

    /**
     * The constructor.
     *
     * @param processingEnv the processing env
     */
    public TranslationFileGenerator(final ProcessingEnvironment processingEnv) {
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
            throw new IllegalArgumentException("The translations parameter cannot be null");
        }

        File pathFile = new File(generatedFilesPath, relativePath);
        pathFile.mkdirs();

        File file = new File(pathFile, fileName);
        BufferedWriter writer = null;

        try {

            writer = new BufferedWriter(new FileWriter(file));
            final Set<String> processed = new HashSet<String>();

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
                final FileObject fileObject = filer().createResource(StandardLocation.CLASS_OUTPUT, messageInterface.packageName(), fileName);
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
        if (messageMethod.isLoggerMethod()) {
            writer.write(String.format("# Level: %s", messageMethod.logLevel()));
            writer.newLine();
        }
        writer.write(String.format("# Message: %s", msg.value()));
        writer.newLine();
        final Map<String, String> parameterComments = parseParameterComments(messageMethod);
        final Set<Parameter> parameters = messageMethod.parameters(ParameterType.FORMAT, ParameterType.TRANSFORM);
        int i = 0;
        for (Parameter parameter : parameters) {
            final String name = parameter.name();
            final String comment = (parameterComments.containsKey(name) ? parameterComments.get(name) : EMPTY_STRING);
            if (parameter.parameterType() == ParameterType.TRANSFORM) {
                final List<TransformType> transformTypes = Arrays.asList(parameter.transform().value());
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
            } else {
                writer.write(String.format("# @param %d: %s - %s", ++i, name, comment));
                writer.newLine();
            }
        }
        writer.write(String.format("%s=", messageMethod.translationKey()));
        writer.write(messageMethod.message().value());
        writer.newLine();
    }

    private Map<String, String> parseParameterComments(final MessageMethod messageMethod) throws IOException {
        final Map<String, String> result = new LinkedHashMap<String, String>();
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
        return !(comparator != null && method.isLoggerMethod()) || (comparator.compareTo(method.logLevel()) >= 0);
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
