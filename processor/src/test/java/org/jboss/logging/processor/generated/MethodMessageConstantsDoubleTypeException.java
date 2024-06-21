package org.jboss.logging.processor.generated;

public class MethodMessageConstantsDoubleTypeException extends RuntimeException {
    @SuppressWarnings("InstanceVariableMayNotBeInitialized")
    public double value;

    public MethodMessageConstantsDoubleTypeException(final String message) {
        super(message);
    }

    public void setValue(final double value) {
        this.value = value;
    }
}
