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

package org.jboss.logging.processor.validation;

import static org.jboss.logging.processor.validation.ValidationMessageFactory.createError;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.jboss.logging.processor.model.MessageInterface;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class IdLengthValidator {
    private final Map<String, Integer> lengths = new HashMap<>();

    public Collection<ValidationMessage> validate(final MessageInterface messageInterface) {
        final List<ValidationMessage> messages = new LinkedList<>();
        final String projectCode = messageInterface.projectCode();
        final int idLength = messageInterface.getIdLength();
        if ((idLength > 0 && idLength < 3) || idLength > 8) {
            messages.add(createError(messageInterface,
                    "The length of the message id padding must be between 3 and 8. The value %d is invalid.", idLength));
        } else {
            synchronized (this) {
                // Check the length id's
                if (lengths.containsKey(projectCode)) {
                    final int len = lengths.get(projectCode);
                    if (len != idLength) {
                        messages.add(createError(messageInterface, "A length of %d was already used for project code '%s'.",
                                len, projectCode));
                    }
                } else {
                    lengths.put(projectCode, idLength);
                }
            }
        }
        return messages;
    }
}
