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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.Type;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaClassSource;
import org.jboss.forge.roaster.model.source.JavaInterfaceSource;
import org.jboss.forge.roaster.model.source.MethodSource;
import org.jboss.forge.roaster.model.source.ParameterSource;
import org.jboss.logging.DelegatingBasicLogger;
import org.jboss.logging.Logger;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class GeneratedSourceAnalysisTest {

    private static String TEST_SRC_PATH = null;
    private static String TEST_GENERATED_SRC_PATH = null;

    @BeforeClass
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
    }

    private void compareLogger(final Class<?> intf) throws IOException {
        final JavaInterfaceSource interfaceSource = parseInterface(intf);
        final JavaClassSource implementationSource = parseGenerated(intf);
        compareCommon(interfaceSource, implementationSource);

        // Logger implementations should have a single constructor which accepts a org.jboss.logging.Logger
        final List<MethodSource<JavaClassSource>> implementationMethods = implementationSource.getMethods();
        final MethodSource<JavaClassSource> constructor = findConstructor(implementationMethods);
        Assert.assertNotNull(constructor, "No constructor found for " + implementationSource.getName());
        final List<ParameterSource<JavaClassSource>> parameters = constructor.getParameters();
        Assert.assertEquals(parameters.size(), 1, "Found more than one parameter for " + implementationSource.getName() + ": " + parameters);
        final ParameterSource<JavaClassSource> parameter = parameters.get(0);
        final Type<JavaClassSource> type = parameter.getType();
        Assert.assertEquals(type.getQualifiedName(), Logger.class.getName());

        // If the logger is not extending the DelegatingBasicLogger there should be a protected final org.jboss.logging.Logger field
        if (!DelegatingBasicLogger.class.getName().equals(implementationSource.getSuperType())) {
            final FieldSource<JavaClassSource> log = implementationSource.getField("log");
            Assert.assertNotNull(log, "Expected a log field in " + implementationSource.getName());
            Assert.assertTrue(log.isProtected() && log.isFinal(),
                    "Expected the log field to be protected and final in " + implementationSource.getName());
        }
    }

    private void compareBundle(final Class<?> intf) throws IOException {
        final JavaInterfaceSource interfaceSource = parseInterface(intf);
        final JavaClassSource implementationSource = parseGenerated(intf);
        compareCommon(interfaceSource, implementationSource);

        // Message bundles should have an INSTANCE field
        final FieldSource<JavaClassSource> instance = implementationSource.getField("INSTANCE");
        Assert.assertNotNull(instance, "Expected an INSTANCE field in " + implementationSource.getName());
        Assert.assertTrue(instance.isStatic() && instance.isFinal() && instance.isPublic(),
                "Expected the instance field to be public, static and final in " + implementationSource.getName());

        // Expect a protected constructor with no parameters
        final MethodSource<JavaClassSource> constructor = findConstructor(implementationSource.getMethods());
        Assert.assertNotNull(constructor, "No constructor found for " + implementationSource.getName());
        Assert.assertTrue(constructor.getParameters().isEmpty(), "Expected the constructor parameters to be empty for " + implementationSource.getName());
        Assert.assertTrue(constructor.isProtected(), "Expected the constructor to be protected for " + implementationSource.getName());
    }

    private void compareCommon(final JavaInterfaceSource interfaceSource, final JavaClassSource implementationSource) {
        final List<MethodSource<JavaInterfaceSource>> interfaceMethods = interfaceSource.getMethods();
        final List<MethodSource<JavaClassSource>> implementationMethods = implementationSource.getMethods();

        // Validate the implementation has all the interface methods, note this should be the cause
        final Collection<String> interfaceMethodNames = toNames(interfaceMethods);
        final Collection<String> implementationMethodNames = toNames(implementationMethods);
        Assert.assertTrue(implementationMethodNames.containsAll(interfaceMethodNames),
                String.format("Implementation is missing methods from the interface:%n\timplementation: %s%n\tinterface:%s", implementationMethodNames, interfaceMethodNames));

        // The generates source files should have a serialVersionUID with a value of one
        Assert.assertTrue(implementationSource.hasField("serialVersionUID"), "Expected a serialVersionUID field in " + implementationSource.getName());
        final FieldSource<JavaClassSource> serialVersionUID = implementationSource.getField("serialVersionUID");
        Assert.assertEquals(serialVersionUID.getLiteralInitializer(), "1L", "Expected serialVersionUID  to be set to 1L in " + implementationSource.getName());
    }

    private MethodSource<JavaClassSource> findConstructor(final List<MethodSource<JavaClassSource>> implementationMethods) {
        for (MethodSource<JavaClassSource> method : implementationMethods) {
            if (method.isConstructor()) {
                return method;
            }
        }
        return null;
    }

    private Collection<String> toNames(final List<? extends MethodSource<?>> methods) {
        final Collection<String> names = new ArrayList<>();
        for (MethodSource<?> method : methods) {
            if (!method.isStatic() && !method.isConstructor()) {
                names.add(method.getName());
            }
        }
        return names;
    }

    private String packageToPath(final Package pkg) {
        String result = pkg.getName().replace('.', File.separatorChar);
        return result.endsWith(File.separator) ? result : result + File.separator;
    }

    private JavaClassSource parseGenerated(final Class<?> intf) throws IOException {
        final Pattern pattern = Pattern.compile(Pattern.quote(intf.getSimpleName()) + "_\\$(logger|bundle)\\.java$");
        // Find all the files that match
        final FileFilter filter = new FileFilter() {
            @Override
            public boolean accept(final File pathname) {
                return pattern.matcher(pathname.getName()).find();
            }
        };
        final File dir = new File(TEST_GENERATED_SRC_PATH, packageToPath(intf.getPackage()));
        final File[] files = dir.listFiles(filter);
        // There should only be one file
        Assert.assertNotNull(files, "Did not find any implementation files for interface " + intf.getName());
        Assert.assertEquals(1, files.length, "Found more than one implementation for interface " + intf.getName() + " " + Arrays.asList(files));

        return Roaster.parse(JavaClassSource.class, files[0]);
    }

    private JavaInterfaceSource parseInterface(final Class<?> intf) throws IOException {
        final File srcFile = new File(TEST_SRC_PATH, packageToPath(intf.getPackage()) +
                intf.getSimpleName() + ".java");
        return Roaster.parse(JavaInterfaceSource.class, srcFile);
    }
}
