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
import org.jboss.logging.Message;

import java.util.HashSet;
import java.util.Set;
import javax.lang.model.element.ExecutableElement;

/**
 * Validates messages id's from the {@link org.jboss.logging.Message} annotation.
 *
 * <p>
 * Message id's must be unique for each method unless the methods have the same
 * name.
 * </p>
 *
 * @author James R. Perkins (jrp)
 */
public class MessageIdValidator implements Validator {

    private final Collection<ExecutableElement> methods;

    public MessageIdValidator(final Collection<ExecutableElement> methods) {
        this.methods = methods;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validate() throws ValidationException {
        final Set<Integer> messageIds = new HashSet<Integer>();
        final Set<String> methodNames = new HashSet<String>();
        // Process method descriptors
        for (ExecutableElement method : methods) {
            if (methodNames.add(method.getSimpleName().toString())) {
                Message message = method.getAnnotation(Message.class);
                if (message != null) {
                    if (message.id() > Message.NONE && !messageIds.add(
                            message.id())) {
                        throw new ValidationException(
                                "Message id's must be unique.", method);
                    }
                }
            }
        }
    }
}
