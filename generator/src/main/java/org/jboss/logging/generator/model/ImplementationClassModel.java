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
import com.sun.codemodel.internal.JMethod;
import com.sun.codemodel.internal.JMod;
import com.sun.codemodel.internal.JVar;
import org.jboss.logging.generator.MessageInterface;
import org.jboss.logging.generator.MessageMethod;
import org.jboss.logging.generator.MethodParameter;
import org.jboss.logging.generator.ThrowableReturnType;

import java.io.Serializable;

import static org.jboss.logging.generator.model.ClassModelUtil.formatMessageId;

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
abstract class ImplementationClassModel extends ClassModel {

    /**
     * Class constructor.
     *
     * @param messageInterface the message interface to implement.
     * @param type             the type of the implementation.
     */
    protected ImplementationClassModel(final MessageInterface messageInterface, final ImplementationType type) {
        super(messageInterface, messageInterface.qualifiedName() + type, null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected JCodeModel generateModel() throws IllegalStateException {
        JCodeModel codeModel = super.generateModel();
        getDefinedClass()._implements(codeModel.ref(Serializable.class));
        // Add the serializable UID
        JFieldVar serialVersionUID = getDefinedClass().field(JMod.PRIVATE | JMod.STATIC | JMod.FINAL, codeModel.LONG, "serialVersionUID");
        serialVersionUID.init(JExpr.lit(1L));

        return codeModel;
    }

    /**
     * Create the bundle method body.
     *
     * @param messageMethod  the message method.
     * @param method         the method to create the body for.
     * @param msgMethod      the message method for retrieving the message.
     * @param projectCodeVar the project code variable
     */
    protected void createBundleMethod(final MessageMethod messageMethod, final JMethod method, final JMethod msgMethod, final JVar projectCodeVar) {
        // Create the body of the method and add the text
        final JBlock body = method.body();
        final JClass returnField = getCodeModel().ref(method.type().fullName());
        final JVar result = body.decl(returnField, "result");
        final JClass formatter = getCodeModel().ref(messageMethod.messageFormat().formatClass());
        final JInvocation formatterMethod = formatter.staticInvoke(messageMethod.messageFormat().staticMethod());
        if (messageMethod.allParameters().isEmpty()) {
            // If the return type is an exception, initialize the exception.
            if (messageMethod.returnType().isThrowable()) {
                if (messageMethod.hasMessageId() && projectCodeVar != null) {
                    String formattedId = formatMessageId(messageMethod.messageId());
                    formatterMethod.arg(projectCodeVar.plus(JExpr.lit(formattedId)).plus(JExpr.invoke(msgMethod)));
                    initCause(result, returnField, body, messageMethod, formatterMethod);
                } else {
                    initCause(result, returnField, body, messageMethod, JExpr.invoke(msgMethod));
                }
            } else {
                result.init(JExpr.invoke(msgMethod));
            }
        } else {
            if (messageMethod.hasMessageId() && projectCodeVar != null) {
                String formattedId = formatMessageId(messageMethod.messageId());
                formatterMethod.arg(projectCodeVar.plus(JExpr.lit(formattedId)).plus(JExpr.invoke(msgMethod)));
            } else {
                formatterMethod.arg(JExpr.invoke(msgMethod));
            }
            // Create the parameters
            for (MethodParameter param : messageMethod.allParameters()) {
                final JClass paramType = getCodeModel().ref(param.type());
                JVar paramVar = method.param(JMod.FINAL, paramType, param.name());
            }
            // Create the parameters
            for (MethodParameter param : messageMethod.formatParameters()) {
                final String formatterClass = param.getFormatterClass();
                if (formatterClass == null) {
                    formatterMethod.arg(JExpr.direct(param.name()));
                } else {
                    formatterMethod.arg(JExpr._new(JClass.parse(getCodeModel(), formatterClass)).arg(JExpr.direct(param.name())));
                }
            }
            // Setup the return type
            if (messageMethod.returnType().isThrowable()) {
                initCause(result, returnField, body, messageMethod, formatterMethod);
            } else {
                result.init(formatterMethod);
            }
        }
        body._return(result);
    }

    /**
     * Initialize the cause (Throwable) return type.
     *
     * @param result          the return variable
     * @param returnField     the return field
     * @param body            the body of the method
     * @param messageMethod   the message method
     * @param formatterMethod the formatter method used to format the string cause
     */
    protected void initCause(final JVar result, final JClass returnField, final JBlock body, final MessageMethod messageMethod, final JInvocation formatterMethod) {
        final ThrowableReturnType returnType = messageMethod.returnType().throwableReturnType();
        if (returnType.useConstructionParameters()) {
            final JInvocation invocation = JExpr._new(returnField);
            for (MethodParameter param : returnType.constructionParameters()) {
                if (param.isMessage()) {
                    invocation.arg(formatterMethod);
                } else {
                    invocation.arg(JExpr.ref(param.name()));
                }
            }
            result.init(invocation);
        } else if (returnType.hasStringAndThrowableConstructor() && messageMethod.hasCause()) {
            result.init(JExpr._new(returnField).arg(formatterMethod).arg(JExpr.ref(messageMethod.cause().name())));
        } else if (returnType.hasThrowableAndStringConstructor() && messageMethod.hasCause()) {
            result.init(JExpr._new(returnField).arg(JExpr.ref(messageMethod.cause().name())).arg(formatterMethod));
        } else if (returnType.hasStringConstructor()) {
            result.init(JExpr._new(returnField).arg(formatterMethod));
            if (messageMethod.hasCause()) {
                JInvocation resultInv = body.invoke(result, "initCause");
                resultInv.arg(JExpr.ref(messageMethod.cause().name()));
            }
        } else if (returnType.hasThrowableConstructor() && messageMethod.hasCause()) {
            result.init(JExpr._new(returnField).arg(JExpr.ref(messageMethod.cause().name())));
        } else if (returnType.hasStringAndThrowableConstructor() && !messageMethod.hasCause()) {
            result.init(JExpr._new(returnField).arg(formatterMethod).arg(JExpr._null()));
        } else if (returnType.hasThrowableAndStringConstructor() && !messageMethod.hasCause()) {
            result.init(JExpr._new(returnField).arg(JExpr._null()).arg(formatterMethod));
        } else if (messageMethod.hasCause()) {
            result.init(JExpr._new(returnField));
            JInvocation resultInv = body.invoke(result, "initCause");
            resultInv.arg(JExpr.ref(messageMethod.cause().name()));
        } else {
            result.init(JExpr._new(returnField));
        }
    }
}
