package org.jboss.logging.generator.validation;

import javax.lang.model.element.Element;

/**
 * A message that contains information about the validation error.
 * <p/>
 * <p>
 * The message is message to print and the element was the element that caused
 * the error.
 * </p>
 *
 * @author Kevin Pollet - SERLI - (kevin.pollet@serli.com)
 */
public class ValidationErrorMessage implements ValidationMessage {

    private final Element element;
    private final String message;

    /**
     * Class constructor.
     *
     * @param element the element that caused the error.
     * @param message the error message.
     */
    public ValidationErrorMessage(final Element element, final String message) {
        this.element = element;
        this.message = message;
    }

    /**
     * Creates a new validation error message.
     *
     * @param element the element to create the message for.
     * @param message the message for the error.
     *
     * @return a new validation error message.
     */
    public static ValidationErrorMessage of(final Element element, final String message) {
        return new ValidationErrorMessage(element, message);
    }

    /**
     * Creates a new formatted validation error message.
     *
     * @param element       the element to create the message for.
     * @param messageFormat the message format.
     * @param args          the replacement arguments for {@link String#format(java.lang.String, java.lang.Object[])}.
     *
     * @return a new validation error message.
     */
    public static ValidationErrorMessage of(final Element element, final String messageFormat, final Object... args) {
        return new ValidationErrorMessage(element, String.format(messageFormat, args));
    }

    @Override
    public MessageType type() {
        return MessageType.ERROR;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int hash = 1;
        hash = prime * hash + ((element == null) ? 0 : element.hashCode());
        hash = prime * hash + ((message == null) ? 0 : message.hashCode());
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof ValidationErrorMessage)) {
            return false;
        }
        final ValidationErrorMessage other = (ValidationErrorMessage) obj;
        if ((this.element == null) ? (other.element != null) : !this.element.equals(other.message)) {
            return false;
        }
        if ((this.message == null) ? (other.message != null) : !this.message.equals(other.message)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        final StringBuilder result = new StringBuilder();
        result.append(getClass().getSimpleName()).
                append("(element=").
                append(element).
                append(", message=").
                append(message).
                append(")");
        return result.toString();
    }

    @Override
    public Element getElement() {
        return element;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

}
