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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.jboss.logging.annotations.ValidIdRange;
import org.jboss.logging.processor.model.MessageInterface;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
public class IdRangeValidator {

    private final Map<String, Map<ValidIdRange, MessageInterface>> processed = new HashMap<>();

    public Collection<ValidationMessage> validate(final MessageInterface messageInterface) {
        final List<ValidationMessage> messages = new LinkedList<>();
        for (ValidIdRange validIdRange : messageInterface.validIdRanges()) {
            if (validIdRange.min() > validIdRange.max()) {
                messages.add(createError(messageInterface, "Minimum id value (%d) cannot be greater than the maximum value (%d).",
                        validIdRange.min(), validIdRange.max()));
            } else {
                final Map<ValidIdRange, MessageInterface> processed = getProcessed(messageInterface);
                for (Entry<ValidIdRange, MessageInterface> entry : processed.entrySet()) {
                    final ValidIdRange vid = entry.getKey();
                    if (overlap(validIdRange, vid)) {
                        messages.add(createError(messageInterface, "@ValidIdRange min/max (%d/%d) overlap the range (%d/%d) on '%s'.",
                                validIdRange.min(), validIdRange.max(), vid.min(), vid.max(), entry.getValue().name()));
                    }
                }
                final MessageInterface previous = processed.put(validIdRange, messageInterface);
                if (previous != null) {
                    messages.add(createError(messageInterface, "%s was used on %s", validIdRange, messageInterface.name()));
                }
            }
        }
        return messages;
    }

    private boolean overlap(final ValidIdRange r1, final ValidIdRange r2) {
        return (r1.min() >= r2.min() && r1.min() <= r2.max()) ||
                (r1.max() >= r2.min() && r1.max() <= r2.max()) ||
                (r2.min() >= r1.min() && r2.min() <= r1.max()) ||
                (r2.max() >= r1.min() && r2.max() <= r1.max());
    }

    private Map<ValidIdRange, MessageInterface> findAll(final MessageInterface messageInterface) {
        final Map<ValidIdRange, MessageInterface> result = new HashMap<>();
        for (ValidIdRange validIdRange : messageInterface.validIdRanges()) {
            result.put(validIdRange, messageInterface);
        }
        for (MessageInterface sub : messageInterface.extendedInterfaces()) {
            result.putAll(findAll(sub));
        }
        return result;
    }

    private Map<ValidIdRange, MessageInterface> getProcessed(final MessageInterface messageInterface) {
        final String projectCode = messageInterface.projectCode();
        if (projectCode.isEmpty()) {
            return Collections.emptyMap();
        }
        if (processed.containsKey(projectCode)) {
            return processed.get(projectCode);
        }
        final Map<ValidIdRange, MessageInterface> result = new HashMap<>();
        processed.put(projectCode, result);
        return result;
    }
}
