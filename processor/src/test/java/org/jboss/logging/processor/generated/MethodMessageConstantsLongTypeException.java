package org.jboss.logging.processor.generated;

public class MethodMessageConstantsLongTypeException extends RuntimeException {
    @SuppressWarnings("InstanceVariableMayNotBeInitialized")
    public long value;

    public MethodMessageConstantsLongTypeException(final String message) {
        super(message);
    }

    public void setValue(final long value) {
        this.value = value;
    }
}
