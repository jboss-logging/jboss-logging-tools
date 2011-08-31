package org.jboss.logging.generator.validation;

import org.jboss.logging.generator.intf.model.Method;
import org.jboss.logging.generator.util.Comparison;
import org.jboss.logging.generator.util.Objects;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.jboss.logging.generator.util.Objects.HashCodeBuilder;
import static org.jboss.logging.generator.util.Objects.areEqual;
import static org.jboss.logging.generator.validation.ValidationMessageFactory.createError;

/**
 * Date: 16.08.2011
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public final class MessageIdValidator {
    public static final MessageIdValidator INSTANCE = new MessageIdValidator();

    private final Map<MessageKey, Method> usedMessageIds = new HashMap<MessageKey, Method>();

    private MessageIdValidator() {
    }

    public Collection<ValidationMessage> validate(final String projectCode, final Method method) {
        final List<ValidationMessage> messages = new LinkedList<ValidationMessage>();
        final Method.Message message = method.message();
        if (message == null) {
            messages.add(createError(method, "No message annotation found."));
        } else {
            final MessageKey key = createMessageKey(projectCode, message.id());
            if (!method.inheritsMessage()) {
                synchronized (this) {
                    if (usedMessageIds.containsKey(key)) {
                        final Method previousMethod = usedMessageIds.get(key);
                        messages.add(createError(previousMethod, "Message id %s is not unique for method %s with project code %s.", message.id(), previousMethod.name(), projectCode));
                        messages.add(createError(method, "Message id %s is not unique for method %s with project code %s.", message.id(), method.name(), projectCode));
                    } else {
                        usedMessageIds.put(key, method);
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
