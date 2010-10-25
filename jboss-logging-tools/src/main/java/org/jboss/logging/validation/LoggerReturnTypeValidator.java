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

import org.jboss.logging.model.MethodDescriptor;

import javax.lang.model.type.TypeKind;

/**
 * Validates the return type for logger methods.
 *
 * <p>
 * Must have a return type of void.
 * </p>
 *
 * @author James R. Perkins (jrp)
 */
public class LoggerReturnTypeValidator implements Validator {

    private final MethodDescriptor methodDesc;

    /**
     * Class constructor.
     *
     * @param methodDesc the method descriptor to validate.
     */
    public LoggerReturnTypeValidator(final MethodDescriptor methodDesc) {
        this.methodDesc = methodDesc;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate() throws ValidationException {
        if (methodDesc.returnType().getKind() != TypeKind.VOID) {
            throw new ValidationException(
                    "Logger methods must have void return types.", methodDesc.
                    method());
        }
    }
}
