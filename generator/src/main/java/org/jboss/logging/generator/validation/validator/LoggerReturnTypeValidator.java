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
package org.jboss.logging.generator.validation.validator;

import org.jboss.logging.generator.LoggingTools;
import org.jboss.logging.generator.validation.ValidationErrorMessage;
import org.jboss.logging.generator.validation.ValidationMessage;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.Collection;

import static org.jboss.logging.generator.util.ElementHelper.isAnnotatedWith;

/**
 * Validates the return type for logger methods.
 * <p>
 * Must have a return type of void.
 * </p>
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class LoggerReturnTypeValidator extends AbstractValidator {

    public LoggerReturnTypeValidator(final Types typeUtil) {
        super(typeUtil);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ValidationMessage> validate(final TypeElement element, final Collection<ExecutableElement> elementMethods) {

        final Collection<ValidationMessage> messages = new ArrayList<ValidationMessage>();

        if (isAnnotatedWith(element, LoggingTools.annotations().messageLogger())) {
            for (ExecutableElement method : elementMethods) {
                if (isAnnotatedWith(method, LoggingTools.annotations().logMessage())) {
                    if (method.getReturnType().getKind() != TypeKind.VOID) {
                        messages.add(ValidationErrorMessage.of(method, "Methods annotated with %s must have a void return type.", LoggingTools.annotations().logMessage().getName()));
                    }
                } else {
                    if (method.getReturnType().getKind() == TypeKind.VOID) {
                        messages.add(ValidationErrorMessage.of(method, "Cannot have a void return type if the method is not a log method."));
                    } else {
                        messages.addAll(checkMessageBundleMethod(element, method));
                    }
                }
            }
        }

        return messages;
    }
}