/*
 * JBoss, Home of Professional Open Source.
 *
 * Copyright 2015 Red Hat, Inc., and individual contributors
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

package org.jboss.logging.test.junit4;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

final class LogListenable {

	// Synchronize access on the field
	private final List<LogListener> enabledListeners = new LinkedList<>();
	private final AtomicBoolean interceptEnabled = new AtomicBoolean( false );

	void registerListener(LogListener newListener) {
		synchronized (enabledListeners) {
			if ( newListener != null ) {
				enabledListeners.add( newListener );
				interceptEnabled.set( true );
			}
		}
	}

	void unregisterListener(LogListener listener) {
		synchronized (enabledListeners) {
			enabledListeners.remove( listener );
			if ( enabledListeners.isEmpty() ) {
				interceptEnabled.set( false );
			}
		}
	}

	boolean isEnabled() {
		return interceptEnabled.get();
	}

	void notify(LoggingEvent event) {
		synchronized (enabledListeners) {
			for ( LogListener listener : enabledListeners ) {
				listener.loggedEvent( event );
			}
		}
	}
}
