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

package org.jboss.logging.processor.generator.model;

import static org.jboss.jdeparser.JExprs.$v;
import static org.jboss.jdeparser.JMod.FINAL;
import static org.jboss.jdeparser.JTypes.$t;
import static org.jboss.jdeparser.JTypes.typeOf;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;

import org.jboss.jdeparser.FormatPreferences;
import org.jboss.jdeparser.JBlock;
import org.jboss.jdeparser.JCall;
import org.jboss.jdeparser.JClassDef;
import org.jboss.jdeparser.JDeparser;
import org.jboss.jdeparser.JExpr;
import org.jboss.jdeparser.JExprs;
import org.jboss.jdeparser.JFiler;
import org.jboss.jdeparser.JMethodDef;
import org.jboss.jdeparser.JMod;
import org.jboss.jdeparser.JSourceFile;
import org.jboss.jdeparser.JSources;
import org.jboss.jdeparser.JType;
import org.jboss.jdeparser.JVarDeclaration;
import org.jboss.logging.annotations.MessageBundle;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.logging.processor.apt.ProcessingException;
import org.jboss.logging.processor.model.MessageInterface;
import org.jboss.logging.processor.model.MessageMethod;

/**
 * The basic java class model.
 *
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public abstract class ClassModel {

    private static final String INSTANCE_FIELD_NAME = "INSTANCE";

    private static final String GET_INSTANCE_METHOD_NAME = "readResolve";

    private final JSources sources;

    private final JClassDef classDef;

    private final MessageInterface messageInterface;

    private final String className;
    private final String superClassName;

    private final String format;

    private final Map<String, JMethodDef> messageMethods;


    final JSourceFile sourceFile;
    final ProcessingEnvironment processingEnv;

    /**
     * Construct a class model.
     *
     * @param processingEnv    the processing environment
     * @param messageInterface the message interface to implement.
     * @param superClassName   the super class used for the translation implementations.
     */
    ClassModel(final ProcessingEnvironment processingEnv, final MessageInterface messageInterface, final String className, final String superClassName) {
        this.processingEnv = processingEnv;
        this.messageInterface = messageInterface;
        this.className = messageInterface.packageName() + "." + className;
        this.superClassName = superClassName;
        sources = JDeparser.createSources(JFiler.newInstance(processingEnv.getFiler()), new FormatPreferences(new Properties()));
        sourceFile = sources.createSourceFile(messageInterface.packageName(), className);
        classDef = sourceFile._class(JMod.PUBLIC, className);
        final int idLen = messageInterface.getIdLength();
        if (idLen > 0) {
            format = "%s%0" + messageInterface.getIdLength() + "d: %s";
        } else {
            format = "%s%d: %s";
        }
        messageMethods = new HashMap<>();
    }

    /**
     * Returns the message interface being used.
     *
     * @return the message interface.
     */
    public final MessageInterface messageInterface() {
        return messageInterface;
    }

    /**
     * Writes the generated source file to the file system.
     *
     * @throws java.io.IOException if the file could not be written
     */
    public final void generateAndWrite() throws IOException {
        generateModel();
        sources.writeSources();
        JDeparser.dropCaches();
    }

    /**
     * Generate the code corresponding to this
     * class model
     *
     * @return the generated code
     *
     * @throws IllegalStateException if the class has already been defined.
     */
    JClassDef generateModel() throws IllegalStateException {
        // Add generated annotation if required
        final TypeElement generatedAnnotation = messageInterface.generatedAnnotation();
        if (generatedAnnotation != null) {
            final JType generatedType = typeOf(generatedAnnotation.asType());
            sourceFile._import(generatedType);
            classDef.annotate(generatedType)
                    .value("value", getClass().getName())
                    .value("date", JExprs.str(ClassModelHelper.generatedDateValue()));
        }

        // Create the default JavaDoc
        classDef.docComment().text("Warning this class consists of generated code.");

        // Add extends
        if (superClassName != null) {
            classDef._extends(superClassName);
        }

        // Always implement the interface
        classDef._implements(typeOf(messageInterface.asType()));

        //Add implements
        if (!messageInterface.extendedInterfaces().isEmpty()) {
            for (MessageInterface intf : messageInterface.extendedInterfaces()) {
                final JType interfaceName = typeOf(intf.asType());
                sourceFile._import(interfaceName);
                classDef._implements(interfaceName);
            }
        }
        final JType serializable = $t(Serializable.class);
        sourceFile._import(serializable);
        classDef._implements(serializable);
        classDef.field(JMod.PRIVATE | JMod.STATIC | FINAL, JType.LONG, "serialVersionUID", JExprs.decimal(1L));
        return classDef;
    }

    /**
     * Adds a method to return the message value. The method name should be the
     * method name annotated {@code org.jboss.logging.Message}. This method will
     * be appended with {@code $str}.
     * <p/>
     * <p>
     * If the message method has already been defined the previously created
     * method is returned.
     * </p>
     * <p/>
     *
     * @param messageMethod the message method
     *
     * @return the newly created method.
     *
     * @throws IllegalStateException if this method is called before the generateModel method
     */
    JMethodDef addMessageMethod(final MessageMethod messageMethod) {
        return addMessageMethod(messageMethod, messageMethod.message().value());
    }

    /**
     * Adds a method to return the message value. The method name should be the
     * method name annotated {@code org.jboss.logging.Message}. This method will
     * be appended with {@code $str}.
     * <p/>
     * <p>
     * If the message method has already been defined the previously created
     * method is returned.
     * </p>
     * <p/>
     *
     * @param messageMethod the message method.
     * @param messageValue  the message value.
     *
     * @return the newly created method.
     *
     * @throws IllegalStateException if this method is called before the generateModel method
     */
    JMethodDef addMessageMethod(final MessageMethod messageMethod, final String messageValue) {
        // Values could be null and we shouldn't create message methods for null values.
        if (messageValue == null) {
            return null;
        }

        // Create the method that returns the string message for formatting
        JMethodDef method = messageMethods.get(messageMethod.messageMethodName());
        if (method == null) {
            method = classDef.method(JMod.PROTECTED, String.class, messageMethod.messageMethodName());
            final JBlock body = method.body();
            final String msg;
            if (messageInterface.projectCode() != null && !messageInterface.projectCode().isEmpty() && messageMethod.message().hasId()) {
                // Prefix the id to the string message
                msg = String.format(format, messageInterface.projectCode(), messageMethod.message().id(), messageValue);
            } else {
                msg = messageValue;
            }
            body._return(JExprs.str(msg));
            messageMethods.put(messageMethod.messageMethodName(), method);
        }

        return method;
    }

    /**
     * Get the class name.
     *
     * @return the class name
     */
    public final String qualifiedClassName() {
        return className;
    }


    /**
     * Creates the read resolve method and instance field.
     *
     * @return the read resolve method.
     */
    protected JMethodDef createReadResolveMethod() {
        final JType type = typeOf(classDef);
        final JVarDeclaration instance = classDef.field(JMod.PUBLIC | JMod.STATIC | JMod.FINAL, type, INSTANCE_FIELD_NAME, type._new());
        final JMethodDef readResolveMethod = classDef.method(JMod.PROTECTED, Object.class, GET_INSTANCE_METHOD_NAME);
        readResolveMethod.body()._return($v(instance));
        return readResolveMethod;
    }

    /**
     * Creates the method used to get the locale for formatting messages.
     * <p>
     * If the {@code locale} parameter is {@code null} the {@link MessageLogger#rootLocale()} or
     * {@link MessageBundle#rootLocale()} will be used to determine the {@linkplain Locale locale} to use.
     * </p>
     *
     * @param locale   the locale to use
     * @param override {@code true} if the {@link Override} annotation should be added to the method
     *
     * @return the call to the locale getter
     */
    JCall createLocaleGetter(final String locale, final boolean override) {
        final String methodName = "getLoggingLocale";
        // Create the type and import it
        final JType localeType = typeOf(Locale.class);
        sourceFile._import(localeType);
        final JVarDeclaration defaultInstance = classDef.field(JMod.PRIVATE | JMod.STATIC | JMod.FINAL, localeType, "LOCALE", determineLocale(locale, localeType));

        // Create the method
        final JMethodDef method = classDef.method(JMod.PROTECTED, localeType, methodName);
        if (override) {
            method.annotate(Override.class);
        }
        method.body()._return($v(defaultInstance));
        return JExprs.call(methodName);
    }

    private JExpr determineLocale(final String locale, final JType localeType) {
        final JExpr initializer;

        if (locale == null) {
            // Create a static instance field for the default locale
            final String bcp47Value;
            if (messageInterface.isAnnotatedWith(MessageBundle.class)) {
                bcp47Value = messageInterface.getAnnotation(MessageBundle.class).rootLocale();
            } else if (messageInterface.isAnnotatedWith(MessageLogger.class)) {
                bcp47Value = messageInterface.getAnnotation(MessageLogger.class).rootLocale();
            } else {
                bcp47Value = "";
            }

            if (bcp47Value.isEmpty()) {
                initializer = localeType.$v("ROOT");
            } else {
                initializer = localeType.call("forLanguageTag").arg(JExprs.str(bcp47Value));
            }
        } else {
            if ("en_CA".equals(locale)) {
                initializer = localeType.$v("CANADA");
            } else if ("fr_CA".equals(locale)) {
                initializer = localeType.$v("CANADA_FRENCH");
            } else if ("zh".equals(locale)) {
                initializer = localeType.$v("CHINESE");
            } else if ("en".equals(locale)) {
                initializer = localeType.$v("ENGLISH");
            } else if ("fr_FR".equals(locale)) {
                initializer = localeType.$v("FRANCE");
            } else if ("fr".equals(locale)) {
                initializer = localeType.$v("FRENCH");
            } else if ("de".equals(locale)) {
                initializer = localeType.$v("GERMAN");
            } else if ("de_DE".equals(locale)) {
                initializer = localeType.$v("GERMANY");
            } else if ("it".equals(locale)) {
                initializer = localeType.$v("ITALIAN");
            } else if ("it_IT".equals(locale)) {
                initializer = localeType.$v("ITALY");
            } else if ("ja_JP".equals(locale)) {
                initializer = localeType.$v("JAPAN");
            } else if ("ja".equals(locale)) {
                initializer = localeType.$v("JAPANESE");
            } else if ("ko_KR".equals(locale)) {
                initializer = localeType.$v("KOREA");
            } else if ("ko".equals(locale)) {
                initializer = localeType.$v("KOREAN");
            } else if ("zh_CN".equals(locale)) {
                initializer = localeType.$v("SIMPLIFIED_CHINESE");
            } else if ("zh_TW".equals(locale)) {
                initializer = localeType.$v("TRADITIONAL_CHINESE");
            } else if ("en_UK".equals(locale)) {
                initializer = localeType.$v("UK");
            } else if ("en_US".equals(locale)) {
                initializer = localeType.$v("US");
            } else {
                final JCall newInstance = localeType._new();
                // Split the locale
                final String[] parts = locale.split("_");
                if (parts.length > 3) {
                    throw new ProcessingException(messageInterface, "Failed to parse %s to a Locale.", locale);
                }
                for (String arg : parts) {
                    newInstance.arg(JExprs.str(arg));
                }
                initializer = newInstance;
            }
        }

        return initializer;
    }
}
