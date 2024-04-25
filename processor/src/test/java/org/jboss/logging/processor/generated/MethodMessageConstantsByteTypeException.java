package org.jboss.logging.processor.generated;

public class MethodMessageConstantsByteTypeException extends RuntimeException {
    @SuppressWarnings("InstanceVariableMayNotBeInitialized")
    public byte value;

    public MethodMessageConstantsByteTypeException(final String message) {
        super(message);
    }

    public void setValue(final byte value) {
        this.value = value;
    }
}
