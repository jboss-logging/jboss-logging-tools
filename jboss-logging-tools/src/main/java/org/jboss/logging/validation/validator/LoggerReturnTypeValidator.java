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
import org.jboss.logging.MessageLogger;
import org.jboss.logging.util.ElementHelper;
import org.jboss.logging.validation.ElementValidator;
import org.jboss.logging.validation.ValidationErrorMessage;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Validates the return type for logger methods.
 * <p>
 * Must have a return type of void.
 * </p>
 *
 * @author James R. Perkins (jrp)
 */
public class LoggerReturnTypeValidator implements ElementValidator {

    private static final String ERROR_MESSAGE = "Logger methods must have void or string return types, method %s return type is %s";

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ValidationErrorMessage> validate(final TypeElement element, final Collection<ExecutableElement> elementMethods) {

        final Collection<ValidationErrorMessage> errorMessages = new ArrayList<ValidationErrorMessage>();

        if (ElementHelper.isAnnotatedWith(element, MessageLogger.class)) {
            for (ExecutableElement method : elementMethods) {
                if (isAnnotatedWith(method, LOG_MESSAGE_ANNOTATION)) {
                    if (method.getReturnType().getKind() != TypeKind.VOID) {
                        errorMessages.add(createMessage(method));
                    }
                } else {
                    if (method.getReturnType().getKind() == TypeKind.VOID) {
                        errorMessages.add(new ValidationErrorMessage(method, "Cannot have a void return type if the method is not a log method."));
                    } else {
                        try {
                            Class<?> returnClass = Class.forName(method.getReturnType().toString());
                            if (!returnClass.isAssignableFrom(String.class)) {
                                errorMessages.add(createMessage(method));
                            }

                        } catch (ClassNotFoundException e) {
                            String message = String.format("Return type %s for method %s is not in the classpath", method.getReturnType(), method);
                            errorMessages.add(new ValidationErrorMessage(method, message));
                        }
                    }
                }
            }
        }

        return errorMessages;
    }

    private ValidationErrorMessage createMessage(final ExecutableElement method) {
        String message = String.format(ERROR_MESSAGE, method, method.getReturnType());
        return new ValidationErrorMessage(method, message);
    }
}
