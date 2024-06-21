package org.jboss.logging.processor.generated;

public class MethodMessageConstantsShortTypeException extends RuntimeException {
    @SuppressWarnings("InstanceVariableMayNotBeInitialized")
    public short value;

    public MethodMessageConstantsShortTypeException(final String message) {
        super(message);
    }

    public void setValue(final short value) {
        this.value = value;
    }
}
