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

package org.jboss.logging.generator.validation.validator;

import org.jboss.logging.generator.LoggingTools;
import org.jboss.logging.generator.ReturnType;
import org.jboss.logging.generator.validation.ElementValidator;
import org.jboss.logging.generator.validation.ValidationErrorMessage;
import org.jboss.logging.generator.validation.ValidationMessage;
import org.jboss.logging.generator.validation.ValidationWarningMessage;

import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.jboss.logging.generator.util.ElementHelper.hasOrInheritsMessage;
import static org.jboss.logging.generator.util.ElementHelper.isAssignableFrom;

/**
 * User: jrp
 * Date: 9/4/11
 * Time: 14:27
 *
 * @author <a href="mailto:jrperkinsjr@gmail.com">James R. Perkins</a>
 */
abstract class AbstractValidator implements ElementValidator {
    private final Types typeUtil;

    protected AbstractValidator(final Types typeUtil) {
        this.typeUtil = typeUtil;
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
        if (!hasOrInheritsMessage(root, method, typeUtil)) {
            messages.add(ValidationErrorMessage.of(method, "Message bundle methods must be annotated with %s.", LoggingTools.annotations().message()));
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
}
