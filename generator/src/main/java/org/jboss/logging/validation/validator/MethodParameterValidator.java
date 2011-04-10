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

import org.jboss.logging.Annotations;
import org.jboss.logging.validation.ValidationErrorMessage;
import org.jboss.logging.validation.ValidationMessage;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Types;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Validates the parameters of a method.
 * <p/>
 * <p>
 * Multiple methods with the same name are permitted, as long as they meet all
 * of the following criteria:
 * <ul>
 * <li>They have the same number of non-{@link org.jboss.logging.Annotations#cause()} parameters.</li>
 * <li>Only one of the methods may specify a {@link org.jboss.logging.Annotations#message()}
 * annotation.
 * </li>
 * </ul>
 * </p>
 *
 * @author James R. Perkins Jr. (jrp)
 */
public class MethodParameterValidator extends AbstractValidator {

    public MethodParameterValidator(final Annotations annotations, final Types typeUtil) {
        super(annotations, typeUtil);
    }

    private static final String ERROR_MESSAGE = "The number of parameters, minus the cause parameter, must match all match all methods with the same name. "
            + "Method %s accepts %d parameters and method %s accepts %d parameters.";

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ValidationMessage> validate(final TypeElement element, final Collection<ExecutableElement> elementMethods) {

        final List<ValidationMessage> messages = new ArrayList<ValidationMessage>();

        // Set for the method names that have been processed
        final Set<Name> methodNames = new HashSet<Name>();
        for (ExecutableElement method : elementMethods) {
            // Only adds methods which have not been processed
            if (methodNames.add(method.getSimpleName())) {
                // Find all like named methods
                final Collection<ExecutableElement> likeMethods = findByName(elementMethods, method.getSimpleName());
                final int paramCount1 = method.getParameters().size() - (hasCause(method.getParameters()) ? 1 : 0);
                for (ExecutableElement m : likeMethods) {
                    int paramCount2 = m.getParameters().size() - (hasCause(m.getParameters()) ? 1 : 0);
                    if (paramCount1 != paramCount2) {
                        messages.add(ValidationErrorMessage.of(m,
                                ERROR_MESSAGE, method.toString(), method.getParameters().size(), m.toString(), m.getParameters().size()));
                    }
                }
            }

            // Finally the method is only allowed one cause parameter
            Annotation ogCause = null;
            for (VariableElement varElem : method.getParameters()) {
                final Annotation cause = varElem.getAnnotation(annotations.cause());
                boolean invalid = (ogCause != null && cause != null);
                if (invalid) {
                    messages.add(ValidationErrorMessage.of(varElem, "Only one cause parameter allowed per method."));
                }
                ogCause = cause;
            }
        }

        return messages;
    }
}
