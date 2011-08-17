/*
 * JBoss, Home of Professional Open Source Copyright 2010, Red Hat, Inc., and
 * individual contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this software; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 * site: http://www.fsf.org.
 */
package org.jboss.logging.generator.model;

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
import org.jboss.logging.generator.MessageInterface;
import org.jboss.logging.generator.MessageMethod;

import javax.tools.JavaFileObject;
import java.io.IOException;

/**
 * The basic java class model.
 *
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public abstract class ClassModel {

    private static final JType[] EMPTY_TYPE_ARRAY = new JTypeVar[0];

    private JCodeModel codeModel;

    private JDefinedClass definedClass;

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
    protected JCodeModel generateModel() throws IllegalStateException {
        codeModel = new JCodeModel();

        try {
            definedClass = codeModel._class(qualifiedClassName());
        } catch (JClassAlreadyExistsException e) {
            throw new IllegalStateException("Class " + qualifiedClassName() + " has already been defined. Cannot generate the class.", e);
        }

        // Add generated annotation
        JAnnotationUse generatedAnnotation = definedClass.annotate(javax.annotation.Generated.class);
        generatedAnnotation.param("value", getClass().getName());
        generatedAnnotation.param("date", ClassModelUtil.generatedDateValue());

        // Create the default JavaDoc
        JDocComment docComment = definedClass.javadoc();
        docComment.add("Warning this class consists of generated code.");

        // Add extends
        if (superClassName != null) {
            definedClass._extends(codeModel.ref(superClassName));
        }

        // Always implement the interface
        // TODO - Temporary fix for implementing nested interfaces.
        definedClass._implements(codeModel.ref(messageInterface.name().replace("$", ".")));

        //Add implements
        if (!messageInterface.extendedInterfaces().isEmpty()) {
            for (MessageInterface intf : messageInterface.extendedInterfaces()) {
                // TODO - Temporary fix for implementing nested interfaces.
                final String interfaceName = intf.name().replace("$", ".");
                definedClass._implements(codeModel.ref(interfaceName));
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
    protected JMethod addMessageMethod(final MessageMethod messageMethod) {
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
    protected JMethod addMessageMethod(final MessageMethod messageMethod, final String messageValue) {
        if (codeModel == null || definedClass == null) {
            throw new IllegalStateException("The code model or the corresponding defined class is null");
        }
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
            JClass returnType = codeModel.ref(String.class);
            method = definedClass.method(JMod.PROTECTED, returnType, messageMethod.messageMethodName());

            JBlock body = method.body();
            body._return(methodField);
        }

        return method;
    }

    public JCodeModel getCodeModel() {
        return codeModel;
    }

    /**
     * Returns the main enclosing class.
     *
     * @return the main enclosing class.
     */
    protected final JDefinedClass getDefinedClass() {
        return definedClass;
    }

    /**
     * Get the class name.
     *
     * @return the class name
     */
    public final String qualifiedClassName() {
        return className;
    }

}
