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

import javax.lang.model.element.ExecutableElement;
import java.util.Collection;

/**
 * Validates the return types for message bundle methods.
 *
 * <p>
 * The return type must be either a {@link java.lang.String} or one of it's
 * super types, or {@link java.lang.Throwable} or one of it's subtypes.
 * </p>
 *
 * @author James R. Perkins (jrp)
 */
public class BundleReturnTypeValidator implements Validator {

    private final Collection<ExecutableElement> methods;

    /**
     * Class constructor.
     *
     * @param methods the methods to process
     */
    public BundleReturnTypeValidator(final Collection<ExecutableElement> methods) {
        this.methods = methods;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate() throws ValidationException {
        for (ExecutableElement method : methods) {
            boolean invalid = true;
            try {
                if (Throwable.class.isAssignableFrom(Class.forName(method.
                        getReturnType().toString()))) {
                    invalid = false;
                }
                if (Class.forName(method.getReturnType().toString()).
                        isAssignableFrom(String.class)) {
                    invalid = false;
                }
            } catch (ClassNotFoundException e) {
                throw new ValidationException("Return type not found in classpath.", e, method);
            }
            if (invalid) {
                throw new ValidationException("Invalid return type.", method);
            }
        }
    }
}
