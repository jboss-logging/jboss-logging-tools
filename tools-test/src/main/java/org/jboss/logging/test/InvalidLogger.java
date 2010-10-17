/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.jboss.logging.test;

import org.jboss.logging.Message;
import org.jboss.logging.MessageLogger;

/**
 *
 * @author jrp
 */
@MessageLogger
public class InvalidLogger {

    @Message("Error message")
    public void printError() {
        
    }

}
