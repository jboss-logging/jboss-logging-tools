package org.jboss.logging.translation.test;

import org.jboss.logging.LogMessage;
import org.jboss.logging.Message;
import org.jboss.logging.MessageLogger;

/**
 * @author Kevin Pollet
 */
@MessageLogger
public interface TrainsSpotterLog {

    @LogMessage
    @Message("There is %s diesel trains")
    void nbDieselTrains(int number);

}
