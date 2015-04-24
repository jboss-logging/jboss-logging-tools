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

package org.jboss.logging.processor.generator.model;

import static org.jboss.jdeparser.JExprs.$v;
import static org.jboss.jdeparser.JMod.FINAL;
import static org.jboss.jdeparser.JTypes.$t;
import static org.jboss.logging.processor.util.ElementHelper.typeToString;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import javax.annotation.Generated;
import javax.annotation.processing.Filer;

import org.jboss.jdeparser.FormatPreferences;
import org.jboss.jdeparser.JClassDef;
import org.jboss.jdeparser.JDeparser;
import org.jboss.jdeparser.JExprs;
import org.jboss.jdeparser.JFiler;
import org.jboss.jdeparser.JMethodDef;
import org.jboss.jdeparser.JMod;
import org.jboss.jdeparser.JSourceFile;
import org.jboss.jdeparser.JSources;
import org.jboss.jdeparser.JType;
import org.jboss.jdeparser.JTypes;
import org.jboss.jdeparser.JVarDeclaration;
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
    protected final JSourceFile sourceFile;

    private final MessageInterface messageInterface;

    private final String className;
    private final String superClassName;

    private final String format;

    private final Map<String, JMethodDef> messageMethods;
    private final Map<String, JVarDeclaration> messageFields;

    /**
     * Construct a class model.
     *
     * @param filer            the filer used to create the source file
     * @param messageInterface the message interface to implement.
     * @param superClassName   the super class used for the translation implementations.
     */
    ClassModel(final Filer filer, final MessageInterface messageInterface, final String className, final String superClassName) {
        this.messageInterface = messageInterface;
        this.className = messageInterface.packageName() + "." + className;
        this.superClassName = superClassName;
        sources = JDeparser.createSources(JFiler.newInstance(filer), new FormatPreferences(new Properties()));
        sourceFile = sources.createSourceFile(messageInterface.packageName(), className);
        classDef = sourceFile._class(JMod.PUBLIC, className);
        final int idLen = messageInterface.getIdLength();
        if (idLen > 0) {
            format = "%s%0" + messageInterface.getIdLength() + "d: %s";
        } else {
            format = "%s%d: %s";
        }
        messageMethods = new HashMap<>();
        messageFields = new HashMap<>();
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
        // Add generated annotation
        final JType generatedType = $t(Generated.class);
        sourceFile._import(generatedType);
        classDef.annotate(generatedType)
                .value("value", getClass().getName())
                .value("date", JExprs.str(ClassModelHelper.generatedDateValue()));

        // Suppress all warnings: all for Eclipse, rawtypes & unchecked for javac
        JAnnotationUse suppressWarningsAnnotation = definedClass.annotate(SuppressWarnings.class);
        suppressWarningsAnnotation.paramArray("value").param("all").param("rawtypes").param("unchecked");

        // Create the default JavaDoc
        classDef.docComment().text("Warning this class consists of generated code.");

        // Add extends
        if (superClassName != null) {
            classDef._extends(superClassName);
        }

        // Always implement the interface
        // TODO - Temporary fix for implementing nested interfaces.
        classDef._implements(typeToString(messageInterface.name()));

        //Add implements
        if (!messageInterface.extendedInterfaces().isEmpty()) {
            for (MessageInterface intf : messageInterface.extendedInterfaces()) {
                // TODO - Temporary fix for implementing nested interfaces.
                final JType interfaceName = $t(typeToString(intf.name()));
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
        final String methodName;
        if (messageMethod.isOverloaded()) {
            methodName = messageMethod.name() + messageMethod.formatParameterCount();
        } else {
            methodName = messageMethod.name();
        }

        // Create the method that returns the string message for formatting
        JMethodDef method = messageMethods.get(messageMethod.messageMethodName());
        if (method == null) {
            JVarDeclaration field = messageFields.get(methodName);
            if (field == null) {
                final String msg;
                if (messageInterface.projectCode() != null && !messageInterface.projectCode().isEmpty() && messageMethod.message().hasId()) {
                    // Prefix the id to the string message
                    msg = String.format(format, messageInterface.projectCode(), messageMethod.message().id(), messageValue);
                } else {
                    msg = messageValue;
                }
                field = classDef.field(JMod.PRIVATE | JMod.STATIC | JMod.FINAL, String.class, methodName, JExprs.str(msg));
                messageFields.put(field.name(), field);
            }
            method = classDef.method(JMod.PROTECTED, String.class, messageMethod.messageMethodName());
            method.body()._return($v(field));
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
        final JType type = JTypes.typeOf(classDef);
        final JVarDeclaration instance = classDef.field(JMod.PUBLIC | JMod.STATIC | JMod.FINAL, type, INSTANCE_FIELD_NAME, type._new());
        final JMethodDef readResolveMethod = classDef.method(JMod.PROTECTED, Object.class, GET_INSTANCE_METHOD_NAME);
        readResolveMethod.body()._return($v(instance));
        return readResolveMethod;
    }
}
