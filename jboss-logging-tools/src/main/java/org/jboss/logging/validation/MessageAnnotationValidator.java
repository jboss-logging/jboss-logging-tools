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

import org.jboss.logging.Message;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Checks to make sure that only one {@link org.jboss.logging.Message}
 * annotation is present on like named methods.
 *
 * @author James R. Perkins (jrp)
 */
public class MessageAnnotationValidator implements Validator {

    private static final Class<? extends Annotation> annotationClass = Message.class;

    private final Collection<ExecutableElement> methods;

    /**
     * Class constructor for singleton.
     */
    public MessageAnnotationValidator(final Collection<ExecutableElement> methods) {
        this.methods = methods;
    }

    /**
     *{@inheritDoc}
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
                boolean foundFirst = false;
                for (ExecutableElement m : likeMethods) {
                    boolean found = m.getAnnotation(annotationClass) != null;
                    if (foundFirst && found) {
                        throw new ValidationException("Only one method is allowed to be annotated with the " + annotationClass.
                                getName() + " annotation.", m);
                    }
                    foundFirst = found;
                }
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
}
