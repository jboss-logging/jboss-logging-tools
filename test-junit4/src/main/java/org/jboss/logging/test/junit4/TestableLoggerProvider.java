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

import org.jboss.logging.DelegatingLoggerProvider;
import org.jboss.logging.Logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * A {@code LoggerProvider} for JBoss Logger.
 * See also META-INF/services/org.jboss.logging.LoggerProvider
 *
 * @author Sanne Grinovero <sanne@hibernate.org> (C) 2015 Red Hat Inc.
 */
public final class TestableLoggerProvider extends DelegatingLoggerProvider {

	//We LEAK Logger instances: good only for testing as we know the set of categories is limited in practice
	private final ConcurrentMap<String, Logger> reuseLoggerInstances = new ConcurrentHashMap<>();

	private final LogListenable listenable = new LogListenable();

	@Override
	public Logger getLogger(final String name) {
		Logger logger = reuseLoggerInstances.get( name );
		if ( logger == null ) {
			Logger delegateLogger = getDelegate().getLogger( name );
			logger = new ListenableDelegatingLogger( "".equals( name ) ? "ROOT" : name, delegateLogger, listenable );
			Logger previous = reuseLoggerInstances.putIfAbsent( name, logger );
			if ( previous != null ) {
				return previous;
			}
		}
		return logger;
	}
}
