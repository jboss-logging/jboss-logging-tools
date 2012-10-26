package org.jboss.logging.processor.generated;

import java.util.Collection;
import java.util.Map;

import org.jboss.logging.Messages;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageBundle;
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
}
