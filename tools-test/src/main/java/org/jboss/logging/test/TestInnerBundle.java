/**
 * 
 */
package org.jboss.logging.test;


import org.jboss.logging.Cause;
import org.jboss.logging.Message;
import org.jboss.logging.Message.Format;
import org.jboss.logging.MessageBundle;

/**
 * @author James R. Perkins Jr. (jrp)
 * 
 */
public class TestInnerBundle {

    @MessageBundle
    private interface InnerBundle {

        @Message(id = 20, value = "ERROR: {0} Root cause: {1}",
                 format = Format.MESSAGE_FORMAT)
        String errorMessage(@Cause Throwable cause, String message);
    }
}
