package org.jboss.logging.translation.test;

import org.jboss.logging.Message;
import org.jboss.logging.MessageBundle;

/**
 * @author Kevin Pollet
 */
@MessageBundle
public interface TrainMessages {

    @Message("There is no diesel trains due to %s")
    String noDieselTrains(String cause);


}
