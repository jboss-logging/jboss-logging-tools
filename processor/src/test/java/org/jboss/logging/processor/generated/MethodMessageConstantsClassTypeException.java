package org.jboss.logging.processor.generated;

public class MethodMessageConstantsClassTypeException extends RuntimeException {
    @SuppressWarnings("InstanceVariableMayNotBeInitialized")
    public Class<?> value;

    public MethodMessageConstantsClassTypeException(final String message) {
        super(message);
    }

    public void setValue(final Class<?> value) {
        this.value = value;
    }
}
