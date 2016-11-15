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

package org.jboss.logging.processor.generated;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class GeneratedSourceAnalysisTest {

    private static String TEST_SRC_PATH = null;
    private static String TEST_GENERATED_SRC_PATH = null;

    private final Pattern methodNamePattern = Pattern.compile("((?:\\s?|\\s+?)(?:public final)?\\s+)?([\\w\\.]+)\\s+(\\w+)(\\(.*)");
    private final Pattern fieldNamePattern = Pattern.compile("((?:\\s?|\\s+?)private static final\\s+)([\\w\\.]+String|String)(?:\\s+)(\\w+)(.*)");
    private final Pattern messageMethodPattern = Pattern.compile("((?:\\s?|\\s+?)protected\\s+)([\\w.]+String|String)(?:\\s+)(\\w+)(\\$str\\(\\))(.*)");

    @BeforeClass
    public static void setUp() {
        TEST_SRC_PATH = System.getProperty("test.src.path");
        TEST_GENERATED_SRC_PATH = System.getProperty("test.generated.src.path");
    }

    @Test
    public void testGeneratedMethodOrder() throws Exception {
        compare(DefaultLogger.class);
        compare(DefaultMessages.class);
        compare(ValidMessages.class);
    }

    private void compare(final Class<?> intf) throws Exception {
        final Descriptor intfDescriptor = parseInterface(intf);
        final Descriptors implDescriptors = parseGenerated(intf);
        final List<String> intfMethods = intfDescriptor.get(Descriptor.Type.METHOD);
        for (Descriptor implDescriptor : implDescriptors) {
            if (implDescriptor.filterNames) {
                List<String> l = new ArrayList<>();
                // We're only testing order, so only test methods that are in the implementation
                final List<String> implMessageMethods = implDescriptor.get(Descriptor.Type.MESSAGE_METHOD);
                Assert.assertFalse(implMessageMethods.isEmpty(), "No methods found in " + implDescriptor.filename);
                for (String s : intfMethods) {
                    if (implMessageMethods.contains(s)) {
                        l.add(s);
                    }
                }
                Result result = compare(l, implMessageMethods);
                if (result.failed) {
                    Assert.fail(String.format("Interface %s (%s) failed on %s; %s", intf.getName(), implDescriptor.filename, Descriptor.Type.MESSAGE_METHOD, result.message));
                }

                l = new ArrayList<>();
                // We're only testing order, so only test methods that are in the implementation
                final List<String> implFields = implDescriptor.get(Descriptor.Type.FIELD);
                Assert.assertFalse(implFields.isEmpty(), "No fields found in " + implDescriptor.filename);
                for (String s : intfMethods) {
                    if (implFields.contains(s)) {
                        l.add(s);
                    }
                }
                result = compare(l, implFields);
                if (result.failed) {
                    Assert.fail(String.format("Interface %s (%s) failed on %s; %s", intf.getName(), implDescriptor.filename, Descriptor.Type.FIELD, result.message));
                }
            } else {
                Result result = compare(intfMethods, implDescriptor.get(Descriptor.Type.METHOD));
                if (result.failed) {
                    Assert.fail(String.format("Interface %s (%s) failed on %s; %s", intf.getName(), implDescriptor.filename, Descriptor.Type.METHOD, result.message));
                }
                result = compare(intfMethods, implDescriptor.get(Descriptor.Type.FIELD));
                if (result.failed) {
                    Assert.fail(String.format("Interface %s (%s) failed on %s; %s", intf.getName(), implDescriptor.filename, Descriptor.Type.FIELD, result.message));
                }
                result = compare(intfMethods, implDescriptor.get(Descriptor.Type.MESSAGE_METHOD));
                if (result.failed) {
                    Assert.fail(String.format("Interface %s (%s) failed on %s; %s", intf.getName(), implDescriptor.filename, Descriptor.Type.MESSAGE_METHOD, result.message));
                }
            }
        }
    }

    private Result compare(final List<String> list1, final List<String> list2) {
        // If the size is different there's an error... ...in the test
        if (list1.size() != list2.size()) {
            return Result.failure("Expected a size of %d but was a size %d%n%s%n%s", list1.size(), list2.size(), list1, list2);
        }
        for (int i = 0; i < list1.size(); i++) {
            final String value1 = list1.get(i);
            final String value2 = list2.get(i);
            if (!value1.equals(value2)) {
                return Result.failure("Values don't match at %d. Expected '%s' found '%s'.", i, value1, value2);
            }
        }
        return Result.SUCCESS;
    }

    private String packageToPath(final Package pkg) {
        String result = pkg.getName().replace('.', File.separatorChar);
        return result.endsWith(File.separator) ? result : result + File.separator;
    }

    private Descriptors parseGenerated(final Class<?> intf) throws IOException {
        final Pattern pattern = Pattern.compile(Pattern.quote(intf.getSimpleName()) + ".*\\.java$");
        // Find all the files that match
        final FileFilter filter = new FileFilter() {
            @Override
            public boolean accept(final File pathname) {
                return pattern.matcher(pathname.getName()).find();
            }
        };
        final File dir = new File(TEST_GENERATED_SRC_PATH, packageToPath(intf.getPackage()));
        final File[] files = dir.listFiles(filter);
        final Descriptors result = new Descriptors();
        for (File file : files) {
            result.add(parse(file, true));
        }
        return result;
    }

    private Descriptor parseInterface(final Class<?> intf) throws IOException {
        final File srcFile = new File(TEST_SRC_PATH, packageToPath(intf.getPackage()) +
                intf.getSimpleName() + ".java");
        return parse(srcFile, false);
    }

    private Descriptor parse(final File file, final boolean isImpl) throws IOException {
        final Descriptor result = new Descriptor(file.getName());
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(file));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                if (line.isEmpty()) continue;
                final Matcher methodNameMatcher = methodNamePattern.matcher(line);
                if (methodNameMatcher.matches()) {
                    final String returnType = methodNameMatcher.group(2);
                    final String methodName = methodNameMatcher.group(3);
                    if (!"public".equals(returnType) || "private".equals(returnType)) {
                        result.addMethod(methodName);
                    }
                } else if (isImpl) {
                    final Matcher fieldNameMatcher = fieldNamePattern.matcher(line);
                    if (fieldNameMatcher.matches()) {
                        final String fieldName = fieldNameMatcher.group(3);
                        if (!"FQCN".equals(fieldName)) {
                            result.addField(fieldName.replaceAll("\\d+$", ""));
                        }
                    } else {
                        final Matcher messageMethodMatcher = messageMethodPattern.matcher(line);
                        if (messageMethodMatcher.matches()) {
                            final String methodName = messageMethodMatcher.group(3);
                            result.addMessageMethod(methodName.replaceAll("\\d+$", ""));
                        }
                    }
                }
            }
        } finally {
            safeClose(bufferedReader);
        }
        return result;
    }

    static void safeClose(final Closeable closeable) {
        if (closeable != null) try {
            closeable.close();
        } catch (Exception ignore) {
        }
    }

    private static class Result {
        static final Result SUCCESS = new Result(false, "");
        final boolean failed;
        final String message;

        private Result(final boolean failed, final String message) {
            this.failed = failed;
            this.message = message;
        }

        static Result failure(final String format, final Object... args) {
            return new Result(true, String.format(format, args));
        }
    }

    private static class Descriptors implements Iterable<Descriptor> {
        private final List<Descriptor> descriptors;

        private Descriptors() {
            this.descriptors = new ArrayList<>();
        }

        public boolean add(final Descriptor descriptor) {
            return descriptors.add(descriptor);
        }


        @Override
        public Iterator<Descriptor> iterator() {
            return descriptors.iterator();
        }
    }

    private static class Descriptor {
        static enum Type {
            METHOD,
            FIELD,
            MESSAGE_METHOD
        }

        final String filename;
        final boolean filterNames;
        private final Map<Type, List<String>> values;

        private Descriptor(final String filename) {
            this.filename = filename;
            this.filterNames = filename.matches(".*(\\$[a-z]+)(_\\w+)\\.java$");
            values = new HashMap<>();
        }

        public void addMethod(final String methodName) {
            getList(Type.METHOD).add(methodName);
        }

        public void addField(final String fieldName) {
            getList(Type.FIELD).add(fieldName);
        }

        public void addMessageMethod(final String methodName) {
            getList(Type.MESSAGE_METHOD).add(methodName);
        }

        public List<String> get(final Type type) {
            return Collections.unmodifiableList(getList(type));
        }

        private List<String> getList(final Type type) {
            if (values.containsKey(type)) {
                return values.get(type);
            }
            final List<String> result = new ArrayList<>();
            values.put(type, result);
            return result;
        }

    }
}
