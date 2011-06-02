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
import org.jboss.logging.generator.validation.ValidationMessage;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.Collection;

import static org.jboss.logging.generator.util.ElementHelper.isAnnotatedWith;

/**
 * Validates the return types for message bundle methods.
 * <p/>
 * <p>
 * The return type must be either a {@link java.lang.String} or one of it's
 * super types, or {@link java.lang.Throwable} or one of it's subtypes.
 * </p>
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class BundleReturnTypeValidator extends AbstractValidator {

    public BundleReturnTypeValidator(final Types typeUtil) {
        super(typeUtil);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ValidationMessage> validate(final TypeElement element, final Collection<ExecutableElement> elementMethods) {

        Collection<ValidationMessage> messages = new ArrayList<ValidationMessage>();

        if (isAnnotatedWith(element, LoggingTools.annotations().messageBundle())) {

            for (ExecutableElement method : elementMethods) {
                messages.addAll(checkMessageBundleMethod(element, method));
            }
        }

        return messages;
    }
}
