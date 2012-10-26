package org.jboss.logging.processor.generated;

import java.util.Collection;
import java.util.Map;

import org.jboss.logging.Logger;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;
import org.jboss.logging.annotations.Transform;
import org.jboss.logging.annotations.Transform.TransformType;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
@MessageLogger(projectCode = AbstractLoggerTest.PROJECT_CODE)
public interface TransformLogger {

    final TransformLogger LOGGER = Logger.getMessageLogger(TransformLogger.class, AbstractLoggerTest.CATEGORY);

    final String HASH_CODE_MSG = "hashCode: %d";

    final String IDENTITY_HASH_CODE_MSG = "SystemIdentity: %d";

    final String GET_CLASS_MSG = "getClass: %s";

    final String SIZE_MSG = "size: %d";

    // getClass().hashCode();
    @LogMessage
    @Message(HASH_CODE_MSG)
    void logClassHashCode(@Transform({TransformType.GET_CLASS, TransformType.HASH_CODE}) String s);

    @LogMessage
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
}
