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

import org.jboss.logging.generator.validation.ValidationErrorMessage;
import org.jboss.logging.generator.validation.ValidationMessage;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.jboss.logging.generator.LoggingTools.annotations;

/**
 * Validates messages id's from the {@link org.jboss.logging.generator.Annotations#message()} annotation.
 * <p/>
 * <p>
 * Message id's must be unique for each method unless the methods have the same
 * name.
 * </p>
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class MessageIdValidator extends AbstractValidator {

    private static final String ERROR_MESSAGE = "Message id %s is not unique for method %s with project code %s.";
    private final Map<String, IdDescriptor> messageIdMap;

    public MessageIdValidator(final Types typeUtil) {
        super(typeUtil);
        messageIdMap = new HashMap<String, IdDescriptor>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<ValidationMessage> validate(final TypeElement element, final Collection<ExecutableElement> elementMethods) {
        final Collection<ValidationMessage> messages = new ArrayList<ValidationMessage>();

        // Process method descriptors
        for (ExecutableElement method : elementMethods) {
            if (annotations().hasMessageId(method) && !annotations().inheritsMessageId(method)) {
                final String projectCode = annotations().projectCode(element);
                final int messageId = annotations().messageId(method);
                final String key = createKey(projectCode, messageId);
                // If the id is in the map, create an error message.
                if (messageIdMap.containsKey(key)) {
                    final IdDescriptor idDesc = messageIdMap.get(key);
                    // Only add the original method once.
                    if (!idDesc.error) {
                        messages.add(ValidationErrorMessage.of(idDesc.method, ERROR_MESSAGE, messageId, idDesc.method, projectCode));
                        idDesc.error = true;
                    }
                    messages.add(ValidationErrorMessage.of(method, ERROR_MESSAGE, messageId, method, projectCode));
                } else {
                    messageIdMap.put(key, new IdDescriptor(messageId, method));
                }
            }
        }

        return messages;
    }

    private String createKey(final String projectCode, final int messageId) {
        final StringBuilder result = new StringBuilder();
        result.append(projectCode);
        result.append(":");
        result.append(messageId);
        return result.toString();
    }

    /**
     * Simple descriptor for id's.
     */
    private static class IdDescriptor {

        /**
         * The id of the message.
         */
        final int id;
        /**
         * The method annotated with the message.
         */
        final ExecutableElement method;
        /**
         * If the message id is in error.
         */
        boolean error = false;

        public IdDescriptor(final int id, final ExecutableElement method) {
            this.id = id;
            this.method = method;
        }
    }
}
