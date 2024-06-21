package org.jboss.logging.processor.generated;

public class MethodMessageConstantsBooleanTypeException extends RuntimeException {
    @SuppressWarnings("InstanceVariableMayNotBeInitialized")
    public boolean value;

    public MethodMessageConstantsBooleanTypeException(final String message) {
        super(message);
    }

    public void setValue(final boolean value) {
        this.value = value;
    }
}
