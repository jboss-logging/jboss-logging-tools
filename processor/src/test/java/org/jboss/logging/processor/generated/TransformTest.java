/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2016, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.logging.processor.generated;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class TransformTest extends AbstractLoggerTest {
    int pos = 0;

    @Test
    public void testLog() throws Exception {

        // Log strings
        final String s = "This is a test string";
        TransformLogger.LOGGER.logClassHashCode(s);
        Assert.assertEquals(HANDLER.getMessage(pos++), String.format(TransformLogger.HASH_CODE_MSG, s.getClass().hashCode()));
        TransformLogger.LOGGER.logClassIdentityHashCode(s);
        Assert.assertEquals(HANDLER.getMessage(pos++), String.format(TransformLogger.IDENTITY_HASH_CODE_MSG, System.identityHashCode(s.getClass())));
        TransformLogger.LOGGER.logObjectClass(s);
        Assert.assertEquals(HANDLER.getMessage(pos++), String.format(TransformLogger.GET_CLASS_MSG, s.getClass()));
        TransformLogger.LOGGER.logObjectHashCode(s);
        Assert.assertEquals(HANDLER.getMessage(pos++), String.format(TransformLogger.HASH_CODE_MSG, s.hashCode()));
        TransformLogger.LOGGER.logObjectIdentityHashCode(s);
        Assert.assertEquals(HANDLER.getMessage(pos++), String.format(TransformLogger.IDENTITY_HASH_CODE_MSG, System.identityHashCode(s)));
        TransformLogger.LOGGER.logSize(s);
        Assert.assertEquals(HANDLER.getMessage(pos++), String.format(TransformLogger.SIZE_MSG, s.length()));

        // Log collections
        final Collection<String> c = Arrays.asList("test");
        TransformLogger.LOGGER.logClassHashCode(c);
        Assert.assertEquals(HANDLER.getMessage(pos++), String.format(TransformLogger.HASH_CODE_MSG, c.getClass().hashCode()));
        TransformLogger.LOGGER.logClassIdentityHashCode(c);
        Assert.assertEquals(HANDLER.getMessage(pos++), String.format(TransformLogger.IDENTITY_HASH_CODE_MSG, System.identityHashCode(c.getClass())));
        TransformLogger.LOGGER.logObjectHashCode(c);
        Assert.assertEquals(HANDLER.getMessage(pos++), String.format(TransformLogger.HASH_CODE_MSG, c.hashCode()));
        TransformLogger.LOGGER.logObjectIdentityHashCode(c);
        Assert.assertEquals(HANDLER.getMessage(pos++), String.format(TransformLogger.IDENTITY_HASH_CODE_MSG, System.identityHashCode(c)));
        TransformLogger.LOGGER.logSize(c);
        Assert.assertEquals(HANDLER.getMessage(pos++), String.format(TransformLogger.SIZE_MSG, c.size()));

        // Log an array
        final Object[] array = {"test1", "test2", "test3"};
        TransformLogger.LOGGER.logClassHashCode(array);
        Assert.assertEquals(HANDLER.getMessage(pos++), String.format(TransformLogger.HASH_CODE_MSG, array.getClass().hashCode()));
        TransformLogger.LOGGER.logClassIdentityHashCode(array);
        Assert.assertEquals(HANDLER.getMessage(pos++), String.format(TransformLogger.IDENTITY_HASH_CODE_MSG, System.identityHashCode(array.getClass())));
        TransformLogger.LOGGER.logObjectClass(array);
        Assert.assertEquals(HANDLER.getMessage(pos++), String.format(TransformLogger.GET_CLASS_MSG, array.getClass()));
        TransformLogger.LOGGER.logObjectHashCode(array);
        Assert.assertEquals(HANDLER.getMessage(pos++), String.format(TransformLogger.HASH_CODE_MSG, Arrays.hashCode(array)));
        TransformLogger.LOGGER.logObjectIdentityHashCode(array);
        Assert.assertEquals(HANDLER.getMessage(pos++), String.format(TransformLogger.IDENTITY_HASH_CODE_MSG, System.identityHashCode(array)));
        TransformLogger.LOGGER.logSize(array);
        Assert.assertEquals(HANDLER.getMessage(pos++), String.format(TransformLogger.SIZE_MSG, array.length));

        // Log vararg array
        final String[] sArray = {"test1", "test2", "test3"};
        TransformLogger.LOGGER.logClassHashCode(sArray);
        Assert.assertEquals(HANDLER.getMessage(pos++), String.format(TransformLogger.HASH_CODE_MSG, sArray.getClass().hashCode()));
        TransformLogger.LOGGER.logClassIdentityHashCode(sArray);
        Assert.assertEquals(HANDLER.getMessage(pos++), String.format(TransformLogger.IDENTITY_HASH_CODE_MSG, System.identityHashCode(sArray.getClass())));
        TransformLogger.LOGGER.logObjectClass(sArray);
        Assert.assertEquals(HANDLER.getMessage(pos++), String.format(TransformLogger.GET_CLASS_MSG, sArray.getClass()));
        TransformLogger.LOGGER.logObjectHashCode(sArray);
        Assert.assertEquals(HANDLER.getMessage(pos++), String.format(TransformLogger.HASH_CODE_MSG, Arrays.hashCode(sArray)));
        TransformLogger.LOGGER.logObjectIdentityHashCode(sArray);
        Assert.assertEquals(HANDLER.getMessage(pos++), String.format(TransformLogger.IDENTITY_HASH_CODE_MSG, System.identityHashCode(sArray)));
        TransformLogger.LOGGER.logSize(sArray);
        Assert.assertEquals(HANDLER.getMessage(pos++), String.format(TransformLogger.SIZE_MSG, sArray.length));

        // Log a map
        final Map<String, String> map = new HashMap<String, String>();
        for (int i = 0; i < 10; i++) {
            map.put("key" + i, "value" + i);
        }
        TransformLogger.LOGGER.logClassHashCode(map);
        Assert.assertEquals(HANDLER.getMessage(pos++), String.format(TransformLogger.HASH_CODE_MSG, map.getClass().hashCode()));
        TransformLogger.LOGGER.logClassIdentityHashCode(map);
        Assert.assertEquals(HANDLER.getMessage(pos++), String.format(TransformLogger.IDENTITY_HASH_CODE_MSG, System.identityHashCode(map.getClass())));
        TransformLogger.LOGGER.logObjectHashCode(map);
        Assert.assertEquals(HANDLER.getMessage(pos++), String.format(TransformLogger.HASH_CODE_MSG, map.hashCode()));
        TransformLogger.LOGGER.logObjectIdentityHashCode(map);
        Assert.assertEquals(HANDLER.getMessage(pos++), String.format(TransformLogger.IDENTITY_HASH_CODE_MSG, System.identityHashCode(map)));
        TransformLogger.LOGGER.logSize(map);
        Assert.assertEquals(HANDLER.getMessage(pos++), String.format(TransformLogger.SIZE_MSG, map.size()));
    }

    @Test
    public void testMessage() throws Exception {
        // Log strings
        final String s = "This is a test string";
        Assert.assertEquals(TransformMessages.MESSAGES.msgClassHashCode(s), String.format(TransformLogger.HASH_CODE_MSG, s.getClass().hashCode()));
        Assert.assertEquals(TransformMessages.MESSAGES.msgClassIdentityHashCode(s), String.format(TransformLogger.IDENTITY_HASH_CODE_MSG, System.identityHashCode(s.getClass())));
        Assert.assertEquals(TransformMessages.MESSAGES.msgObjectClass(s), String.format(TransformLogger.GET_CLASS_MSG, s.getClass()));
        Assert.assertEquals(TransformMessages.MESSAGES.msgObjectHashCode(s), String.format(TransformLogger.HASH_CODE_MSG, s.hashCode()));
        Assert.assertEquals(TransformMessages.MESSAGES.msgObjectIdentityHashCode(s), String.format(TransformLogger.IDENTITY_HASH_CODE_MSG, System.identityHashCode(s)));
        Assert.assertEquals(TransformMessages.MESSAGES.msgSize(s), String.format(TransformLogger.SIZE_MSG, s.length()));

        // Log collections
        final Collection<String> c = Arrays.asList("test");
        Assert.assertEquals(TransformMessages.MESSAGES.msgClassHashCode(c), String.format(TransformLogger.HASH_CODE_MSG, c.getClass().hashCode()));
        Assert.assertEquals(TransformMessages.MESSAGES.msgClassIdentityHashCode(c), String.format(TransformLogger.IDENTITY_HASH_CODE_MSG, System.identityHashCode(c.getClass())));
        Assert.assertEquals(TransformMessages.MESSAGES.msgObjectHashCode(c), String.format(TransformLogger.HASH_CODE_MSG, c.hashCode()));
        Assert.assertEquals(TransformMessages.MESSAGES.msgObjectIdentityHashCode(c), String.format(TransformLogger.IDENTITY_HASH_CODE_MSG, System.identityHashCode(c)));
        Assert.assertEquals(TransformMessages.MESSAGES.msgSize(c), String.format(TransformLogger.SIZE_MSG, c.size()));

        // Log an array
        final Object[] array = {"test1", "test2", "test3"};
        Assert.assertEquals(TransformMessages.MESSAGES.msgClassHashCode(array), String.format(TransformLogger.HASH_CODE_MSG, array.getClass().hashCode()));
        Assert.assertEquals(TransformMessages.MESSAGES.msgClassIdentityHashCode(array), String.format(TransformLogger.IDENTITY_HASH_CODE_MSG, System.identityHashCode(array.getClass())));
        Assert.assertEquals(TransformMessages.MESSAGES.msgObjectClass(array), String.format(TransformLogger.GET_CLASS_MSG, array.getClass()));
        Assert.assertEquals(TransformMessages.MESSAGES.msgObjectHashCode(array), String.format(TransformLogger.HASH_CODE_MSG, Arrays.hashCode(array)));
        Assert.assertEquals(TransformMessages.MESSAGES.msgObjectIdentityHashCode(array), String.format(TransformLogger.IDENTITY_HASH_CODE_MSG, System.identityHashCode(array)));
        Assert.assertEquals(TransformMessages.MESSAGES.msgSize(array), String.format(TransformLogger.SIZE_MSG, array.length));

        // Log vararg array
        final String[] sArray = {"test1", "test2", "test3"};
        Assert.assertEquals(TransformMessages.MESSAGES.msgClassHashCode(sArray), String.format(TransformLogger.HASH_CODE_MSG, sArray.getClass().hashCode()));
        Assert.assertEquals(TransformMessages.MESSAGES.msgClassIdentityHashCode(sArray), String.format(TransformLogger.IDENTITY_HASH_CODE_MSG, System.identityHashCode(sArray.getClass())));
        Assert.assertEquals(TransformMessages.MESSAGES.msgObjectClass(sArray), String.format(TransformLogger.GET_CLASS_MSG, sArray.getClass()));
        Assert.assertEquals(TransformMessages.MESSAGES.msgObjectHashCode(sArray), String.format(TransformLogger.HASH_CODE_MSG, Arrays.hashCode(sArray)));
        Assert.assertEquals(TransformMessages.MESSAGES.msgObjectIdentityHashCode(sArray), String.format(TransformLogger.IDENTITY_HASH_CODE_MSG, System.identityHashCode(sArray)));
        Assert.assertEquals(TransformMessages.MESSAGES.msgSize(sArray), String.format(TransformLogger.SIZE_MSG, sArray.length));

        // Log a map
        final Map<String, String> map = new HashMap<String, String>();
        for (int i = 0; i < 10; i++) {
            map.put("key" + i, "value" + i);
        }
        Assert.assertEquals(TransformMessages.MESSAGES.msgClassHashCode(map), String.format(TransformLogger.HASH_CODE_MSG, map.getClass().hashCode()));
        Assert.assertEquals(TransformMessages.MESSAGES.msgClassIdentityHashCode(map), String.format(TransformLogger.IDENTITY_HASH_CODE_MSG, System.identityHashCode(map.getClass())));
        Assert.assertEquals(TransformMessages.MESSAGES.msgObjectHashCode(map), String.format(TransformLogger.HASH_CODE_MSG, map.hashCode()));
        Assert.assertEquals(TransformMessages.MESSAGES.msgObjectIdentityHashCode(map), String.format(TransformLogger.IDENTITY_HASH_CODE_MSG, System.identityHashCode(map)));
        Assert.assertEquals(TransformMessages.MESSAGES.msgSize(map), String.format(TransformLogger.SIZE_MSG, map.size()));
    }

    @Test
    public void testPositions() throws Exception {

        // Log strings
        final String msg1 = "Test message 1";
        final String msg2 = "Test message 2";
        String expected = String.format(TransformLogger.POS_MSG_1, msg2.length(), msg1.hashCode(), System.identityHashCode(msg1));
        TransformLogger.LOGGER.posTest1(msg1, msg2);
        Assert.assertEquals(HANDLER.getMessage(pos++), expected);
        Assert.assertEquals(TransformMessages.MESSAGES.posTest1(msg1, msg2), expected);

        final Object obj = "Test";
        final String msg = "This is a test message";
        final String s1 = "s1";
        final String s2 = "s2";
        expected = String.format(TransformLogger.POS_MSG_2, msg.length(), s1, s2, obj.getClass());
        TransformLogger.LOGGER.posTest2(obj, msg, s1, s2);
        Assert.assertEquals(HANDLER.getMessage(pos++), expected);
        Assert.assertEquals(TransformMessages.MESSAGES.posTest2(obj, msg, s1, s2), expected);
    }
}
