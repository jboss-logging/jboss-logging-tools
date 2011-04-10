/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
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

package org.jboss.logging.validation.validator;

import org.jboss.logging.Annotations;
import org.jboss.logging.generator.ReturnType;
import org.jboss.logging.util.ElementHelper;
import org.jboss.logging.validation.ElementValidator;
import org.jboss.logging.validation.ValidationErrorMessage;
import org.jboss.logging.validation.ValidationMessage;
import org.jboss.logging.validation.ValidationWarningMessage;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.jboss.logging.util.ElementHelper.isAnnotatedWith;
import static org.jboss.logging.util.ElementHelper.isAssignableFrom;

/**
 * User: jrp
 * Date: 9/4/11
 * Time: 14:27
 *
 * @author <a href="mailto:jrperkinsjr@gmail.com">James R. Perkins</a>
 */
abstract class AbstractValidator implements ElementValidator {
    protected final Annotations annotations;
    private final Types typeUtil;

    protected AbstractValidator(final Annotations annotations, final Types typeUtil) {
        this.typeUtil = typeUtil;
        this.annotations = annotations;
    }

    /**
     * Validates a message bundle method.
     *
     * @param root   the interface.
     * @param method the method to validate.
     *
     * @return a collection of validation messages or an empty collection.
     */
    public final Collection<ValidationMessage> checkMessageBundleMethod(final TypeElement root, final ExecutableElement method) {
        final Collection<ValidationMessage> messages = new ArrayList<ValidationMessage>();
        if (!hasOrInheritsMessage(root, method)) {
            messages.add(ValidationErrorMessage.of(method, "Message bundle methods must be annotated with %s.", annotations.message()));
        }
        if (!(isAssignableFrom(method.getReturnType(), String.class) || isAssignableFrom(Throwable.class, method.getReturnType()))) {
            messages.add(ValidationErrorMessage.of(method,
                    "Message bundle %s has a method with invalid return type, method %s has a return type of %s",
                    root, method, method.getReturnType()));
        } else if (isAssignableFrom(Throwable.class, method.getReturnType())) {
            messages.addAll(checkExceptionConstructor(method));
        }
        return messages;
    }

    /**
     * Validates the exception constructor.
     *
     * @param method the method to validate.
     *
     * @return a collection of validation messages or an empty collection.
     */
    public final Collection<ValidationMessage> checkExceptionConstructor(final ExecutableElement method) {

        final Collection<ValidationMessage> result = new ArrayList<ValidationMessage>();
        final TypeMirror type = method.getReturnType();
        final Element element = typeUtil.asElement(type);
        final List<ExecutableElement> constructors = ElementFilter.constructorsIn(element.getEnclosedElements());
        final ReturnType returnType = ReturnType.of(type, typeUtil);

        boolean hasValidNonDefault = (returnType.hasStringAndThrowableConstructor() || returnType.hasStringConstructor() ||
                returnType.hasThrowableAndStringConstructor() || returnType.hasThrowableConstructor());

        if (!hasValidNonDefault && returnType.hasDefaultConstructor()) {
            result.add(ValidationWarningMessage.of(method, "Exception %s does not have a constructor to set the message. The message will be ignored.", type.toString()));
        } else if (!hasValidNonDefault && !returnType.hasDefaultConstructor()) {
            result.add(ValidationErrorMessage.of(method, "Type %s does not have a constructor that can be used to create the exception.", type.toString()));
        }
        // Warn if there are no string message parameters
        if (!returnType.hasStringAndThrowableConstructor() && !returnType.hasStringConstructor() && !returnType.hasThrowableAndStringConstructor()) {
            result.add(ValidationWarningMessage.of(method, "Exception %s does not have a constructor to set the message. The message will be ignored.", type.toString()));
        }
        return result;
    }


    /**
     * Returns a collection of methods with the same name.
     *
     * @param methods    the methods to process.
     * @param methodName the method name to find.
     *
     * @return a collection of methods with the same name.
     */
    public final Collection<ExecutableElement> findByName(final Collection<ExecutableElement> methods, final Name methodName) {
        final List<ExecutableElement> result = new ArrayList<ExecutableElement>();
        for (ExecutableElement method : methods) {
            if (methodName.equals(method.getSimpleName())) {
                result.add(method);
            }
        }
        return result;
    }

    /**
     * Checks to see if there is a cause parameter.
     *
     * @param params the parameters to check.
     *
     * @return {@code true} if there is a cause, otherwise {@code false}.
     */
    public final boolean hasCause(Collection<? extends VariableElement> params) {
        // Look for cause
        for (VariableElement param : params) {
            if (param.getAnnotation(annotations.cause()) != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks to see if the method has or inherits a {@link org.jboss.logging.Annotations#message()}  annotation.
     *
     * @param root   the interface, root element.
     * @param method the method to check.
     *
     * @return {@code true} if the method has or inherits a message annotation, otherwise {@code false}.
     */
    public final boolean hasOrInheritsMessage(final TypeElement root, final ExecutableElement method) {
        final Collection<ExecutableElement> allMethods = findByName(ElementHelper.getInterfaceMethods(root, typeUtil, null), method.getSimpleName());
        for (ExecutableElement m : allMethods) {
            if (isAnnotatedWith(m, annotations.message())) {
                return true;
            }
        }
        return false;
    }
}
