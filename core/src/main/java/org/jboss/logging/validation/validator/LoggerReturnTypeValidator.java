/*
 *  JBoss, Home of Professional Open Source Copyright 2010, Red Hat, Inc., and
 *  individual contributors by the @authors tag. See the copyright.txt in the
 *  distribution for a full listing of individual contributors.
 *
 *  This is free software; you can redistribute it and/or modify it under the
 *  terms of the GNU Lesser General Public License as published by the Free
 *  Software Foundation; either version 2.1 of the License, or (at your option)
 *  any later version.
 *
 *  This software is distributed in the hope that it will be useful, but WITHOUT
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 *  FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 *  details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this software; if not, write to the Free Software Foundation,
 *  Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
 *  site: http://www.fsf.org.
 */
package org.jboss.logging.validation.validator;

import static org.jboss.logging.util.ElementHelper.*;
import org.jboss.logging.util.ElementHelper;
import org.jboss.logging.validation.ElementValidator;
import org.jboss.logging.validation.ValidationErrorMessage;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import java.util.ArrayList;
import java.util.Collection;
import org.jboss.logging.Annotations;

/**
 * Validates the return type for logger methods.
 * <p>
 * Must have a return type of void.
 * </p>
 *
 * @author James R. Perkins (jrp)
 */
public class LoggerReturnTypeValidator implements ElementValidator {

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ValidationErrorMessage> validate(final TypeElement element, final Collection<ExecutableElement> elementMethods,
            final Annotations annotations) {

        final Collection<ValidationErrorMessage> errorMessages = new ArrayList<ValidationErrorMessage>();

        if (ElementHelper.isAnnotatedWith(element, annotations.messageLogger())) {
            for (ExecutableElement method : elementMethods) {
                if (isAnnotatedWith(method, annotations.logMessage())) {
                    if (method.getReturnType().getKind() != TypeKind.VOID) {
                        errorMessages.add(ValidationErrorMessage.of(method,
                                "Methods annotated with %s must have a void return type.",
                                annotations.logMessage().getName()));
                    }
                } else {
                    if (method.getReturnType().getKind() == TypeKind.VOID) {
                        errorMessages.add(ValidationErrorMessage.of(method,
                                "Cannot have a void return type if the method is not a log method."));
                    } else {
                        if (!(ElementHelper.isAssignableFrom(method.getReturnType(), String.class) ||
                                ElementHelper.isAssignableFrom(Throwable.class, method.getReturnType()))) {
                                errorMessages.add(ValidationErrorMessage.of(element,
                                        "Method %s has an invalud return type type. Must have a String or Throwable return type if not annotated with %s.",
                                        method, annotations.logMessage().getName()));
                        }
                    }
                }
            }
        }

        return errorMessages;
    }
}