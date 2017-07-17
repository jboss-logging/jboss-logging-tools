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

import org.junit.Assert;
import org.junit.Test;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class TransformTest extends AbstractLoggerTest {

    @Test
    public void testLog() throws Exception {

        // Log strings
        final String s = "This is a test string";
        TransformLogger.LOGGER.logClassHashCode(s);
        Assert.assertEquals(String.format(TransformLogger.HASH_CODE_MSG, s.getClass().hashCode()), HANDLER.getMessage());
        TransformLogger.LOGGER.logClassIdentityHashCode(s);
        Assert.assertEquals(String.format(TransformLogger.IDENTITY_HASH_CODE_MSG, System.identityHashCode(s.getClass())), HANDLER.getMessage());
        TransformLogger.LOGGER.logObjectClass(s);
        Assert.assertEquals(String.format(TransformLogger.GET_CLASS_MSG, s.getClass()), HANDLER.getMessage());
        TransformLogger.LOGGER.logObjectHashCode(s);
        Assert.assertEquals(String.format(TransformLogger.HASH_CODE_MSG, s.hashCode()), HANDLER.getMessage());
        TransformLogger.LOGGER.logObjectIdentityHashCode(s);
        Assert.assertEquals(String.format(TransformLogger.IDENTITY_HASH_CODE_MSG, System.identityHashCode(s)), HANDLER.getMessage());
        TransformLogger.LOGGER.logSize(s);
        Assert.assertEquals(String.format(TransformLogger.SIZE_MSG, s.length()), HANDLER.getMessage());

        // Log collections
        final Collection<String> c = Arrays.asList("test");
        TransformLogger.LOGGER.logClassHashCode(c);
        Assert.assertEquals(String.format(TransformLogger.HASH_CODE_MSG, c.getClass().hashCode()), HANDLER.getMessage());
        TransformLogger.LOGGER.logClassIdentityHashCode(c);
        Assert.assertEquals(String.format(TransformLogger.IDENTITY_HASH_CODE_MSG, System.identityHashCode(c.getClass())), HANDLER.getMessage());
        TransformLogger.LOGGER.logObjectHashCode(c);
        Assert.assertEquals(String.format(TransformLogger.HASH_CODE_MSG, c.hashCode()), HANDLER.getMessage());
        TransformLogger.LOGGER.logObjectIdentityHashCode(c);
        Assert.assertEquals(String.format(TransformLogger.IDENTITY_HASH_CODE_MSG, System.identityHashCode(c)), HANDLER.getMessage());
        TransformLogger.LOGGER.logSize(c);
        Assert.assertEquals(String.format(TransformLogger.SIZE_MSG, c.size()), HANDLER.getMessage());

        // Log an array
        final Object[] array = {"test1", "test2", "test3"};
        TransformLogger.LOGGER.logClassHashCode(array);
        Assert.assertEquals(String.format(TransformLogger.HASH_CODE_MSG, array.getClass().hashCode()), HANDLER.getMessage());
        TransformLogger.LOGGER.logClassIdentityHashCode(array);
        Assert.assertEquals(String.format(TransformLogger.IDENTITY_HASH_CODE_MSG, System.identityHashCode(array.getClass())), HANDLER.getMessage());
        TransformLogger.LOGGER.logObjectClass(array);
        Assert.assertEquals(String.format(TransformLogger.GET_CLASS_MSG, array.getClass()), HANDLER.getMessage());
        TransformLogger.LOGGER.logObjectHashCode(array);
        Assert.assertEquals(String.format(TransformLogger.HASH_CODE_MSG, Arrays.hashCode(array)), HANDLER.getMessage());
        TransformLogger.LOGGER.logObjectIdentityHashCode(array);
        Assert.assertEquals(String.format(TransformLogger.IDENTITY_HASH_CODE_MSG, System.identityHashCode(array)), HANDLER.getMessage());
        TransformLogger.LOGGER.logSize(array);
        Assert.assertEquals(String.format(TransformLogger.SIZE_MSG, array.length), HANDLER.getMessage());

        // Log vararg array
        final String[] sArray = {"test1", "test2", "test3"};
        TransformLogger.LOGGER.logClassHashCode(sArray);
        Assert.assertEquals(String.format(TransformLogger.HASH_CODE_MSG, sArray.getClass().hashCode()), HANDLER.getMessage());
        TransformLogger.LOGGER.logClassIdentityHashCode(sArray);
        Assert.assertEquals(String.format(TransformLogger.IDENTITY_HASH_CODE_MSG, System.identityHashCode(sArray.getClass())), HANDLER.getMessage());
        TransformLogger.LOGGER.logObjectClass(sArray);
        Assert.assertEquals(String.format(TransformLogger.GET_CLASS_MSG, sArray.getClass()), HANDLER.getMessage());
        TransformLogger.LOGGER.logObjectHashCode(sArray);
        Assert.assertEquals(String.format(TransformLogger.HASH_CODE_MSG, Arrays.hashCode(sArray)), HANDLER.getMessage());
        TransformLogger.LOGGER.logObjectIdentityHashCode(sArray);
        Assert.assertEquals(String.format(TransformLogger.IDENTITY_HASH_CODE_MSG, System.identityHashCode(sArray)), HANDLER.getMessage());
        TransformLogger.LOGGER.logSize(sArray);
        Assert.assertEquals(String.format(TransformLogger.SIZE_MSG, sArray.length), HANDLER.getMessage());

        // Log a map
        final Map<String, String> map = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            map.put("key" + i, "value" + i);
        }
        TransformLogger.LOGGER.logClassHashCode(map);
        Assert.assertEquals(String.format(TransformLogger.HASH_CODE_MSG, map.getClass().hashCode()), HANDLER.getMessage());
        TransformLogger.LOGGER.logClassIdentityHashCode(map);
        Assert.assertEquals(String.format(TransformLogger.IDENTITY_HASH_CODE_MSG, System.identityHashCode(map.getClass())), HANDLER.getMessage());
        TransformLogger.LOGGER.logObjectHashCode(map);
        Assert.assertEquals(String.format(TransformLogger.HASH_CODE_MSG, map.hashCode()), HANDLER.getMessage());
        TransformLogger.LOGGER.logObjectIdentityHashCode(map);
        Assert.assertEquals(String.format(TransformLogger.IDENTITY_HASH_CODE_MSG, System.identityHashCode(map)), HANDLER.getMessage());
        TransformLogger.LOGGER.logSize(map);
        Assert.assertEquals(String.format(TransformLogger.SIZE_MSG, map.size()), HANDLER.getMessage());
    }

    @Test
    public void testMessage() throws Exception {
        // Log strings
        final String s = "This is a test string";
        Assert.assertEquals(String.format(TransformLogger.HASH_CODE_MSG, s.getClass().hashCode()), TransformMessages.MESSAGES.msgClassHashCode(s));
        Assert.assertEquals(String.format(TransformLogger.IDENTITY_HASH_CODE_MSG, System.identityHashCode(s.getClass())), TransformMessages.MESSAGES.msgClassIdentityHashCode(s));
        Assert.assertEquals(String.format(TransformLogger.GET_CLASS_MSG, s.getClass()), TransformMessages.MESSAGES.msgObjectClass(s));
        Assert.assertEquals(String.format(TransformLogger.HASH_CODE_MSG, s.hashCode()), TransformMessages.MESSAGES.msgObjectHashCode(s));
        Assert.assertEquals(String.format(TransformLogger.IDENTITY_HASH_CODE_MSG, System.identityHashCode(s)), TransformMessages.MESSAGES.msgObjectIdentityHashCode(s));
        Assert.assertEquals(String.format(TransformLogger.SIZE_MSG, s.length()), TransformMessages.MESSAGES.msgSize(s));

        // Log collections
        final Collection<String> c = Arrays.asList("test");
        Assert.assertEquals(String.format(TransformLogger.HASH_CODE_MSG, c.getClass().hashCode()), TransformMessages.MESSAGES.msgClassHashCode(c));
        Assert.assertEquals(String.format(TransformLogger.IDENTITY_HASH_CODE_MSG, System.identityHashCode(c.getClass())), TransformMessages.MESSAGES.msgClassIdentityHashCode(c));
        Assert.assertEquals(String.format(TransformLogger.HASH_CODE_MSG, c.hashCode()), TransformMessages.MESSAGES.msgObjectHashCode(c));
        Assert.assertEquals(String.format(TransformLogger.IDENTITY_HASH_CODE_MSG, System.identityHashCode(c)), TransformMessages.MESSAGES.msgObjectIdentityHashCode(c));
        Assert.assertEquals(String.format(TransformLogger.SIZE_MSG, c.size()), TransformMessages.MESSAGES.msgSize(c));

        // Log an array
        final Object[] array = {"test1", "test2", "test3"};
        Assert.assertEquals(String.format(TransformLogger.HASH_CODE_MSG, array.getClass().hashCode()), TransformMessages.MESSAGES.msgClassHashCode(array));
        Assert.assertEquals(String.format(TransformLogger.IDENTITY_HASH_CODE_MSG, System.identityHashCode(array.getClass())), TransformMessages.MESSAGES.msgClassIdentityHashCode(array));
        Assert.assertEquals(String.format(TransformLogger.GET_CLASS_MSG, array.getClass()), TransformMessages.MESSAGES.msgObjectClass(array));
        Assert.assertEquals(String.format(TransformLogger.HASH_CODE_MSG, Arrays.hashCode(array)), TransformMessages.MESSAGES.msgObjectHashCode(array));
        Assert.assertEquals(String.format(TransformLogger.IDENTITY_HASH_CODE_MSG, System.identityHashCode(array)), TransformMessages.MESSAGES.msgObjectIdentityHashCode(array));
        Assert.assertEquals(String.format(TransformLogger.SIZE_MSG, array.length), TransformMessages.MESSAGES.msgSize(array));

        // Log vararg array
        final String[] sArray = {"test1", "test2", "test3"};
        Assert.assertEquals(String.format(TransformLogger.HASH_CODE_MSG, sArray.getClass().hashCode()), TransformMessages.MESSAGES.msgClassHashCode(sArray));
        Assert.assertEquals(String.format(TransformLogger.IDENTITY_HASH_CODE_MSG, System.identityHashCode(sArray.getClass())), TransformMessages.MESSAGES.msgClassIdentityHashCode(sArray));
        Assert.assertEquals(String.format(TransformLogger.GET_CLASS_MSG, sArray.getClass()), TransformMessages.MESSAGES.msgObjectClass(sArray));
        Assert.assertEquals(String.format(TransformLogger.HASH_CODE_MSG, Arrays.hashCode(sArray)), TransformMessages.MESSAGES.msgObjectHashCode(sArray));
        Assert.assertEquals(String.format(TransformLogger.IDENTITY_HASH_CODE_MSG, System.identityHashCode(sArray)), TransformMessages.MESSAGES.msgObjectIdentityHashCode(sArray));
        Assert.assertEquals(String.format(TransformLogger.SIZE_MSG, sArray.length), TransformMessages.MESSAGES.msgSize(sArray));

        // Log a map
        final Map<String, String> map = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            map.put("key" + i, "value" + i);
        }
        Assert.assertEquals(String.format(TransformLogger.HASH_CODE_MSG, map.getClass().hashCode()), TransformMessages.MESSAGES.msgClassHashCode(map));
        Assert.assertEquals(String.format(TransformLogger.IDENTITY_HASH_CODE_MSG, System.identityHashCode(map.getClass())), TransformMessages.MESSAGES.msgClassIdentityHashCode(map));
        Assert.assertEquals(String.format(TransformLogger.HASH_CODE_MSG, map.hashCode()), TransformMessages.MESSAGES.msgObjectHashCode(map));
        Assert.assertEquals(String.format(TransformLogger.IDENTITY_HASH_CODE_MSG, System.identityHashCode(map)), TransformMessages.MESSAGES.msgObjectIdentityHashCode(map));
        Assert.assertEquals(String.format(TransformLogger.SIZE_MSG, map.size()), TransformMessages.MESSAGES.msgSize(map));
    }

    @Test
    public void testPositions() throws Exception {

        // Log strings
        final String msg1 = "Test message 1";
        final String msg2 = "Test message 2";
        String expected = String.format(TransformLogger.POS_MSG_1, msg2.length(), msg1.hashCode(), System.identityHashCode(msg1));
        TransformLogger.LOGGER.posTest1(msg1, msg2);
        Assert.assertEquals(expected, HANDLER.getMessage());
        Assert.assertEquals(expected, TransformMessages.MESSAGES.posTest1(msg1, msg2));

        final Object obj = "Test";
        final String msg = "This is a test message";
        final String s1 = "s1";
        final String s2 = "s2";
        expected = String.format(TransformLogger.POS_MSG_2, msg.length(), s1, s2, obj.getClass());
        TransformLogger.LOGGER.posTest2(obj, msg, s1, s2);
        Assert.assertEquals(expected, HANDLER.getMessage());
        Assert.assertEquals(expected, TransformMessages.MESSAGES.posTest2(obj, msg, s1, s2));
    }
}
