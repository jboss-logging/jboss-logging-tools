package org.jboss.logging.processor.generated;

@SuppressWarnings({ "InstanceVariableMayNotBeInitialized", "unused" })
public class MethodMessageConstantsTypeException extends RuntimeException {
    public Class<?> type;
    public Object value;

    public MethodMessageConstantsTypeException() {
    }

    public MethodMessageConstantsTypeException(final String msg) {
        super(msg);
    }

    public MethodMessageConstantsTypeException(final Throwable t) {
        super(t);
    }

    public MethodMessageConstantsTypeException(final String msg, final Throwable t) {
        super(msg, t);
    }

    public void setValue(final Object value) {
        this.value = value;
    }

    public void setType(final Class<?> type) {
        this.type = type;
    }
}
