package org.jboss.logging.processor.generated;

import org.jboss.logging.Logger;
import org.jboss.logging.Logger.Level;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.Message.Format;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.logging.annotations.ValidIdRange;
import org.jboss.logging.annotations.ValidIdRanges;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@MessageLogger(projectCode = AbstractLoggerTest.PROJECT_CODE)
@ValidIdRanges({
        @ValidIdRange(min = 200, max = 202),
        @ValidIdRange(min = 203, max = 203)
})
public interface ValidLogger {

    final ValidLogger LOGGER = Logger.getMessageLogger(ValidLogger.class, AbstractLoggerTest.CATEGORY);

    @LogMessage(level = Level.INFO, loggingClass = ValidLogger.class)
    @Message(id = 200, value = "This is a generated message.")
    void testInfoMessage();

    @LogMessage(level = Level.INFO)
    @Message(id = 201, value = "Test message format message. Test value: {0}", format = Format.MESSAGE_FORMAT)
    void testMessageFormat(Object value);

    /**
     * Logs a default informational greeting.
     *
     * @param name the name.
     */
    @LogMessage
    @Message(id = 202, value = "Greetings %s")
    void greeting(String name);

    /**
     * Logs an error message indicating a processing error.
     */
    @LogMessage(level = Level.ERROR)
    @Message(id = 203, value = "Processing error")
    void processingError();

    /**
     * Logs an error message indicating a processing error.
     *
     * @param cause the cause of the error.
     */
    @LogMessage(level = Level.ERROR)
    void processingError(@Cause Throwable cause);

    /**
     * Logs an error message indicating there was a processing error.
     *
     * @param cause      the cause of the error.
     * @param moduleName the module that caused the error.
     */
    @LogMessage(level = Level.ERROR)
    @Message(id = Message.INHERIT, value = "Processing error in module '%s'")
    void processingError(@Cause Throwable cause, String moduleName);

    /**
     * Logs an error message indicating a processing error.
     *
     * @param on      the object the error occurred on
     * @param message the error message
     */
    @LogMessage(level = Level.ERROR)
    @Message(id = 203, value = "Processing error on '%s' with error '%s'")
    void processingError(Object on, String message);
}
