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

import static org.jboss.logging.processor.intf.model.Parameter.ParameterType;
import static org.jboss.logging.processor.model.ClassModelHelper.formatMessageId;
import static org.jboss.logging.processor.model.ClassModelHelper.implementationClassName;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.sun.codemodel.internal.JBlock;
import com.sun.codemodel.internal.JClass;
import com.sun.codemodel.internal.JCodeModel;
import com.sun.codemodel.internal.JExpr;
import com.sun.codemodel.internal.JExpression;
import com.sun.codemodel.internal.JFieldVar;
import com.sun.codemodel.internal.JInvocation;
import com.sun.codemodel.internal.JMethod;
import com.sun.codemodel.internal.JMod;
import com.sun.codemodel.internal.JVar;
import org.jboss.logging.processor.Annotations;
import org.jboss.logging.processor.intf.model.MessageInterface;
import org.jboss.logging.processor.intf.model.MessageMethod;
import org.jboss.logging.processor.intf.model.Parameter;
import org.jboss.logging.processor.intf.model.ThrowableType;

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
     */
    ImplementationClassModel(final MessageInterface messageInterface) {
        super(messageInterface, implementationClassName(messageInterface), null);
    }

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
    void createBundleMethod(final MessageMethod messageMethod, final JMethod method, final JMethod msgMethod, final JVar projectCodeVar) {
        addThrownTypes(messageMethod, method);
        // Create the body of the method and add the text
        final JBlock body = method.body();
        final JClass returnField = getCodeModel().ref(method.type().fullName());
        final JVar result = body.decl(returnField, "result");
        final MessageMethod.Message message = messageMethod.message();
        final JExpression format;
        final JClass formatter = getCodeModel().ref(message.format().formatClass());
        final JInvocation formatterMethod = formatter.staticInvoke(message.format().staticMethod());
        final boolean noFormatParameters = messageMethod.parameters(ParameterType.FORMAT).isEmpty();
        // Get the format options
        if (message.format() == Annotations.FormatType.MESSAGE_FORMAT) {
            if (message.hasId() && projectCodeVar != null && noFormatParameters) {
                final String formattedId = formatMessageId(message.id());
                format = projectCodeVar.plus(JExpr.lit(formattedId)).plus(JExpr.invoke(msgMethod));
            } else if (message.hasId() && projectCodeVar != null) {
                final String formattedId = formatMessageId(message.id());
                formatterMethod.arg(projectCodeVar.plus(JExpr.lit(formattedId)).plus(JExpr.invoke(msgMethod)));
                format = formatterMethod;
            } else if (noFormatParameters) {
                format = JExpr.invoke(msgMethod);
            } else {
                formatterMethod.arg(JExpr.invoke(msgMethod));
                format = formatterMethod;
            }
        } else {
            if (message.hasId() && projectCodeVar != null) {
                final String formattedId = formatMessageId(message.id());
                formatterMethod.arg(projectCodeVar.plus(JExpr.lit(formattedId)).plus(JExpr.invoke(msgMethod)));
                format = formatterMethod;
            } else {
                formatterMethod.arg(JExpr.invoke(msgMethod));
                format = formatterMethod;
            }
        }
        // Create maps for the fields and properties. Key is the field or setter method, value is the parameter to set
        // the value to.
        final Map<String, JVar> fields = new HashMap<String, JVar>();
        final Map<String, JVar> properties = new HashMap<String, JVar>();
        // Create the parameters
        for (Parameter param : messageMethod.parameters(ParameterType.ANY)) {
            final JClass paramType = getCodeModel().ref(param.type());
            final JVar var = method.param(JMod.FINAL, paramType, param.name());
            final String formatterClass = param.formatterClass();
            switch (param.parameterType()) {
                case FORMAT: {
                    if (formatterClass == null) {
                        formatterMethod.arg(var);
                    } else {
                        formatterMethod.arg(JExpr._new(getCodeModel().ref(formatterClass)).arg(var));
                    }
                    break;
                }
                case FIELD: {
                    fields.put(param.targetName(), var);
                    break;
                }
                case PROPERTY: {
                    properties.put(param.targetName(), var);
                    break;
                }
            }
        }
        // Setup the return type
        if (messageMethod.returnType().isThrowable()) {
            initCause(result, returnField, body, messageMethod, format);
        } else {
            result.init(format);
        }
        // Set the fields and properties of the return type
        for (Map.Entry<String, JVar> entry : fields.entrySet()) {
            body.assign(result.ref(entry.getKey()), entry.getValue());
        }
        for (Map.Entry<String, JVar> entry : properties.entrySet()) {
            body.add(result.invoke(entry.getKey()).arg(entry.getValue()));
        }
        body._return(result);
    }

    /**
     * Initialize the cause (Throwable) return type.
     *
     * @param result        the return variable
     * @param returnField   the return field
     * @param body          the body of the messageMethod
     * @param messageMethod the message messageMethod
     * @param format        the format used to format the string cause
     */
    private void initCause(final JVar result, final JClass returnField, final JBlock body, final MessageMethod messageMethod, final JExpression format) {
        final ThrowableType returnType = messageMethod.returnType().throwableReturnType();
        if (returnType.useConstructionParameters()) {
            final JInvocation invocation = JExpr._new(returnField);
            for (Parameter param : returnType.constructionParameters()) {
                switch (param.parameterType()) {
                    case MESSAGE:
                        invocation.arg(format);
                        break;
                    default:
                        invocation.arg(JExpr.ref(param.name()));
                        break;

                }
            }
            result.init(invocation);
        } else if (returnType.hasStringAndThrowableConstructor() && messageMethod.hasCause()) {
            result.init(JExpr._new(returnField).arg(format).arg(JExpr.ref(messageMethod.cause().name())));
        } else if (returnType.hasThrowableAndStringConstructor() && messageMethod.hasCause()) {
            result.init(JExpr._new(returnField).arg(JExpr.ref(messageMethod.cause().name())).arg(format));
        } else if (returnType.hasStringConstructor()) {
            result.init(JExpr._new(returnField).arg(format));
            if (messageMethod.hasCause()) {
                JInvocation initCause = body.invoke(result, "initCause");
                initCause.arg(JExpr.ref(messageMethod.cause().name()));
            }
        } else if (returnType.hasThrowableConstructor() && messageMethod.hasCause()) {
            result.init(JExpr._new(returnField).arg(JExpr.ref(messageMethod.cause().name())));
        } else if (returnType.hasStringAndThrowableConstructor() && !messageMethod.hasCause()) {
            result.init(JExpr._new(returnField).arg(format).arg(JExpr._null()));
        } else if (returnType.hasThrowableAndStringConstructor() && !messageMethod.hasCause()) {
            result.init(JExpr._new(returnField).arg(JExpr._null()).arg(format));
        } else if (messageMethod.hasCause()) {
            result.init(JExpr._new(returnField));
            JInvocation initCause = body.invoke(result, "initCause");
            initCause.arg(JExpr.ref(messageMethod.cause().name()));
        } else {
            result.init(JExpr._new(returnField));
        }
        final JClass arrays = getCodeModel().ref(Arrays.class);
        final JClass stClass = getCodeModel().ref(StackTraceElement.class).array();
        final JVar st = body.decl(stClass, "st").init(result.invoke("getStackTrace"));
        final JInvocation setStackTrace = result.invoke("setStackTrace");
        setStackTrace.arg(arrays.staticInvoke("copyOfRange").arg(st).arg(JExpr.lit(1)).arg(st.ref("length")));
        body.add(setStackTrace);
    }

    protected final void addThrownTypes(final MessageMethod messageMethod, final JMethod jMethod) {
        for (ThrowableType thrownType : messageMethod.thrownTypes()) {
            jMethod._throws(getCodeModel().ref(thrownType.name()));
        }
    }
}
