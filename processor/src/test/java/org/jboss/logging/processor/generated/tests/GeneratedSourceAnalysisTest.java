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

package org.jboss.logging.processor.generated.tests;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.Method;
import org.jboss.forge.roaster.model.Named;
import org.jboss.forge.roaster.model.Type;
import org.jboss.forge.roaster.model.source.AnnotationSource;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.jboss.forge.roaster.model.source.ParameterSource;
import org.jboss.logging.DelegatingBasicLogger;
import org.jboss.logging.Logger;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.processor.generated.DefaultLogger;
import org.jboss.logging.processor.generated.DefaultMessages;
import org.jboss.logging.processor.generated.ExtendedLogger;
import org.jboss.logging.processor.generated.LogOnceLogger;
import org.jboss.logging.processor.generated.RootLocaleLogger;
import org.jboss.logging.processor.generated.TransformLogger;
import org.jboss.logging.processor.generated.ValidLogger;
import org.jboss.logging.processor.generated.ValidMessages;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class GeneratedSourceAnalysisTest {

    private static final Map<Locale, String> LOCALE_CONSTANTS = new LinkedHashMap<>();
    private static String TEST_SRC_PATH = null;
    private static String TEST_GENERATED_SRC_PATH = null;

    static {
        LOCALE_CONSTANTS.put(Locale.CANADA, "Locale.CANADA");
        LOCALE_CONSTANTS.put(Locale.CANADA_FRENCH, "Locale.CANADA_FRENCH");
        LOCALE_CONSTANTS.put(Locale.CHINESE, "Locale.CHINESE");
        LOCALE_CONSTANTS.put(Locale.ENGLISH, "Locale.ENGLISH");
        LOCALE_CONSTANTS.put(Locale.FRANCE, "Locale.FRANCE");
        LOCALE_CONSTANTS.put(Locale.FRENCH, "Locale.FRENCH");
        LOCALE_CONSTANTS.put(Locale.GERMAN, "Locale.GERMAN");
        LOCALE_CONSTANTS.put(Locale.GERMANY, "Locale.GERMANY");
        LOCALE_CONSTANTS.put(Locale.ITALIAN, "Locale.ITALIAN");
        LOCALE_CONSTANTS.put(Locale.ITALY, "Locale.ITALY");
        LOCALE_CONSTANTS.put(Locale.JAPAN, "Locale.JAPAN");
        LOCALE_CONSTANTS.put(Locale.JAPANESE, "Locale.JAPANESE");
        LOCALE_CONSTANTS.put(Locale.KOREA, "Locale.KOREA");
        LOCALE_CONSTANTS.put(Locale.KOREAN, "Locale.KOREAN");
        LOCALE_CONSTANTS.put(Locale.SIMPLIFIED_CHINESE, "Locale.SIMPLIFIED_CHINESE");
        LOCALE_CONSTANTS.put(Locale.TRADITIONAL_CHINESE, "Locale.TRADITIONAL_CHINESE");
        LOCALE_CONSTANTS.put(Locale.UK, "Locale.UK");
        LOCALE_CONSTANTS.put(Locale.US, "Locale.US");
    }

    @BeforeAll
    public static void setUp() {
        TEST_SRC_PATH = System.getProperty("test.src.path");
        TEST_GENERATED_SRC_PATH = System.getProperty("test.generated.src.path");
    }

    @Test
    public void testBundles() throws Exception {
        compareBundle(DefaultMessages.class);
        compareBundle(ValidMessages.class);
    }

    @Test
    public void testLoggers() throws Exception {
        compareLogger(DefaultLogger.class);
        compareLogger(ExtendedLogger.class);
        compareLogger(ValidLogger.class);
        compareLogger(RootLocaleLogger.class);
        compareLogger(TransformLogger.class);
        compareLogger(LogOnceLogger.class);
    }

    @Test
    public void testGeneratedTranslations() throws Exception {
        compareTranslations(DefaultLogger.class);
        compareTranslations(DefaultMessages.class);
        compareTranslations(RootLocaleLogger.class);
    }

    @Test
    public void testRootLocale() throws Exception {
        JavaClassSource implementationSource = parseGenerated(RootLocaleLogger.class);
        FieldSource<JavaClassSource> locale = implementationSource.getField("LOCALE");
        Assertions.assertNotNull(locale, "Expected a LOCALE field for " + implementationSource.getName());
        Assertions.assertEquals("Locale.forLanguageTag(\"en-UK\")", locale.getLiteralInitializer());

        implementationSource = parseGenerated(DefaultLogger.class);
        locale = implementationSource.getField("LOCALE");
        Assertions.assertNotNull(locale, "Expected a LOCALE field for " + implementationSource.getName());
        Assertions.assertEquals("Locale.ROOT", locale.getLiteralInitializer());
    }

    private void compareLogger(final Class<?> intf) throws IOException {
        final JavaInterfaceSource interfaceSource = parseInterface(intf);
        final JavaClassSource implementationSource = parseGenerated(intf);
        compareCommon(interfaceSource, implementationSource);

        // Logger implementations should have a single constructor which accepts a org.jboss.logging.Logger
        final List<MethodSource<JavaClassSource>> implementationMethods = implementationSource.getMethods();
        final Optional<MethodSource<JavaClassSource>> constructor = findConstructor(implementationMethods);
        Assertions.assertTrue(constructor.isPresent(), "No constructor found for " + implementationSource.getName());
        final List<ParameterSource<JavaClassSource>> parameters = constructor.get().getParameters();
        Assertions.assertEquals(1, parameters.size(),
                "Found more than one parameter for " + implementationSource.getName() + ": " + parameters);
        final ParameterSource<JavaClassSource> parameter = parameters.get(0);
        final Type<JavaClassSource> type = parameter.getType();
        Assertions.assertEquals(Logger.class.getName(), type.getQualifiedName());

        // If the logger is not extending the DelegatingBasicLogger there should be a protected final org.jboss.logging.Logger field
        if (!DelegatingBasicLogger.class.getName().equals(implementationSource.getSuperType())) {
            final FieldSource<JavaClassSource> log = implementationSource.getField("log");
            Assertions.assertNotNull(log, "Expected a log field in " + implementationSource.getName());
            Assertions.assertTrue(log.isProtected() && log.isFinal(),
                    "Expected the log field to be protected and final in " + implementationSource.getName());
        }

        // Check the interface for log messages that should wrap the TCCL
        final List<MethodSource<JavaInterfaceSource>> interfaceMethods = interfaceSource.getMethods();
        for (MethodSource<JavaInterfaceSource> method : interfaceMethods) {
            if (method.hasAnnotation(LogMessage.class)) {
                final AnnotationSource<JavaInterfaceSource> annotation = method.getAnnotation(LogMessage.class);
                final String value = annotation.getStringValue("useThreadContext");
                if (Boolean.parseBoolean(value)) {
                    // Find the implementation method
                    final MethodSource<JavaClassSource> implementationMethod = findImplementationMethod(method,
                            implementationMethods);
                    Assertions.assertNotNull(implementationMethod, "Could not find implementation method for " + method);
                    final String body = implementationMethod.getBody();
                    String[] lines = body.split("[\n\f\r]");
                    Assertions.assertTrue((lines.length > 5),
                            String.format("Expected at least 5 lines found %d: %s", lines.length, body));

                    // First line should be getting the current TCCL
                    Assertions.assertEquals("final ClassLoader currentTccl=Thread.currentThread().getContextClassLoader();",
                            lines[0].trim());
                    // The third line should be setting the log context
                    Assertions.assertEquals("Thread.currentThread().setContextClassLoader(this.getClass().getClassLoader());",
                            lines[2].trim());

                    lines = Arrays.copyOfRange(lines, 3, lines.length);
                    boolean finallyFound = false;
                    // The first line should be the message retrieval
                    for (String line : lines) {
                        // Process until we find a finally block
                        if (line.contains("finally {")) {
                            finallyFound = true;
                            continue;
                        }
                        if (finallyFound && !"}".equals(line)) {
                            Assertions.assertEquals("Thread.currentThread().setContextClassLoader(currentTccl);", line.trim());
                        }
                    }
                    Assertions.assertTrue(finallyFound, "Expected a finally block: " + body);
                }
            }
        }
    }

    private void compareBundle(final Class<?> intf) throws IOException {
        final JavaInterfaceSource interfaceSource = parseInterface(intf);
        final JavaClassSource implementationSource = parseGenerated(intf);
        compareCommon(interfaceSource, implementationSource);

        // Message bundles should have an INSTANCE field
        final FieldSource<JavaClassSource> instance = implementationSource.getField("INSTANCE");
        Assertions.assertNotNull(instance, "Expected an INSTANCE field in " + implementationSource.getName());
        Assertions.assertTrue(instance.isStatic() && instance.isFinal() && instance.isPublic(),
                "Expected the instance field to be public, static and final in " + implementationSource.getName());

        // Expect a protected constructor with no parameters
        final Optional<MethodSource<JavaClassSource>> constructor = findConstructor(implementationSource.getMethods());
        Assertions.assertTrue(constructor.isPresent(), "No constructor found for " + implementationSource.getName());
        final MethodSource<JavaClassSource> c = constructor.get();
        Assertions.assertTrue(c.getParameters().isEmpty(),
                "Expected the constructor parameters to be empty for " + implementationSource.getName());
        Assertions.assertTrue(c.isProtected(),
                "Expected the constructor to be protected for " + implementationSource.getName());
    }

    private void compareCommon(final JavaInterfaceSource interfaceSource, final JavaClassSource implementationSource) {
        final List<MethodSource<JavaInterfaceSource>> interfaceMethods = interfaceSource.getMethods();
        final List<MethodSource<JavaClassSource>> implementationMethods = implementationSource.getMethods();

        // Validate the implementation has all the interface methods, note this should be the cause
        final Collection<String> interfaceMethodNames = toNames(interfaceMethods);
        final Collection<String> implementationMethodNames = toNames(implementationMethods);
        Assertions.assertTrue(implementationMethodNames.containsAll(interfaceMethodNames),
                String.format("Implementation is missing methods from the interface:%n\timplementation: %s%n\tinterface:%s",
                        implementationMethodNames, interfaceMethodNames));

        // The generates source files should have a serialVersionUID with a value of one
        Assertions.assertTrue(implementationSource.hasField("serialVersionUID"),
                "Expected a serialVersionUID field in " + implementationSource.getName());
        final FieldSource<JavaClassSource> serialVersionUID = implementationSource.getField("serialVersionUID");
        Assertions.assertEquals("1L", serialVersionUID.getLiteralInitializer(),
                "Expected serialVersionUID  to be set to 1L in " + implementationSource.getName());

        // All bundles should have a getLoggingLocale()
        final MethodSource<JavaClassSource> getLoggingLocale = implementationSource.getMethod("getLoggingLocale");
        Assertions.assertNotNull(getLoggingLocale, "Expected a getLoggingLocale() method in " + implementationSource.getName());
        Assertions.assertTrue(getLoggingLocale.isProtected(),
                "Expected the getLoggingLocale() to be protected in " + implementationSource.getName());
    }

    private void compareTranslations(final Class<?> intf) throws IOException {
        final JavaInterfaceSource interfaceSource = parseInterface(intf);
        final Collection<JavaClassSource> implementations = parseGeneratedTranslations(intf);
        // Find the default source file
        final JavaClassSource superImplementationSource = parseGenerated(intf);
        for (JavaClassSource implementationSource : implementations) {
            compareTranslations(interfaceSource, superImplementationSource, implementationSource);
        }
    }

    private void compareTranslations(final JavaInterfaceSource interfaceSource, final JavaClassSource superImplementationSource,
            final JavaClassSource implementationSource) {
        // The implementations should not contain any methods from the interface
        final List<String> interfaceMethods = interfaceSource.getMethods()
                .stream()
                .map(Named::getName)
                .collect(Collectors.toList());
        final Collection<String> found = new ArrayList<>();
        for (MethodSource<JavaClassSource> method : implementationSource.getMethods()) {
            if (interfaceMethods.contains(method.getName())) {
                found.add(method.getName());
            }
        }
        Assertions.assertTrue(found.isEmpty(),
                "Found methods in implementation that were in the interface " + implementationSource.getName() + " : " + found);

        // The getLoggerLocale() should be overridden
        final MethodSource<JavaClassSource> getLoggerLocale = implementationSource.getMethod("getLoggingLocale");
        Assertions.assertNotNull(getLoggerLocale,
                "Missing overridden getLoggingLocale() method " + implementationSource.getName());

        // If the file should have a locale constant, validate the constant is one of the Locale constants
        LOCALE_CONSTANTS.forEach((locale, constant) -> {
            if (implementationSource.getName().endsWith(locale.toString())) {
                // Get the LOCALE field
                final FieldSource<JavaClassSource> localeField = implementationSource.getField("LOCALE");
                Assertions.assertNotNull(localeField, "Expected a LOCALE field " + implementationSource.getName());
                Assertions.assertEquals(constant, localeField.getLiteralInitializer(),
                        "Expected the LOCALE to be set to " + constant + " in " + implementationSource.getName());
            }
        });

        // Get all the method names from the super class
        final List<String> superMethods = superImplementationSource.getMethods()
                .stream()
                .filter(method -> !method.isConstructor())
                .map(Named::getName)
                .collect(Collectors.toList());

        // All methods in the translation implementation should be overrides of methods in the super class
        implementationSource.getMethods().forEach(method -> {
            if (!method.isConstructor()) {
                Assertions.assertTrue(method.hasAnnotation(Override.class),
                        String.format("Expected method %s to be overridden in %s.",
                                method.getName(), implementationSource.getName()));
                Assertions.assertTrue(superMethods.contains(method.getName()),
                        String.format("Expected method %s to override the super (%s) method in %s.",
                                method.getName(), superImplementationSource.getName(), implementationSource.getName()));
            }
        });
    }

    private Optional<MethodSource<JavaClassSource>> findConstructor(
            final List<MethodSource<JavaClassSource>> implementationMethods) {
        return implementationMethods.stream()
                .filter(Method::isConstructor)
                .findFirst();
    }

    private MethodSource<JavaClassSource> findImplementationMethod(final MethodSource<JavaInterfaceSource> interfaceMethod,
            final List<MethodSource<JavaClassSource>> implementationMethods) {
        for (MethodSource<JavaClassSource> method : implementationMethods) {
            if (interfaceMethod.getName().equals(method.getName())) {
                final List<ParameterSource<JavaInterfaceSource>> interfaceParams = interfaceMethod.getParameters();
                final List<ParameterSource<JavaClassSource>> parameters = method.getParameters();
                if (interfaceParams.size() == parameters.size()) {
                    boolean matched = true;
                    for (int i = 0; i < interfaceParams.size(); i++) {
                        final ParameterSource<JavaInterfaceSource> param1 = interfaceParams.get(i);
                        final ParameterSource<JavaClassSource> param2 = parameters.get(i);
                        if (!param1.getType().getQualifiedNameWithGenerics()
                                .equals(param2.getType().getQualifiedNameWithGenerics())) {
                            matched = false;
                        }
                    }
                    if (matched) {
                        return method;
                    }
                }
            }
        }
        return null;
    }

    private Collection<String> toNames(final List<? extends MethodSource<?>> methods) {
        return methods.stream()
                // Skip default methods, static methods and constructors
                .filter(m -> !m.isDefault() && !m.isStatic() && !m.isConstructor())
                .map(Named::getName)
                .collect(Collectors.toList());

    }

    private String packageToPath(final Package pkg) {
        String result = pkg.getName().replace('.', File.separatorChar);
        return result.endsWith(File.separator) ? result : result + File.separator;
    }

    private JavaClassSource parseGenerated(final Class<?> intf) throws IOException {
        final Pattern pattern = Pattern.compile(Pattern.quote(intf.getSimpleName()) + "_\\$(logger|bundle)\\.java$");
        // Find all the files that match
        final FileFilter filter = pathname -> pattern.matcher(pathname.getName()).find();
        final File dir = new File(TEST_GENERATED_SRC_PATH, packageToPath(intf.getPackage()));
        final File[] files = dir.listFiles(filter);
        // There should only be one file
        Assertions.assertNotNull(files, "Did not find any implementation files for interface " + intf.getName());
        Assertions.assertEquals(1, files.length,
                "Found more than one implementation for interface " + intf.getName() + " " + Arrays.asList(files));

        return Roaster.parse(JavaClassSource.class, files[0]);
    }

    private Collection<JavaClassSource> parseGeneratedTranslations(final Class<?> intf) throws IOException {
        final Pattern pattern = Pattern.compile(Pattern.quote(intf.getSimpleName()) + "_\\$(logger|bundle)_.*\\.java$");
        // Find all the files that match
        final FileFilter filter = pathname -> pattern.matcher(pathname.getName()).matches();
        final File dir = new File(TEST_GENERATED_SRC_PATH, packageToPath(intf.getPackage()));
        final File[] files = dir.listFiles(filter);
        // There should only be one file
        Assertions.assertNotNull(files, "Did not find any implementation files for interface " + intf.getName());
        Assertions.assertTrue(files.length > 0, "Did not find any translation implementations for interface " + intf.getName());
        final Collection<JavaClassSource> result = new ArrayList<>();
        for (final File file : files) {
            result.add(Roaster.parse(JavaClassSource.class, file));
        }
        return result;
    }

    private JavaInterfaceSource parseInterface(final Class<?> intf) throws IOException {
        final File srcFile = new File(TEST_SRC_PATH, packageToPath(intf.getPackage()) +
                intf.getSimpleName() + ".java");
        return Roaster.parse(JavaInterfaceSource.class, srcFile);
    }
}
