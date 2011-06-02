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
import org.jboss.logging.generator.MethodDescriptor;

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

    /**
     * Qualified interface name.
     */
    private String[] interfaceNames;

    /**
     * Qualified super class name.
     */
    private String superClassName;

    /**
     * Qualified class name.
     */
    private String className;

    /**
     * The corresponding model.
     */
    private JCodeModel codeModel;

    /**
     * The defined class.
     */
    private JDefinedClass definedClass;

    /**
     * The project code from the annotation.
     */
    private String projectCode;

    /**
     * Construct a class model.
     *
     * @param className      the qualified class name.
     * @param superClassName the qualified super class name.
     */
    public ClassModel(final String className, final String superClassName) {
        this(className, null, superClassName);
    }

    /**
     * Construct a class model.
     *
     * @param className      the qualified class name.
     * @param projectCode    the project code.
     * @param superClassName the super class name.
     * @param interfaceNames an array of interfaces to implement.
     */
    protected ClassModel(final String className, final String projectCode, final String superClassName, final String... interfaceNames) {
        this.interfaceNames = interfaceNames;
        this.superClassName = superClassName;
        this.className = className;
        this.projectCode = projectCode;
        initModel();
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
     * @throws IllegalStateException if the class has already been defined.
     */
    protected JCodeModel generateModel() throws IllegalStateException {
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
     * @param methodDescriptor the method descriptor.
     *
     * @return the newly created method.
     * @throws IllegalStateException if this method is called before the generateModel method
     */
    protected JMethod addMessageMethod(final MethodDescriptor methodDescriptor) {
        return addMessageMethod(methodDescriptor, methodDescriptor.messageValue());
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
     * @param methodDescriptor the method descriptor.
     * @param messageValue     the message value.
     *
     * @return the newly created method.
     * @throws IllegalStateException if this method is called before the generateModel method
     */
    protected JMethod addMessageMethod(final MethodDescriptor methodDescriptor, final String messageValue) {
        if (codeModel == null || definedClass == null) {
            throw new IllegalStateException("The code model or the corresponding defined class is null");
        }
        // Values could be null and we shouldn't create message methods for null values.
        if (messageValue == null) {
            return null;
        }
        final String methodName;
        if (methodDescriptor.isOverloaded()) {
            methodName = methodDescriptor.name() + methodDescriptor.relativeParameterCount();
        } else {
            methodName = methodDescriptor.name();
        }
        JMethod method = definedClass.getMethod(methodDescriptor.messageMethodName(), EMPTY_TYPE_ARRAY);

        if (method == null) {

            //Create method return field
            JFieldVar methodField = definedClass.fields().get(methodName);
            if (methodField == null) {
                methodField = definedClass.field(JMod.PRIVATE | JMod.STATIC | JMod.FINAL, String.class, methodName);
                methodField.init(JExpr.lit(messageValue));
            }

            //Create method
            JClass returnType = codeModel.ref(String.class);
            method = definedClass.method(JMod.PROTECTED, returnType, methodDescriptor.messageMethodName());

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
    public final JDefinedClass getDefinedClass() {
        return definedClass;
    }

    /**
     * Get the class name.
     *
     * @return the class name
     */
    public final String getClassName() {
        return this.className;
    }

    /**
     * Get the project code from the annotation.
     *
     * @return the project code or {@code null} if one was not specified.
     */
    public String getProjectCode() {
        return projectCode;
    }

    /**
     * Initialize the model.
     */
    private void initModel() {
        codeModel = new JCodeModel();

        try {
            definedClass = codeModel._class(className);
        } catch (JClassAlreadyExistsException e) {
            throw new IllegalStateException("Class " + className + " has already been defined. Cannot generate the class.", e);
        }

        //Add generated annotation
        JAnnotationUse generatedAnnotation = definedClass.annotate(javax.annotation.Generated.class);
        generatedAnnotation.param("value", getClass().getName());
        generatedAnnotation.param("date", ClassModelUtil.generatedDateValue());

        //Create the default JavaDoc
        JDocComment docComment = definedClass.javadoc();
        docComment.add("Warning this class consists of generated code.");

        //Add extends
        if (superClassName != null) {
            definedClass._extends(codeModel.ref(superClassName));
        }

        //Add implements
        if (interfaceNames != null) {
            for (String intf : interfaceNames) {
                // TODO - Temporary fix for implementing nested interfaces.
                definedClass._implements(codeModel.ref(intf.replace("$", ".")));
            }
        }
    }

}
