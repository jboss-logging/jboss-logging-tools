package org.jboss.logging.processor.generated;

public class MethodMessageConstantsIntTypeException extends RuntimeException {
    @SuppressWarnings("InstanceVariableMayNotBeInitialized")
    public int value;

    public MethodMessageConstantsIntTypeException(final String message) {
        super(message);
    }

    public void setValue(final Integer value) {
        this.value = value;
    }
}
