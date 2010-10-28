package org.jboss.logging.validation;

/**
 * @author Kevin Pollet
 */
public class ValidationErrorMessage {

    private String message;

    public ValidationErrorMessage(final String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }
    
}
