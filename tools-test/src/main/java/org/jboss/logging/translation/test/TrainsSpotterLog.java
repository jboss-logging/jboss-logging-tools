package org.jboss.logging.translation.test;

import org.jboss.logging.LogMessage;
import org.jboss.logging.Message;
import org.jboss.logging.MessageLogger;

/**
 * @author Kevin Pollet
 */
@MessageLogger(projectCode = "TPS")
public interface TrainsSpotterLog {

    @LogMessage
    @Message(id = 1, value = "There is %s diesel trains")
    void nbDieselTrains(int number);
}
