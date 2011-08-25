package org.jboss.logging.generator.validation;

import org.jboss.logging.generator.intf.model.MessageObject;

/**
 * Date: 12.08.2011
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public interface ValidationMessage {

    /**
     * Validation message type enum.
     */
    public enum Type {
        ERROR,
        WARN
    }

    /**
     * The type of the message.
     *
     * @return the type of the message.
     */
    Type type();

    /**
     * Returns the message object that caused the error.
     *
     * @return the message object that caused the error.
     */
    MessageObject getMessageObject();

    /**
     * Returns the error message.
     *
     * @return the error message.
     */
    String getMessage();
}
