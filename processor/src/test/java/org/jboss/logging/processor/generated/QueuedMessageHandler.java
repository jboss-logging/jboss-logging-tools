/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.jboss.logmanager.ExtHandler;
import org.jboss.logmanager.ExtLogRecord;

/**
 * @author <a href="mailto:jperkins@redhat.com">James R. Perkins</a>
 */
class QueuedMessageHandler extends ExtHandler {
    //private final List<String> messages = Collections.synchronizedList(new ArrayList<>());
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
}
