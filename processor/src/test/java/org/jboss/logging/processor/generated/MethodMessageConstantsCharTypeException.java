package org.jboss.logging.processor.generated;

public class MethodMessageConstantsCharTypeException extends RuntimeException {
    @SuppressWarnings("InstanceVariableMayNotBeInitialized")
    public char value;

    public MethodMessageConstantsCharTypeException(final String message) {
        super(message);
    }

    public void setValue(final char value) {
        this.value = value;
    }
}
