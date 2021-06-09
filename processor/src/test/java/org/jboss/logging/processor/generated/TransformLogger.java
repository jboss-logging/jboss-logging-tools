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

package org.jboss.logging.processor.generated;

import java.util.Collection;
import java.util.Map;

import org.jboss.logging.Logger;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.logging.annotations.Pos;
import org.jboss.logging.annotations.Transform;
import org.jboss.logging.annotations.Transform.TransformType;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@MessageLogger(projectCode = TestConstants.PROJECT_CODE)
public interface TransformLogger {

    TransformLogger LOGGER = Logger.getMessageLogger(TransformLogger.class, TestConstants.CATEGORY);

    String HASH_CODE_MSG = "hashCode: %d";

    String IDENTITY_HASH_CODE_MSG = "SystemIdentity: %d";

    String GET_CLASS_MSG = "getClass: %s";

    String SIZE_MSG = "size: %d";

    // getClass().hashCode();
    @LogMessage
    @Message(HASH_CODE_MSG)
    void logClassHashCode(@Transform({TransformType.GET_CLASS, TransformType.HASH_CODE}) String s);

    @LogMessage(useThreadContext = true)
    void logClassHashCode(@Transform({TransformType.GET_CLASS, TransformType.HASH_CODE}) Collection<String> c);

    @LogMessage
    void logClassHashCode(@Transform({TransformType.GET_CLASS, TransformType.HASH_CODE}) String... array);

    @LogMessage
    void logClassHashCode(@Transform({TransformType.GET_CLASS, TransformType.HASH_CODE}) Object[] array);

    @LogMessage
    void logClassHashCode(@Transform({TransformType.GET_CLASS, TransformType.HASH_CODE}) Map<String, String> map);

    // hashCode()
    @LogMessage
    @Message(HASH_CODE_MSG)
    void logObjectHashCode(@Transform(TransformType.HASH_CODE) String s);

    @LogMessage
    void logObjectHashCode(@Transform(TransformType.HASH_CODE) Collection<String> c);

    @LogMessage
    void logObjectHashCode(@Transform(TransformType.HASH_CODE) String... array);

    @LogMessage
    void logObjectHashCode(@Transform(TransformType.HASH_CODE) Object[] array);

    @LogMessage
    void logObjectHashCode(@Transform(TransformType.HASH_CODE) Map<String, String> map);

    // System.identityHashCode(getClass())
    @LogMessage
    @Message(IDENTITY_HASH_CODE_MSG)
    void logClassIdentityHashCode(@Transform({TransformType.GET_CLASS, TransformType.IDENTITY_HASH_CODE}) String s);

    @LogMessage
    void logClassIdentityHashCode(@Transform({TransformType.GET_CLASS, TransformType.IDENTITY_HASH_CODE}) Collection<String> c);

    @LogMessage
    void logClassIdentityHashCode(@Transform({TransformType.GET_CLASS, TransformType.IDENTITY_HASH_CODE}) String... array);

    @LogMessage
    void logClassIdentityHashCode(@Transform({TransformType.GET_CLASS, TransformType.IDENTITY_HASH_CODE}) Object[] array);

    @LogMessage
    void logClassIdentityHashCode(@Transform({TransformType.GET_CLASS, TransformType.IDENTITY_HASH_CODE}) Map<String, String> map);

    // System.identityHashCode()
    @LogMessage
    @Message(IDENTITY_HASH_CODE_MSG)
    void logObjectIdentityHashCode(@Transform(TransformType.IDENTITY_HASH_CODE) String s);

    @LogMessage
    void logObjectIdentityHashCode(@Transform(TransformType.IDENTITY_HASH_CODE) Collection<String> c);

    @LogMessage
    void logObjectIdentityHashCode(@Transform(TransformType.IDENTITY_HASH_CODE) String... array);

    @LogMessage
    void logObjectIdentityHashCode(@Transform(TransformType.IDENTITY_HASH_CODE) Object[] array);

    @LogMessage
    void logObjectIdentityHashCode(@Transform(TransformType.IDENTITY_HASH_CODE) Map<String, String> map);

    // getClass()
    @LogMessage
    @Message(GET_CLASS_MSG)
    void logObjectClass(@Transform(TransformType.GET_CLASS) String s);


    @LogMessage
    void logObjectClass(@Transform(TransformType.GET_CLASS) String... array);

    @LogMessage
    void logObjectClass(@Transform(TransformType.GET_CLASS) Object[] array);

    // length/length()/size()
    @LogMessage
    @Message(SIZE_MSG)
    void logSize(@Transform(TransformType.SIZE) String s);

    @LogMessage
    void logSize(@Transform(TransformType.SIZE) Collection<String> c);

    @LogMessage
    void logSize(@Transform(TransformType.SIZE) String... array);

    @LogMessage
    void logSize(@Transform(TransformType.SIZE) Object[] array);

    @LogMessage
    void logSize(@Transform(TransformType.SIZE) Map<String, String> map);

    // Position tests
    String POS_MSG_1 = "size %d hashCode %d identityHashCode %d";
    @LogMessage
    @Message(POS_MSG_1)
    void posTest1(@Pos(value = {2, 3}, transform = {@Transform(TransformType.HASH_CODE), @Transform(TransformType.IDENTITY_HASH_CODE)}) String msg1, @Pos(value = 1, transform = @Transform(TransformType.SIZE)) String msg2);

    String POS_MSG_2 = "size %d s1=%s s2=%s getClass() %s";
    @LogMessage
    @Message(POS_MSG_2)
    void posTest2(@Pos(value = 4, transform = @Transform(TransformType.GET_CLASS)) Object type, @Pos(value = 1, transform = @Transform(TransformType.SIZE)) String msg, @Pos(2) String s1, @Pos(3) String s2);
}
