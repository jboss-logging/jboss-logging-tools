/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2021, Red Hat, Inc., and individual contributors
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

package org.jboss.logging.processor.generated.tests;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jboss.logging.processor.generated.TransformLogger;
import org.jboss.logging.processor.generated.TransformMessages;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class TransformTest extends AbstractLoggerTest {

    @Test
    public void testLog() throws Exception {

        // Log strings
        final String s = "This is a test string";
        TransformLogger.LOGGER.logClassHashCode(s);
        Assertions.assertEquals(String.format(TransformLogger.HASH_CODE_MSG, s.getClass().hashCode()), HANDLER.getMessage());
        TransformLogger.LOGGER.logClassIdentityHashCode(s);
        Assertions.assertEquals(String.format(TransformLogger.IDENTITY_HASH_CODE_MSG, System.identityHashCode(s.getClass())), HANDLER.getMessage());
        TransformLogger.LOGGER.logObjectClass(s);
        Assertions.assertEquals(String.format(TransformLogger.GET_CLASS_MSG, s.getClass()), HANDLER.getMessage());
        TransformLogger.LOGGER.logObjectHashCode(s);
        Assertions.assertEquals(String.format(TransformLogger.HASH_CODE_MSG, s.hashCode()), HANDLER.getMessage());
        TransformLogger.LOGGER.logObjectIdentityHashCode(s);
        Assertions.assertEquals(String.format(TransformLogger.IDENTITY_HASH_CODE_MSG, System.identityHashCode(s)), HANDLER.getMessage());
        TransformLogger.LOGGER.logSize(s);
        Assertions.assertEquals(String.format(TransformLogger.SIZE_MSG, s.length()), HANDLER.getMessage());

        // Log collections
        final Collection<String> c = Collections.singletonList("test");
        TransformLogger.LOGGER.logClassHashCode(c);
        Assertions.assertEquals(String.format(TransformLogger.HASH_CODE_MSG, c.getClass().hashCode()), HANDLER.getMessage());
        TransformLogger.LOGGER.logClassIdentityHashCode(c);
        Assertions.assertEquals(String.format(TransformLogger.IDENTITY_HASH_CODE_MSG, System.identityHashCode(c.getClass())), HANDLER.getMessage());
        TransformLogger.LOGGER.logObjectHashCode(c);
        Assertions.assertEquals(String.format(TransformLogger.HASH_CODE_MSG, c.hashCode()), HANDLER.getMessage());
        TransformLogger.LOGGER.logObjectIdentityHashCode(c);
        Assertions.assertEquals(String.format(TransformLogger.IDENTITY_HASH_CODE_MSG, System.identityHashCode(c)), HANDLER.getMessage());
        TransformLogger.LOGGER.logSize(c);
        Assertions.assertEquals(String.format(TransformLogger.SIZE_MSG, c.size()), HANDLER.getMessage());

        // Log an array
        final Object[] array = {"test1", "test2", "test3"};
        TransformLogger.LOGGER.logClassHashCode(array);
        Assertions.assertEquals(String.format(TransformLogger.HASH_CODE_MSG, array.getClass().hashCode()), HANDLER.getMessage());
        TransformLogger.LOGGER.logClassIdentityHashCode(array);
        Assertions.assertEquals(String.format(TransformLogger.IDENTITY_HASH_CODE_MSG, System.identityHashCode(array.getClass())), HANDLER.getMessage());
        TransformLogger.LOGGER.logObjectClass(array);
        Assertions.assertEquals(String.format(TransformLogger.GET_CLASS_MSG, array.getClass()), HANDLER.getMessage());
        TransformLogger.LOGGER.logObjectHashCode(array);
        Assertions.assertEquals(String.format(TransformLogger.HASH_CODE_MSG, Arrays.hashCode(array)), HANDLER.getMessage());
        TransformLogger.LOGGER.logObjectIdentityHashCode(array);
        Assertions.assertEquals(String.format(TransformLogger.IDENTITY_HASH_CODE_MSG, System.identityHashCode(array)), HANDLER.getMessage());
        TransformLogger.LOGGER.logSize(array);
        Assertions.assertEquals(String.format(TransformLogger.SIZE_MSG, array.length), HANDLER.getMessage());

        // Log vararg array
        final String[] sArray = {"test1", "test2", "test3"};
        TransformLogger.LOGGER.logClassHashCode(sArray);
        Assertions.assertEquals(String.format(TransformLogger.HASH_CODE_MSG, sArray.getClass().hashCode()), HANDLER.getMessage());
        TransformLogger.LOGGER.logClassIdentityHashCode(sArray);
        Assertions.assertEquals(String.format(TransformLogger.IDENTITY_HASH_CODE_MSG, System.identityHashCode(sArray.getClass())), HANDLER.getMessage());
        TransformLogger.LOGGER.logObjectClass(sArray);
        Assertions.assertEquals(String.format(TransformLogger.GET_CLASS_MSG, sArray.getClass()), HANDLER.getMessage());
        TransformLogger.LOGGER.logObjectHashCode(sArray);
        Assertions.assertEquals(String.format(TransformLogger.HASH_CODE_MSG, Arrays.hashCode(sArray)), HANDLER.getMessage());
        TransformLogger.LOGGER.logObjectIdentityHashCode(sArray);
        Assertions.assertEquals(String.format(TransformLogger.IDENTITY_HASH_CODE_MSG, System.identityHashCode(sArray)), HANDLER.getMessage());
        TransformLogger.LOGGER.logSize(sArray);
        Assertions.assertEquals(String.format(TransformLogger.SIZE_MSG, sArray.length), HANDLER.getMessage());

        // Log a map
        final Map<String, String> map = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            map.put("key" + i, "value" + i);
        }
        TransformLogger.LOGGER.logClassHashCode(map);
        Assertions.assertEquals(String.format(TransformLogger.HASH_CODE_MSG, map.getClass().hashCode()), HANDLER.getMessage());
        TransformLogger.LOGGER.logClassIdentityHashCode(map);
        Assertions.assertEquals(String.format(TransformLogger.IDENTITY_HASH_CODE_MSG, System.identityHashCode(map.getClass())), HANDLER.getMessage());
        TransformLogger.LOGGER.logObjectHashCode(map);
        Assertions.assertEquals(String.format(TransformLogger.HASH_CODE_MSG, map.hashCode()), HANDLER.getMessage());
        TransformLogger.LOGGER.logObjectIdentityHashCode(map);
        Assertions.assertEquals(String.format(TransformLogger.IDENTITY_HASH_CODE_MSG, System.identityHashCode(map)), HANDLER.getMessage());
        TransformLogger.LOGGER.logSize(map);
        Assertions.assertEquals(String.format(TransformLogger.SIZE_MSG, map.size()), HANDLER.getMessage());
    }

    @Test
    public void testMessage() {
        // Log strings
        final String s = "This is a test string";
        Assertions.assertEquals(String.format(TransformLogger.HASH_CODE_MSG, s.getClass().hashCode()), TransformMessages.MESSAGES.msgClassHashCode(s));
        Assertions.assertEquals(String.format(TransformLogger.IDENTITY_HASH_CODE_MSG, System.identityHashCode(s.getClass())), TransformMessages.MESSAGES.msgClassIdentityHashCode(s));
        Assertions.assertEquals(String.format(TransformLogger.GET_CLASS_MSG, s.getClass()), TransformMessages.MESSAGES.msgObjectClass(s));
        Assertions.assertEquals(String.format(TransformLogger.HASH_CODE_MSG, s.hashCode()), TransformMessages.MESSAGES.msgObjectHashCode(s));
        Assertions.assertEquals(String.format(TransformLogger.IDENTITY_HASH_CODE_MSG, System.identityHashCode(s)), TransformMessages.MESSAGES.msgObjectIdentityHashCode(s));
        Assertions.assertEquals(String.format(TransformLogger.SIZE_MSG, s.length()), TransformMessages.MESSAGES.msgSize(s));

        // Log collections
        final Collection<String> c = Collections.singletonList("test");
        Assertions.assertEquals(String.format(TransformLogger.HASH_CODE_MSG, c.getClass().hashCode()), TransformMessages.MESSAGES.msgClassHashCode(c));
        Assertions.assertEquals(String.format(TransformLogger.IDENTITY_HASH_CODE_MSG, System.identityHashCode(c.getClass())), TransformMessages.MESSAGES.msgClassIdentityHashCode(c));
        Assertions.assertEquals(String.format(TransformLogger.HASH_CODE_MSG, c.hashCode()), TransformMessages.MESSAGES.msgObjectHashCode(c));
        Assertions.assertEquals(String.format(TransformLogger.IDENTITY_HASH_CODE_MSG, System.identityHashCode(c)), TransformMessages.MESSAGES.msgObjectIdentityHashCode(c));
        Assertions.assertEquals(String.format(TransformLogger.SIZE_MSG, c.size()), TransformMessages.MESSAGES.msgSize(c));

        // Log an array
        final Object[] array = {"test1", "test2", "test3"};
        Assertions.assertEquals(String.format(TransformLogger.HASH_CODE_MSG, array.getClass().hashCode()), TransformMessages.MESSAGES.msgClassHashCode(array));
        Assertions.assertEquals(String.format(TransformLogger.IDENTITY_HASH_CODE_MSG, System.identityHashCode(array.getClass())), TransformMessages.MESSAGES.msgClassIdentityHashCode(array));
        Assertions.assertEquals(String.format(TransformLogger.GET_CLASS_MSG, array.getClass()), TransformMessages.MESSAGES.msgObjectClass(array));
        Assertions.assertEquals(String.format(TransformLogger.HASH_CODE_MSG, Arrays.hashCode(array)), TransformMessages.MESSAGES.msgObjectHashCode(array));
        Assertions.assertEquals(String.format(TransformLogger.IDENTITY_HASH_CODE_MSG, System.identityHashCode(array)), TransformMessages.MESSAGES.msgObjectIdentityHashCode(array));
        Assertions.assertEquals(String.format(TransformLogger.SIZE_MSG, array.length), TransformMessages.MESSAGES.msgSize(array));

        // Log vararg array
        final String[] sArray = {"test1", "test2", "test3"};
        Assertions.assertEquals(String.format(TransformLogger.HASH_CODE_MSG, sArray.getClass().hashCode()), TransformMessages.MESSAGES.msgClassHashCode(sArray));
        Assertions.assertEquals(String.format(TransformLogger.IDENTITY_HASH_CODE_MSG, System.identityHashCode(sArray.getClass())), TransformMessages.MESSAGES.msgClassIdentityHashCode(sArray));
        Assertions.assertEquals(String.format(TransformLogger.GET_CLASS_MSG, sArray.getClass()), TransformMessages.MESSAGES.msgObjectClass(sArray));
        Assertions.assertEquals(String.format(TransformLogger.HASH_CODE_MSG, Arrays.hashCode(sArray)), TransformMessages.MESSAGES.msgObjectHashCode(sArray));
        Assertions.assertEquals(String.format(TransformLogger.IDENTITY_HASH_CODE_MSG, System.identityHashCode(sArray)), TransformMessages.MESSAGES.msgObjectIdentityHashCode(sArray));
        Assertions.assertEquals(String.format(TransformLogger.SIZE_MSG, sArray.length), TransformMessages.MESSAGES.msgSize(sArray));

        // Log a map
        final Map<String, String> map = new HashMap<>();
        for (int i = 0; i < 10; i++) {
            map.put("key" + i, "value" + i);
        }
        Assertions.assertEquals(String.format(TransformLogger.HASH_CODE_MSG, map.getClass().hashCode()), TransformMessages.MESSAGES.msgClassHashCode(map));
        Assertions.assertEquals(String.format(TransformLogger.IDENTITY_HASH_CODE_MSG, System.identityHashCode(map.getClass())), TransformMessages.MESSAGES.msgClassIdentityHashCode(map));
        Assertions.assertEquals(String.format(TransformLogger.HASH_CODE_MSG, map.hashCode()), TransformMessages.MESSAGES.msgObjectHashCode(map));
        Assertions.assertEquals(String.format(TransformLogger.IDENTITY_HASH_CODE_MSG, System.identityHashCode(map)), TransformMessages.MESSAGES.msgObjectIdentityHashCode(map));
        Assertions.assertEquals(String.format(TransformLogger.SIZE_MSG, map.size()), TransformMessages.MESSAGES.msgSize(map));
    }

    @Test
    public void testPositions() throws Exception {

        // Log strings
        final String msg1 = "Test message 1";
        final String msg2 = "Test message 2";
        String expected = String.format(TransformLogger.POS_MSG_1, msg2.length(), msg1.hashCode(), System.identityHashCode(msg1));
        TransformLogger.LOGGER.posTest1(msg1, msg2);
        Assertions.assertEquals(expected, HANDLER.getMessage());
        Assertions.assertEquals(expected, TransformMessages.MESSAGES.posTest1(msg1, msg2));

        final Object obj = "Test";
        final String msg = "This is a test message";
        final String s1 = "s1";
        final String s2 = "s2";
        expected = String.format(TransformLogger.POS_MSG_2, msg.length(), s1, s2, obj.getClass());
        TransformLogger.LOGGER.posTest2(obj, msg, s1, s2);
        Assertions.assertEquals(expected, HANDLER.getMessage());
        Assertions.assertEquals(expected, TransformMessages.MESSAGES.posTest2(obj, msg, s1, s2));
    }
}
