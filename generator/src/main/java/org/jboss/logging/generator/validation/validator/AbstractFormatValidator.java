package org.jboss.logging.generator.validation.validator;

/**
 * Date: 12.08.2011
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
abstract class AbstractFormatValidator implements FormatValidator {
    private String summaryMessage;
    private String detailMessage;

    protected AbstractFormatValidator() {
        detailMessage = "";
        summaryMessage = "";
    }

    protected final void setDetailMessage(final String detailMessage) {
        this.detailMessage = detailMessage;
    }

    protected final void setDetailMessage(final String format, final Object... args) {
        this.detailMessage = String.format(format, args);
    }

    protected final void setSummaryMessage(final String summaryMessage) {
        this.summaryMessage = summaryMessage;
    }

    protected final void setSummaryMessage(final String format, final Object... args) {
        this.summaryMessage = String.format(format, args);
    }

    @Override
    public final String detailMessage() {
        return (detailMessage.isEmpty() ? summaryMessage : detailMessage);
    }

    @Override
    public final String summaryMessage() {
        return summaryMessage;
    }
}
