/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2023 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

import org.jboss.logging.annotations.ValidIdRange;
import org.jboss.logging.processor.model.MessageInterface;
import org.jboss.logging.processor.model.MessageMethod;
import org.jboss.logging.processor.util.Comparison;

/**
 * Date: 16.08.2011
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public final class MessageIdValidator {

    private final Map<MessageKey, MessageMethod> usedMessageIds = new HashMap<>();

    MessageIdValidator() {
    }

    public Collection<ValidationMessage> validate(final MessageInterface messageInterface, final MessageMethod messageMethod) {
        final List<ValidationMessage> messages = new LinkedList<>();
        final MessageMethod.Message message = messageMethod.message();
        if (message == null) {
            messages.add(createError(messageMethod, "No message annotation found."));
        } else {
            final Collection<MessageMethod> interfaceMessageMethods = messageInterface.methods();
            if (!messageMethod.inheritsMessage() && !message.inheritsId()) {
                final int id = message.id();
                if (interfaceMessageMethods.contains(messageMethod)) {
                    final List<ValidIdRange> validIdRanges = messageInterface.validIdRanges();
                    boolean invalidId = !validIdRanges.isEmpty();
                    for (ValidIdRange validIdRange : validIdRanges) {
                        if (id >= validIdRange.min() && id <= validIdRange.max()) {
                            invalidId = false;
                            break;
                        }
                    }
                    if (invalidId) {
                        final StringBuilder ranges = new StringBuilder();
                        int count = 0;
                        for (ValidIdRange validIdRange : validIdRanges) {
                            ranges.append(validIdRange.min()).append('-').append(validIdRange.max());
                            if (++count < validIdRanges.size()) {
                                ranges.append(", ");
                            }
                        }
                        messages.add(createError(messageMethod, "Message id %d on method %s is not within the valid range: %s",
                                id, messageMethod.name(), ranges.toString()));
                    }
                }
                final String projectCode = messageInterface.projectCode();
                final MessageKey key = createMessageKey(projectCode, id);
                synchronized (this) {
                    if (usedMessageIds.containsKey(key)) {
                        final MessageMethod previousMethod = usedMessageIds.get(key);
                        // Allow methods with the same name to use the same id, like INHERIT does
                        if (!previousMethod.name().equals(messageMethod.name())) {
                            messages.add(createError(previousMethod,
                                    "Message id %s is not unique for messageMethod %s with project code %s.", id,
                                    previousMethod.name(), projectCode));
                            messages.add(createError(messageMethod,
                                    "Message id %s is not unique for messageMethod %s with project code %s.", id,
                                    messageMethod.name(), projectCode));
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
