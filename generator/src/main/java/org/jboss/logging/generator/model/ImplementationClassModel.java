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

import com.sun.codemodel.internal.JBlock;
import com.sun.codemodel.internal.JClass;
import com.sun.codemodel.internal.JCodeModel;
import com.sun.codemodel.internal.JExpr;
import com.sun.codemodel.internal.JFieldVar;
import com.sun.codemodel.internal.JInvocation;
import com.sun.codemodel.internal.JMod;
import com.sun.codemodel.internal.JVar;
import org.jboss.logging.generator.MethodDescriptor;
import org.jboss.logging.generator.MethodDescriptors;
import org.jboss.logging.generator.ReturnType;

import java.io.Serializable;

/**
 * An abstract code model to create the source file that implements the
 * interface.
 * <p/>
 * <p>
 * Essentially this uses the com.sun.codemodel.internal.JCodeModel to generate the
 * source files with. This class is for convenience in generating default source
 * files.
 * </p>
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public abstract class ImplementationClassModel extends ClassModel {

    private final ImplementationType type;

    private final MethodDescriptors methodDescriptors;

    /**
     * Class constructor.
     *
     * @param interfaceName the interface name to implement.
     * @param methodDescriptors the method descriptions
     * @param projectCode   the project code to prepend messages with.
     * @param type          the type of the implementation.
     */
    protected ImplementationClassModel(final String interfaceName, final MethodDescriptors methodDescriptors, final String projectCode, final ImplementationType type) {
        super(interfaceName + type, projectCode, Object.class.getName(), interfaceName, Serializable.class.getName());
        this.type = type;
        this.methodDescriptors = methodDescriptors;
    }

    /**
     * Returns the implementation type.
     *
     * @return the implementation type.
     */
    public final ImplementationType getType() {
        return type;
    }

    /**
     * Returns the method descriptions.
     *
     * @return the method descriptions.
     */
    public final MethodDescriptors getMethodDescriptors() {
        return methodDescriptors;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected JCodeModel generateModel() throws IllegalStateException {
        JCodeModel codeModel = super.generateModel();

        // Add the serializable UID
        JFieldVar serialVersionUID = getDefinedClass().field(JMod.PRIVATE | JMod.STATIC | JMod.FINAL, codeModel.LONG, "serialVersionUID");
        serialVersionUID.init(JExpr.lit(1L));

        return codeModel;
    }

    /**
     * Initialize the cause (Throwable) return type.
     *
     * @param result          the return variable
     * @param returnField     the return field
     * @param body            the body of the method
     * @param methodDesc      the method description
     * @param formatterMethod the formatter method used to format the string cause
     */
    protected void initCause(final JVar result, final JClass returnField, final JBlock body, final MethodDescriptor methodDesc, final JInvocation formatterMethod) {
        ReturnType desc = methodDesc.returnType();
        if (desc.hasStringAndThrowableConstructor() && methodDesc.hasCause()) {
            result.init(JExpr._new(returnField).arg(formatterMethod).arg(JExpr.ref(methodDesc.cause().name())));
        } else if (desc.hasThrowableAndStringConstructor() && methodDesc.hasCause()) {
            result.init(JExpr._new(returnField).arg(JExpr.ref(methodDesc.cause().name())).arg(formatterMethod));
        } else if (desc.hasStringConstructor()) {
            result.init(JExpr._new(returnField).arg(formatterMethod));
            if (methodDesc.hasCause()) {
                JInvocation resultInv = body.invoke(result, "initCause");
                resultInv.arg(JExpr.ref(methodDesc.cause().name()));
            }
        } else if (desc.hasThrowableConstructor() && methodDesc.hasCause()) {
            result.init(JExpr._new(returnField).arg(JExpr.ref(methodDesc.cause().name())));
        } else if (desc.hasStringAndThrowableConstructor() && !methodDesc.hasCause()) {
            result.init(JExpr._new(returnField).arg(formatterMethod).arg(JExpr._null()));
        } else if (desc.hasThrowableAndStringConstructor() && !methodDesc.hasCause()) {
            result.init(JExpr._new(returnField).arg(JExpr._null()).arg(formatterMethod));
        } else if (methodDesc.hasCause()) {
            result.init(JExpr._new(returnField));
            JInvocation resultInv = body.invoke(result, "initCause");
            resultInv.arg(JExpr.ref(methodDesc.cause().name()));
        } else {
            result.init(JExpr._new(returnField));
        }
    }
}
