/**
 * 
 */
package org.jboss.logging.test;

import org.jboss.logging.LogMessage;
import org.jboss.logging.Logger.Level;
import org.jboss.logging.Message;
import org.jboss.logging.MessageLogger;

/**
 * @author James R. Perkins Jr. (jrp)
 * 
 */
@MessageLogger(projectCode = "LOGEL")
public interface TestExtendedLogger extends TestLogger {
    @LogMessage(level = Level.ERROR)
    @Message(id = Message.INHERIT, value = "%s.")
    void extendedError(String value);
}
