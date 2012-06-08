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

package org.jboss.logging.processor.model;

import static org.jboss.logging.processor.util.ElementHelper.typeToString;

import java.io.IOException;
import javax.tools.JavaFileObject;

import com.sun.codemodel.internal.JAnnotationUse;
import com.sun.codemodel.internal.JBlock;
import com.sun.codemodel.internal.JClass;
import com.sun.codemodel.internal.JClassAlreadyExistsException;
import com.sun.codemodel.internal.JCodeModel;
import com.sun.codemodel.internal.JDefinedClass;
import com.sun.codemodel.internal.JDocComment;
import com.sun.codemodel.internal.JExpr;
import com.sun.codemodel.internal.JFieldVar;
import com.sun.codemodel.internal.JMethod;
import com.sun.codemodel.internal.JMod;
import com.sun.codemodel.internal.JType;
import com.sun.codemodel.internal.JTypeVar;
import org.jboss.logging.processor.intf.model.MessageInterface;
import org.jboss.logging.processor.intf.model.MessageMethod;

/**
 * The basic java class model.
 *
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public abstract class ClassModel {

    private static final JType[] EMPTY_TYPE_ARRAY = new JTypeVar[0];

    private static final String INSTANCE_FIELD_NAME = "INSTANCE";

    private static final String GET_INSTANCE_METHOD_NAME = "readResolve";

    private final JCodeModel codeModel;

    private volatile JDefinedClass definedClass;

    private final MessageInterface messageInterface;

    private final String className;

    private final String superClassName;

    /**
     * Construct a class model.
     *
     * @param messageInterface the message interface to implement.
     * @param className        the final class name.
     * @param superClassName   the super class used for the translation implementations.
     */
    ClassModel(final MessageInterface messageInterface, final String className, final String superClassName) {
        this.messageInterface = messageInterface;
        this.className = className;
        this.superClassName = superClassName;
        codeModel = new JCodeModel();
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
     * Creates the source file.
     *
     * @param fileObject the files object to write the source to.
     *
     * @throws java.io.IOException   if the file could not be written.
     * @throws IllegalStateException if the implementation is in an invalid state.
     */
    public final void create(final JavaFileObject fileObject) throws IOException, IllegalStateException {

        //Generate the model class
        final JCodeModel model = this.generateModel();

        //Write it to a file
        model.build(new JavaFileObjectCodeWriter(fileObject));
    }

    /**
     * Generate the code corresponding to this
     * class model
     *
     * @return the generated code
     *
     * @throws IllegalStateException if the class has already been defined.
     */
    JCodeModel generateModel() throws IllegalStateException {
        final JDefinedClass definedClass = getDefinedClass();

        // Add generated annotation
        JAnnotationUse generatedAnnotation = definedClass.annotate(javax.annotation.Generated.class);
        generatedAnnotation.param("value", getClass().getName());
        generatedAnnotation.param("date", ClassModelHelper.generatedDateValue());

        // Create the default JavaDoc
        JDocComment docComment = definedClass.javadoc();
        docComment.add("Warning this class consists of generated code.");

        // Add extends
        if (superClassName != null) {
            definedClass._extends(codeModel.directClass(superClassName));
        }

        // Always implement the interface
        // TODO - Temporary fix for implementing nested interfaces.
        definedClass._implements(codeModel.directClass(typeToString(messageInterface.name())));

        //Add implements
        if (!messageInterface.extendedInterfaces().isEmpty()) {
            for (MessageInterface intf : messageInterface.extendedInterfaces()) {
                // TODO - Temporary fix for implementing nested interfaces.
                final String interfaceName = typeToString(intf.name());
                definedClass._implements(codeModel.directClass(interfaceName));
            }
        }
        return codeModel;
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
    JMethod addMessageMethod(final MessageMethod messageMethod) {
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
    JMethod addMessageMethod(final MessageMethod messageMethod, final String messageValue) {
        final JDefinedClass definedClass = getDefinedClass();
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
        JMethod method = definedClass.getMethod(messageMethod.messageMethodName(), EMPTY_TYPE_ARRAY);

        if (method == null) {

            //Create method return field
            JFieldVar methodField = definedClass.fields().get(methodName);
            if (methodField == null) {
                methodField = definedClass.field(JMod.PRIVATE | JMod.STATIC | JMod.FINAL, String.class, methodName);
                methodField.init(JExpr.lit(messageValue));
            }

            //Create method
            JClass returnType = codeModel.directClass(String.class.getName());
            method = definedClass.method(JMod.PROTECTED, returnType, messageMethod.messageMethodName());

            JBlock body = method.body();
            body._return(methodField);
        }

        return method;
    }

    final JCodeModel getCodeModel() {
        return codeModel;
    }

    /**
     * Returns the main enclosing class.
     *
     * @return the main enclosing class.
     */
    final JDefinedClass getDefinedClass() {
        JDefinedClass result = definedClass;
        if (result == null) {
            synchronized (codeModel) {
                result = definedClass;
                if (result == null) {
                    try {
                        definedClass = codeModel._class(qualifiedClassName());
                        result = definedClass;
                    } catch (JClassAlreadyExistsException e) {
                        throw new IllegalStateException("Class " + qualifiedClassName() + " has already been defined. Cannot generate the class.", e);
                    }

                }

            }
        }
        return result;
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
    protected JMethod createReadResolveMethod() {
        final JDefinedClass definedClass = getDefinedClass();
        final JFieldVar instance = definedClass.field(JMod.PUBLIC | JMod.STATIC | JMod.FINAL, definedClass, INSTANCE_FIELD_NAME);
        instance.init(JExpr._new(definedClass));
        final JMethod readResolveMethod = definedClass.method(JMod.PROTECTED, Object.class, GET_INSTANCE_METHOD_NAME);
        readResolveMethod.body()._return(instance);
        return readResolveMethod;
    }
}
