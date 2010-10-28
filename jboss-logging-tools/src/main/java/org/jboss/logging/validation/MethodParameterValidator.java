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

import org.jboss.logging.Cause;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.VariableElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    private final Collection<ExecutableElement> methods;

    /**
     * Class constructor.
     *
     * @param methodDescriptor the method descriptor to process.
     */
    public MethodParameterValidator(final Collection<ExecutableElement> methods) {
        this.methods = methods;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate() throws ValidationException {
        // Set for the method names that have been processed
        final Set<Name> methodNames = new HashSet<Name>();
        for (ExecutableElement method : methods) {
            // Only adds methods which have not been processed
            if (methodNames.add(method.getSimpleName())) {
                // Find all like named methods
                final Collection<ExecutableElement> likeMethods = findByName(method.
                        getSimpleName());
                final int paramCount1 = method.getParameters().size() - (hasCause(method.
                        getParameters()) ? 1 : 0);
                for (ExecutableElement m : likeMethods) {
                    int paramCount2 = m.getParameters().size() - (hasCause(m.
                            getParameters()) ? 1 : 0);
                    if (paramCount1 != paramCount2) {
                        throw new ValidationException(
                                "The number of parameters, minus the clause parameter, must match all methods with the same name.",
                                m);
                    }
                }
            }

            // Finally the method is only allowed one cause parameter
            boolean invalid = false;
            Cause ogCause = null;
            for (VariableElement varElem : method.getParameters()) {
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

    /**
     * Returns a collection of methods with the same name.
     *
     * @param methodName the method name to find.
     *
     * @return a collection of methods with the same name.
     */
    private Collection<ExecutableElement> findByName(final Name methodName) {
        final List<ExecutableElement> result = new ArrayList<ExecutableElement>();
        for (ExecutableElement method : methods) {
            if (methodName.equals(method.getSimpleName())) {
                result.add(method);
            }
        }
        return result;
    }

    private boolean hasCause(Collection<? extends VariableElement> params) {
        // Look for cause
        for (VariableElement param : params) {
            if (param.getAnnotation(Cause.class) != null) {
                return true;
            }
        }
        return false;
    }
}
