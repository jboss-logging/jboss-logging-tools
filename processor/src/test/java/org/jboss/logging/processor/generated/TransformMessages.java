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

import java.util.Collection;
import java.util.Map;

import org.jboss.logging.Messages;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageBundle;
import org.jboss.logging.annotations.Pos;
import org.jboss.logging.annotations.Transform;
import org.jboss.logging.annotations.Transform.TransformType;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@MessageBundle(projectCode = AbstractLoggerTest.PROJECT_CODE)
public interface TransformMessages {

    final TransformMessages MESSAGES = Messages.getBundle(TransformMessages.class);

    final String HASH_CODE_MSG = "hashCode: %d";

    final String IDENTITY_HASH_CODE_MSG = "SystemIdentity: %d";

    final String GET_CLASS_MSG = "getClass: %s";

    final String SIZE_MSG = "size: %d";

    // getClass().hashCode();
    @Message(HASH_CODE_MSG)
    String msgClassHashCode(@Transform({TransformType.GET_CLASS, TransformType.HASH_CODE}) String s);

    String msgClassHashCode(@Transform({TransformType.GET_CLASS, TransformType.HASH_CODE}) Collection<String> c);

    String msgClassHashCode(@Transform({TransformType.GET_CLASS, TransformType.HASH_CODE}) String... array);

    String msgClassHashCode(@Transform({TransformType.GET_CLASS, TransformType.HASH_CODE}) Object[] array);

    String msgClassHashCode(@Transform({TransformType.GET_CLASS, TransformType.HASH_CODE}) Map<String, String> map);

    // hashCode()
    @Message(HASH_CODE_MSG)
    String msgObjectHashCode(@Transform(TransformType.HASH_CODE) String s);

    String msgObjectHashCode(@Transform(TransformType.HASH_CODE) Collection<String> c);

    String msgObjectHashCode(@Transform(TransformType.HASH_CODE) String... array);

    String msgObjectHashCode(@Transform(TransformType.HASH_CODE) Object[] array);

    String msgObjectHashCode(@Transform(TransformType.HASH_CODE) Map<String, String> map);

    // System.identityHashCode(getClass())
    @Message(IDENTITY_HASH_CODE_MSG)
    String msgClassIdentityHashCode(@Transform({TransformType.GET_CLASS, TransformType.IDENTITY_HASH_CODE}) String s);

    String msgClassIdentityHashCode(@Transform({TransformType.GET_CLASS, TransformType.IDENTITY_HASH_CODE}) Collection<String> c);

    String msgClassIdentityHashCode(@Transform({TransformType.GET_CLASS, TransformType.IDENTITY_HASH_CODE}) String... array);

    String msgClassIdentityHashCode(@Transform({TransformType.GET_CLASS, TransformType.IDENTITY_HASH_CODE}) Object[] array);

    String msgClassIdentityHashCode(@Transform({TransformType.GET_CLASS, TransformType.IDENTITY_HASH_CODE}) Map<String, String> map);

    // System.identityHashCode()
    @Message(IDENTITY_HASH_CODE_MSG)
    String msgObjectIdentityHashCode(@Transform(TransformType.IDENTITY_HASH_CODE) String s);

    String msgObjectIdentityHashCode(@Transform(TransformType.IDENTITY_HASH_CODE) Collection<String> c);

    String msgObjectIdentityHashCode(@Transform(TransformType.IDENTITY_HASH_CODE) String... array);

    String msgObjectIdentityHashCode(@Transform(TransformType.IDENTITY_HASH_CODE) Object[] array);

    String msgObjectIdentityHashCode(@Transform(TransformType.IDENTITY_HASH_CODE) Map<String, String> map);

    // getClass()
    @Message(GET_CLASS_MSG)
    String msgObjectClass(@Transform(TransformType.GET_CLASS) String s);


    String msgObjectClass(@Transform(TransformType.GET_CLASS) String... array);

    String msgObjectClass(@Transform(TransformType.GET_CLASS) Object[] array);

    // length/length()/size()
    @Message(SIZE_MSG)
    String msgSize(@Transform(TransformType.SIZE) String s);

    String msgSize(@Transform(TransformType.SIZE) Collection<String> c);

    String msgSize(@Transform(TransformType.SIZE) String... array);

    String msgSize(@Transform(TransformType.SIZE) Object[] array);

    String msgSize(@Transform(TransformType.SIZE) Map<String, String> map);

    // Position tests
    String POS_MSG_1 = "size %d hashCode %d identityHashCode %d";
    @Message(POS_MSG_1)
    String posTest1(@Pos(value = {2, 3}, transform = {@Transform(TransformType.HASH_CODE), @Transform(TransformType.IDENTITY_HASH_CODE)}) String msg1, @Pos(value = 1, transform = @Transform(TransformType.SIZE)) String msg2);

    String POS_MSG_2 = "size %d s1=%s s2=%s getClass() %s";
    @Message(POS_MSG_2)
    String posTest2(@Pos(value = 4, transform = @Transform(TransformType.GET_CLASS)) Object type, @Pos(value = 1, transform = @Transform(TransformType.SIZE)) String msg, @Pos(2) String s1, @Pos(3) String s2);
}
