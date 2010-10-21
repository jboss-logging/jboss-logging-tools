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
package org.jboss.logging.validation;

import java.util.Collection;
import javax.lang.model.element.VariableElement;
import org.jboss.logging.Cause;
import org.jboss.logging.Message;

import org.jboss.logging.model.MethodDescriptor;

/**
 * Validates the parameters of a method.
 *
 * <p>
 * Multiple methods with the same name are permitted, as long as they meet all
 * of the following criteria:
 * <ul>
 *   <li>They have the same number of non-{@link org.jboss.logging.Cause} parameters.</li>
 *   <li>Only one of the methods may specify a {@link org.jboss..Message}
 *       annotation.
 *   </li>
 * </ul>
 * </p>
 *
 * @author James R. Perkins Jr. (jrp)
 * 
 */
public class MethodParameterValidator implements Validator {

    private final MethodDescriptor methodDescriptor;

    /**
     * Class constructor.
     *
     * @param methodDescriptor the method descriptor to process.
     */
    public MethodParameterValidator(final MethodDescriptor methodDescriptor) {
        this.methodDescriptor = methodDescriptor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate() throws ValidationException {
        final Collection<MethodDescriptor> methodDescriptors = methodDescriptor.
                find(methodDescriptor.name());
        final int paramCount1 = methodDescriptor.parameters().size()
                - ((methodDescriptor.hasClause()) ? 1 : 0);
        // Validate the parameters
        for (MethodDescriptor methodDesc : methodDescriptors) {
            final int paramCount2 = methodDesc.parameters().size()
                    - ((methodDesc.hasClause()) ? 1 : 0);
            if (paramCount1 != paramCount2) {
                throw new ValidationException(
                        "The number of parameters, minus the clause parameter, must match all methods with the same name.",
                        methodDesc.method());
            }
            // The method must also have a message annotation
            if (methodDesc.message() == null) {
                throw new ValidationException(
                        "All defined methods must have a @" + Message.class.
                        getName() + " annotation unless a method with the same name has the annotation present.", methodDesc.
                        method());
            }

            // Finally the method is only allowed one cause parameter
            boolean invalid = false;
            Cause ogCause = null;
            for (VariableElement varElem : methodDesc.method().getParameters()) {
                final Cause cause = varElem.getAnnotation(Cause.class);
                invalid = (ogCause != null && cause != null);
                if (invalid) {
                    throw new ValidationException(
                            "Only allowed one cause parameter per method.",
                            varElem);
                }
                ogCause = cause;
            }
        }
    }
}
