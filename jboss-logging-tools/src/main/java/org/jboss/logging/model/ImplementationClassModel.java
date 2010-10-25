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
package org.jboss.logging.model;

import com.sun.codemodel.internal.JCodeModel;
import com.sun.codemodel.internal.JExpr;
import com.sun.codemodel.internal.JFieldVar;
import com.sun.codemodel.internal.JMod;
import com.sun.codemodel.internal.JVar;
import org.jboss.logging.Message;
import org.jboss.logging.validation.MessageAnnotationValidator;
import org.jboss.logging.validation.MessageIdValidator;
import org.jboss.logging.validation.MethodParameterValidator;

import javax.lang.model.element.ExecutableElement;
import java.io.Serializable;

/**
 * An abstract code model to create the source file that implements the
 * interface.
 * 
 * <p>
 * Essentially this uses the com.sun.codemodel.internal.JCodeModel to generate the
 * source files with. This class is for convenience in generating default source
 * files.
 * </p>
 * 
 * @author James R. Perkins Jr. (jrp)
 * 
 */
public abstract class ImplementationClassModel extends ClassModel {

    private final String interfaceName;

    private final ImplementationType type;

    protected MethodDescriptor methodDescriptor;

    private final MessageAnnotationValidator messageAnnotationValidator;

    /**
     * Class constructor.
     * 
     * @param interfaceName
     *            the interface name to implement.
     * @param projectCode
     *            the project code to prepend messages with.
     * @param type
     *            the type of the implementation.
     */
    protected ImplementationClassModel(final String interfaceName,
            final String projectCode, ImplementationType type) {
        super(interfaceName + type.extension(), projectCode, Object.class.
                getName(), interfaceName, Serializable.class.getName());
        this.interfaceName = interfaceName;
        this.type = type;
        methodDescriptor = new MethodDescriptor();
        messageAnnotationValidator = new MessageAnnotationValidator();
    }

    /**
     * Returns the implementation type.
     * 
     * @return the implementation type.
     */
    public final ImplementationType type() {
        return type;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final String getClassName() {
        return interfaceName + type().extension();
    }

    /**
     * Adds a method to the class.
     * 
     * @param method
     *            the method to add.
     */
    public void addMethod(final ExecutableElement method) {
        methodDescriptor = methodDescriptor.add(method);
        messageAnnotationValidator.addMethod(method);
        addValidator(new MethodParameterValidator(methodDescriptor));
    }

    /**
     * Adds and id variable to the generated class if the id is greater than 0.
     *
     * <p>
     * The variable name will be the method name with &quot;Id&quot; as the
     * suffix.
     * </p>
     *
     * @param methodName the method name to prefix the id with.
     * @param id         the id of the message.
     *
     * @return the variable that was created or {@code null} if no variable was
     *         created.
     */
    protected JVar addIdVar(final String methodName, final int id) {
        final String idFieldName = methodName + "Id";
        JVar idVar = definedClass().fields().get(idFieldName);
        if (idVar == null && id > Message.NONE) {
            // Create the message id field
            idVar = definedClass().field(
                    JMod.PROTECTED | JMod.STATIC | JMod.FINAL,
                    String.class, idFieldName);
            idVar.init(JExpr.lit(ClassModelUtil.formatMessageId(
                    projectCode(), id)));
        }
        return idVar;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void preValidation() {
        super.preValidation();
        addValidator(messageAnnotationValidator);
        addValidator(new MessageIdValidator(methodDescriptor));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected JCodeModel generateModel() throws IllegalStateException {
        final JCodeModel codeModel = super.generateModel();
        // Add the serializable UID
        final JFieldVar serialVersionUID = definedClass().field(
                JMod.PRIVATE | JMod.STATIC | JMod.FINAL, codeModel.LONG,
                "serialVersionUID");
        serialVersionUID.init(JExpr.lit(1L));
        return codeModel;
    }
}
