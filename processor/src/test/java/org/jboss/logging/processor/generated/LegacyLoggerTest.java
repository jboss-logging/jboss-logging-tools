package org.jboss.logging.processor.generated;

import java.io.BufferedOutputStream;

import org.testng.annotations.Test;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class LegacyLoggerTest extends AbstractLoggerTest {

    @Test
    public void logEventsTest() throws Exception {
        LegacyLogger.LOGGER.formatWith("This is a test message");
        LegacyLogger.LOGGER.noFormat();
    }
}
