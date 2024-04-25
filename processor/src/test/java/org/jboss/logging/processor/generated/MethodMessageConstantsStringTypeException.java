package org.jboss.logging.processor.generated;

public class MethodMessageConstantsStringTypeException extends RuntimeException {
    @SuppressWarnings("InstanceVariableMayNotBeInitialized")
    public String value;

    public MethodMessageConstantsStringTypeException(final String message) {
        super(message);
    }

    public void setValue(final String value) {
        this.value = value;
    }
}
