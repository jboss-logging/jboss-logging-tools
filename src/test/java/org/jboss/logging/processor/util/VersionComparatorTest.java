package org.jboss.logging.processor.util;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Date: 09.11.2011
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class VersionComparatorTest {

    @Test
    public void testComparator() {
        final String version = "3.1";
        Assert.assertTrue(VersionComparator.compareVersion("3.1.1", version) > 0);
        Assert.assertTrue(VersionComparator.compareVersion("3.1", version) == 0);
        Assert.assertTrue(VersionComparator.compareVersion("3.0", version) < 0);
        Assert.assertTrue(VersionComparator.compareVersion("3.0.1", version) < 0);
        Assert.assertTrue(VersionComparator.compareVersion("3.0.1", version) < 0);
        Assert.assertTrue(VersionComparator.compareVersion("3.1.x", version) == 0);
    }
}
