/**
 * 
 */
package org.jboss.logging.test;

import org.jboss.logging.Cause;
import org.jboss.logging.LogMessage;
import org.jboss.logging.Logger.Level;
import org.jboss.logging.Message;
import org.jboss.logging.Message.Format;
import org.jboss.logging.MessageBundle;

import java.io.IOException;

/**
 * @author James R. Perkins Jr. (jrp)
 * 
 */
@MessageBundle(projectCode = "LOGB")
public interface TestBundle {
    @LogMessage(level = Level.ERROR)
    @Message("%s cannot be null.")
    String valueNotNull(String value);

    @LogMessage(level = Level.ERROR)
    @Message(id = 1, value = "Version: %s")
    String version(String version);

    @LogMessage(level = Level.FATAL)
    @Message(id = 2, value = "This is not good %s is melting.")
    RuntimeException meltDown(@Cause IOException cause, String value);

    @LogMessage(level = Level.WARN)
    @Message(id = 3, value = "Value {0} could not be added to {1}", format = Format.MESSAGE_FORMAT)
    String invalidValue(@Cause Throwable cause);
}
