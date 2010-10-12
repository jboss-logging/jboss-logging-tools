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
package org.jboss.logging;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.lang.model.element.ExecutableElement;
import javax.tools.JavaFileObject;

import org.jboss.logging.util.TransformationUtil;

import com.sun.codemodel.internal.CodeWriter;
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
import com.sun.codemodel.internal.JVar;

/**
 * An abstract code model to create the source file that implements the
 * interface.
 * 
 * <p>
 * Essentially this uses a com.sun.codemodel.internal.JCodeModel to generate the
 * source files with. This class is for convenience in generating default source
 * files.
 * </p>
 * 
 * @author James R. Perkins Jr. (jrp)
 * 
 */
public abstract class CodeModel {

    public static final JType[] EMPTY_TYPE_ARRAY = new JTypeVar[0];

    /**
     * The implementation types.
     * 
     * @author James R. Perkins Jr. (jrp)
     * 
     */
    public static enum Implementation {
        /**
         * Represents the {@code org.jboss.logging.MessageBundle}.
         */
        BUNDLE("$bundle"),
        /**
         * Represents the {@code org.jboss.logging.MessageLogger}.
         */
        LOGGER("$logger");
        /**
         * The extension to append the implementation with.
         */
        protected final String extension;

        /**
         * Enum constructor.
         * 
         * @param extension
         *            the extension to append the implementation with.
         */
        private Implementation(final String extension) {
            this.extension = extension;
        }
    }

    private final JCodeModel codeModel;
    private JDefinedClass definedClass;
    private final String interfaceName;
    private final String packageName;
    private final String projectCode;

    /**
     * Class constructor.
     * 
     * @param interfaceName
     *            the interface name to implement.
     * @param projectCode
     *            the project code to prepend messages with.
     * @throws JClassAlreadyExistsException
     *             When the specified class/interface was already created.
     */
    protected CodeModel(final String interfaceName, final String projectCode)
            throws JClassAlreadyExistsException {
        codeModel = new JCodeModel();
        this.interfaceName = interfaceName;
        this.packageName = TransformationUtil.toPackage(interfaceName());
        this.projectCode = projectCode;
        init();
    }

    /**
     * Returns the code model.
     * 
     * @return the code model.
     */
    public final JCodeModel codeModel() {
        return codeModel;
    }

    /**
     * Returns the main enclosing class.
     * 
     * @return the main enclosing class.
     */
    public final JDefinedClass definedClass() {
        return definedClass;
    }

    /**
     * Returns the implementation type.
     * 
     * @return the implementation type.
     */
    public abstract Implementation type();

    /**
     * Adds a method to the class.
     * 
     * @param method
     *            the method to add.
     */
    public abstract void addMethod(final ExecutableElement method);

    /**
     * The interface name this generated class will be implementing.
     * 
     * @return the interface name.
     */
    public final String interfaceName() {
        return interfaceName;
    }

    /**
     * Returns the fully qualified class name of the class.
     * 
     * @return the fully qualified class name.
     */
    public final String className() {
        return interfaceName() + type().extension;
    }

    /**
     * Returns the package name for the class.
     * 
     * @return the package name.
     */
    public final String packageName() {
        return packageName;
    }

    /**
     * This method is invoked before the class is written. There is no need to
     * explicitly execute this method. Doing so could result in errors.
     */
    protected abstract void beforeWrite();

    /**
     * Writes the class created to a generated class file.
     * 
     * <p>
     * Invokes the {@code CodeModel#beforeWrite()} method before the class is
     * written.
     * </p>
     * 
     * @param fileObject
     *            the file object where to write the source to.
     * @throws IOException
     *             if a write error occurs.
     * @throws ValidationException
     *             if invalid.
     */
    public final void writeClass(final JavaFileObject fileObject)
            throws IOException {
        beforeWrite();
        final CodeWriter codeWriter = new JavaFileObjectCodeWriter(fileObject);
        codeModel().build(codeWriter);
    }

    /**
     * Creates the variable that stores the message.
     * 
     * <p>
     * If the message variable has already been defined the previously created
     * variable is returned.
     * </p>
     * 
     * @param varName
     *            the variable name.
     * @param messageValue
     *            the value for the message.
     * @param id
     *            the id to prepend the project code/message with.
     * @return the newly created variable.
     */
    protected final JVar addMessageVar(final String varName,
            final String messageValue, int id) {
        JFieldVar var = definedClass().fields().get(varName);
        if (var == null) {
            var = definedClass.field(JMod.PRIVATE | JMod.STATIC | JMod.FINAL,
                    String.class, varName);
            String value = messageValue;
            if (id > 0) {
                value = formatMessageId(id) + messageValue;
            }
            var.init(JExpr.lit(value));
        }
        return var;
    }

    /**
     * Adds a method to return the message value. The method name should be the
     * method name annotated {@code org.jboss.logging.Message}. This method will
     * be appended with {@code $str}.
     * 
     * <p>
     * If the message method has already been defined the previously created
     * method is returned.
     * </p>
     * 
     * <p>
     * Note this method invokes the
     * {@code addMessageVar(varName, messageValue, id)} to add the variable.
     * </p>
     * 
     * 
     * @param methodName
     *            the method name.
     * @param returnValue
     *            the message value.
     * @param id
     *            the id to prepend the project code/message with.
     * @return the newly created method.
     */
    protected final JMethod addMessageMethod(final String methodName,
            final String returnValue, final int id) {
        final String internalMethodName = methodName + "$str";
        JMethod method = definedClass().getMethod(internalMethodName,
                EMPTY_TYPE_ARRAY);
        // Create the method
        if (method == null) {
            final JClass returnType = codeModel().ref(String.class);
            method = definedClass().method(JMod.PROTECTED, returnType,
                    internalMethodName);
            final JBlock body = method.body();
            body._return(addMessageVar(methodName, returnValue, id));
        }
        return method;
    }

    /**
     * Formats the message id. The message id is comprised of the project code
     * plus the id.
     * 
     * @param id
     *            the id used to prepend the project code.
     * @return the formatted message id.
     */
    protected final String formatMessageId(final int id) {
        final StringBuilder result = new StringBuilder(projectCode);
        if (result.length() > 0) {
            result.append("-");
            result.append(padLeft("" + id, '0', 5));
            result.append(": ");
        }
        return result.toString();
    }

    /**
     * Pads the initial value with the character. If the length is greater than
     * or equal to the length of the initial value, the initial value will be
     * returned.
     * 
     * @param initValue
     *            the value to pad.
     * @param padChar
     *            the character to pad the value with.
     * @param padLen
     *            the total length the string should be.
     * @return the padded value.
     */
    protected final String padLeft(final String initValue, final char padChar,
            final int padLen) {
        final StringBuilder result = new StringBuilder();
        for (int i = initValue.length(); i < padLen; i++) {
            result.append(padChar);
        }
        result.append(initValue);
        return result.toString();
    }

    /**
     * Initializes the class to generate with defaults.
     * 
     * @throws JClassAlreadyExistsException
     *             When the specified class/interface was already created.
     */
    private void init() throws JClassAlreadyExistsException {
        // Define the class
        definedClass = codeModel._class(className());
        final JAnnotationUse anno = definedClass
                .annotate(javax.annotation.Generated.class);
        anno.param("value", getClass().getCanonicalName());
        anno.param("date", generatedDateValue());
        definedClass._extends(Object.class);
        definedClass._implements(codeModel.directClass(interfaceName()));
        definedClass._implements(java.io.Serializable.class);

        // Create the default JavaDoc
        final JDocComment docComment = definedClass.javadoc();
        docComment.add("Warning this class consists of generated code.");

        // Add the serializable UID
        final JFieldVar serialVersionUID = definedClass.field(JMod.PRIVATE
                | JMod.STATIC | JMod.FINAL, codeModel.LONG, "serialVersionUID");
        serialVersionUID.init(JExpr.lit(1L));
    }

    /**
     * Returns the current date formatted in the ISO 8601 format.
     * 
     * @return the current date formatted in ISO 8601.
     */
    protected static String generatedDateValue() {
        final SimpleDateFormat sdf = new SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ssZ");
        return sdf.format(new Date());
    }

}
