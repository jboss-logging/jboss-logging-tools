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

package org.jboss.logging.processor.generated;

import java.util.Collection;
import java.util.Map;

import org.jboss.logging.Messages;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageBundle;
import org.jboss.logging.annotations.Pos;
import org.jboss.logging.annotations.Transform;
import org.jboss.logging.annotations.Transform.TransformType;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@MessageBundle(projectCode = TestConstants.PROJECT_CODE)
public interface TransformMessages {

    TransformMessages MESSAGES = Messages.getBundle(TransformMessages.class);

    String HASH_CODE_MSG = "hashCode: %d";

    String IDENTITY_HASH_CODE_MSG = "SystemIdentity: %d";

    String GET_CLASS_MSG = "getClass: %s";

    String SIZE_MSG = "size: %d";

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
