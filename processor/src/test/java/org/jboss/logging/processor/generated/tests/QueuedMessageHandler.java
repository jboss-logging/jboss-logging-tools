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

package org.jboss.logging.processor.generated.tests;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.jboss.logging.processor.util.Objects;
import org.jboss.logmanager.ExtHandler;
import org.jboss.logmanager.ExtLogRecord;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
class QueuedMessageHandler extends ExtHandler {
    private final BlockingQueue<String> messages = new LinkedBlockingQueue<>();

    @Override
    protected void doPublish(final ExtLogRecord record) {
        messages.add(record.getFormattedMessage());
    }

    /**
     * Polls the message from queue waiting for up to 1 second for the message to appear.
     *
     * @return the message
     *
     * @throws InterruptedException if the poll was interrupted
     */
    String getMessage() throws InterruptedException {
        return messages.poll(1, TimeUnit.SECONDS);
    }

    int size() {
        return messages.size();
    }

    @Override
    public void flush() {
        // no-op
    }

    @Override
    public void close() throws SecurityException {
        messages.clear();
    }

    @Override
    public String toString() {
        return Objects.ToStringBuilder.of(this)
                .add("messages", messages)
                .toString();
    }
}
