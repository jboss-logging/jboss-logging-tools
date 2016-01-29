/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2013, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.logging.processor.validation;

import static org.jboss.logging.processor.validation.ValidationMessageFactory.createError;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jboss.logging.processor.model.MessageInterface;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class IdLengthValidator {
    private final Map<String, Integer> lengths = new HashMap<>();

    public Collection<ValidationMessage> validate(final MessageInterface messageInterface) {
        final List<ValidationMessage> messages = new LinkedList<>();
        final String projectCode = messageInterface.projectCode();
        final int idLength = messageInterface.getIdLength();
        if ((idLength > 0 && idLength < 3) || idLength > 8) {
            messages.add(createError(messageInterface, "The length of the message id padding must be between 3 and 8. The value %d is invalid.", idLength));
        } else {
            synchronized (this) {
                // Check the length id's
                if (lengths.containsKey(projectCode)) {
                    final int len = lengths.get(projectCode);
                    if (len != idLength) {
                        messages.add(createError(messageInterface, "A length of %d was already used for project code '%s'.", len, projectCode));
                    }
                } else {
                    lengths.put(projectCode, idLength);
                }
            }
        }
        return messages;
    }
}
