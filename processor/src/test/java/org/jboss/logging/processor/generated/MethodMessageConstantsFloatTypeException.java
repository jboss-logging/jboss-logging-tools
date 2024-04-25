package org.jboss.logging.processor.generated;

public class MethodMessageConstantsFloatTypeException extends RuntimeException {
    @SuppressWarnings("InstanceVariableMayNotBeInitialized")
    public float value;

    public MethodMessageConstantsFloatTypeException(final String message) {
        super(message);
    }

    public void setValue(final float value) {
        this.value = value;
    }
}
