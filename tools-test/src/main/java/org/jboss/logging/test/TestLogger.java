/**
 * 
 */
package org.jboss.logging.test;

import org.jboss.logging.Cause;
import org.jboss.logging.LogMessage;
import org.jboss.logging.Logger.Level;
import org.jboss.logging.Message;
import org.jboss.logging.Message.Format;
import org.jboss.logging.MessageLogger;

/**
 * @author James R. Perkins Jr. (jrp)
 * 
 */
@MessageLogger(projectCode = "LOGL")
public interface TestLogger {

    String valueNotNull(Object value);

    @LogMessage(level = Level.WARN)
    @Message(id = 1, value = "%s cannot be null.")
    void valueNotNull(String value);

    @LogMessage(level = Level.ERROR)
    void valueNotNull(@Cause IllegalArgumentException cause, Object value);

    @LogMessage(level = Level.INFO)
    @Message(id = 2, value = "Version: %s")
    void version(String version);

    @LogMessage(level = Level.FATAL)
    @Message(id = 3, value = "This is not good %s is melting.")
    void meltDown(@Cause Throwable cause, String value);

    @Message(id = 4, value = "Value {0} could not be added to {1}",
             format = Format.MESSAGE_FORMAT)
    String invalidValue(@Cause Throwable cause, String value, String collection);
}
