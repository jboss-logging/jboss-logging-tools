package org.jboss.logging.validation;

import javax.lang.model.element.Element;

/**
 * A message that contains information about the validation error.
 *
 * <p>
 * The message is message to print and the element was the element that caused
 * the error.
 * </p>
 *
 * @author Kevin Pollet
 */
public class ValidationErrorMessage {

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
     * @return a new validation error message.
     */
    public static ValidationErrorMessage of(final Element element, final String messageFormat, final Object... args) {
        return new ValidationErrorMessage(element, String.format(messageFormat, args));
    }

    /**
     * Returns the element that caused the error.
     *
     * @return the element that caused the error.
     */
    public Element getElement() {
        return element;
    }

    /**
     * Returns the error message.
     * 
     * @return the error message.
     */
    public String getMessage() {
        return this.message;
    }
    
}
