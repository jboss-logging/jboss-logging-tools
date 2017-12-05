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

import java.text.MessageFormat;
import java.util.Locale;

import org.jboss.logging.Logger;

/**
 * A {@code Logger} implementation which delegates to another one
 * but makes it possible to test for events being logged (not logged).
 *
 * @author Sanne Grinovero <sanne@hibernate.org> (C) 2015 Red Hat Inc.
 */
final class ListenableDelegatingLogger extends Logger {

	private final Logger delegate;
	private final LogListenable listenable;

	ListenableDelegatingLogger(String name, Logger delegate, LogListenable listenable) {
		super( name );
		this.delegate = delegate;
		this.listenable = listenable;
	}

	public LogListenable getListenable() {
		return listenable;
	}

	@Override
	public boolean isEnabled(final Level level) {
		// We want users to think this logger is enabled, so as to detect all logger calls
		return listenable.isEnabled() || delegate.isEnabled(level);
	}

	@Override
	protected void doLog(final Level level, final String loggerClassName, final Object message, final Object[] parameters, final Throwable thrown) {
		if ( listenable.isEnabled() ) {
			LoggingEvent event = new LoggingEvent(
					getName(), loggerClassName, level,
					parameters == null || parameters.length == 0 ? String.valueOf( message )
							: new MessageFormat( String.valueOf( message ), Locale.getDefault() ).format( parameters ),
					thrown );
			listenable.notify( event );
		}
		delegate.log( loggerClassName, level, message, parameters, thrown );
	}

	@Override
	protected void doLogf(final Level level, final String loggerClassName, final String format, final Object[] parameters, final Throwable thrown) {
		if ( listenable.isEnabled() ) {
			LoggingEvent event = new LoggingEvent(
					getName(), loggerClassName, level,
					parameters == null ? format : String.format( Locale.getDefault(), format, parameters ),
					thrown );
			listenable.notify( event );
		}
		delegate.logf( loggerClassName, level, thrown, format, parameters );
	}
}
