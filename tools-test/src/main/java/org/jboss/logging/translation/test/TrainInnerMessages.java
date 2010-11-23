package org.jboss.logging.translation.test;

import org.jboss.logging.Message;
import org.jboss.logging.MessageBundle;

/**
 * @author Kevin Pollet
 */
public class TrainInnerMessages {

    @MessageBundle
    interface InnerMessages {

        @Message("There is no diesel trains due to %s")
        String noDieselTrains(String cause);
    }
}
