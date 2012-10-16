/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

import static org.jboss.logging.processor.util.Objects.HashCodeBuilder;
import static org.jboss.logging.processor.util.Objects.areEqual;
import static org.jboss.logging.processor.validation.ValidationMessageFactory.createError;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jboss.logging.processor.model.MessageMethod;
import org.jboss.logging.processor.util.Comparison;

/**
 * Date: 16.08.2011
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public final class MessageIdValidator {

    private final Map<MessageKey, MessageMethod> usedMessageIds = new HashMap<MessageKey, MessageMethod>();

    MessageIdValidator() {
    }

    public Collection<ValidationMessage> validate(final String projectCode, final MessageMethod messageMethod) {
        final List<ValidationMessage> messages = new LinkedList<ValidationMessage>();
        final MessageMethod.Message message = messageMethod.message();
        if (message == null) {
            messages.add(createError(messageMethod, "No message annotation found."));
        } else {
            if (!messageMethod.inheritsMessage() && !message.inheritsId()) {
                final MessageKey key = createMessageKey(projectCode, message.id());
                synchronized (this) {
                    if (usedMessageIds.containsKey(key)) {
                        final MessageMethod previousMethod = usedMessageIds.get(key);
                        // Allow methods with the same name to use the same id, like INHERIT does
                        if (!previousMethod.name().equals(messageMethod.name())) {
                            messages.add(createError(previousMethod, "Message id %s is not unique for messageMethod %s with project code %s.", message.id(), previousMethod.name(), projectCode));
                            messages.add(createError(messageMethod, "Message id %s is not unique for messageMethod %s with project code %s.", message.id(), messageMethod.name(), projectCode));
                        }
                    } else {
                        usedMessageIds.put(key, messageMethod);
                    }
                }
            }
        }
        return messages;
    }

    private static MessageKey createMessageKey(final String projectCode, final int messageId) {
        return new MessageKey(projectCode, messageId);
    }

    private static class MessageKey implements Comparable<MessageKey> {
        final String projectCode;
        final int id;

        MessageKey(final String projectCode, final int id) {
            this.projectCode = (projectCode == null ? "" : projectCode);
            this.id = id;
        }

        @Override
        public int hashCode() {
            return HashCodeBuilder.builder()
                    .add(projectCode)
                    .add(id).toHashCode();
        }

        @Override
        public boolean equals(final Object obj) {
            if (obj == this) {
                return true;
            }
            if (!(obj instanceof MessageKey)) {
                return false;
            }
            final MessageKey other = (MessageKey) obj;
            return areEqual(projectCode, other.projectCode) && areEqual(id, other.id);
        }

        @Override
        public int compareTo(final MessageKey other) {
            return Comparison.begin()
                    .compare(projectCode, other.projectCode)
                    .compare(id, other.id).result();
        }
    }
}
