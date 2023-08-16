/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2023 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.logging.processor.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Date: 09.11.2011
 *
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class VersionComparatorTest {

    @Test
    public void testComparator() {
        final String version = "3.1";
        Assertions.assertTrue(VersionComparator.compareVersion("3.1.1", version) > 0);
        Assertions.assertEquals(0, VersionComparator.compareVersion("3.1", version));
        Assertions.assertTrue(VersionComparator.compareVersion("3.0", version) < 0);
        Assertions.assertTrue(VersionComparator.compareVersion("3.0.1", version) < 0);
        Assertions.assertTrue(VersionComparator.compareVersion("3.0.1", version) < 0);
        Assertions.assertEquals(0, VersionComparator.compareVersion("3.1.x", version));
    }
}
