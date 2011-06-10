package org.jboss.logging.generator.validation.validator;

/**
 * The parameter portion of the a {@link java.text.MessageFormat}.
 * <p/>
 * <i><b>**Note:</b> Currently the format type and format style are not validated</i>
 * <p/>
 * Date: 14.06.2011
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
class MessageFormatPart extends AbstractFormatPart {
    private final String originalFormat;
    private final int position;
    private int index;
    private String formatType;
    private String formatStyle;

    private MessageFormatPart(final int position, final String format) {
        this.position = position;
        originalFormat = format;
        index = 0;
    }

    public static MessageFormatPart of(final int position, final String format) {
        final MessageFormatPart result = new MessageFormatPart(position, format);
        // The first character and last character must be { and } respectively.
        if (format.charAt(0) != '{' || format.charAt(format.length() - 1) != '}') {
            throw new IllegalArgumentException("Format must begin with '{' and end with '}'. Format: " + format);
        }
        // Trim off the {}
        String formatText = format.substring(1, format.length() - 1);
        // Can't contain any more { or }
        if (formatText.contains("{") || formatText.contains("}")) {
            throw new IllegalArgumentException("String contains an invalid character. Cannot specify either '{' or '}'.");
        }
        result.init(formatText);
        return result;
    }

    @Override
    public int index() {
        return index;
    }

    @Override
    public int position() {
        return position;
    }

    @Override
    public String part() {
        return originalFormat;
        /** Should use something like this when
         final StringBuilder result = new StringBuilder("{");
         if (index >= 0) {
         result.append(index);
         }
         if (formatType != null) {
         result.append(",").append(formatType);
         }
         if (formatStyle != null) {
         result.append(",").append(formatStyle);
         }
         return result.append("}").toString();
         **/
    }

    private void init(final String formatText) {
        if (formatText != null && !formatText.trim().isEmpty()) {
            try {
                index = Integer.parseInt(formatText.substring(0, 1));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid index portion of format.", e);
            }
        }
    }
}
