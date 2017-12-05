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

import java.lang.reflect.Field;

import org.jboss.logging.BasicLogger;
import org.jboss.logging.DelegatingBasicLogger;

/**
 * Test helper to listen for logging events.
 * For this to work, it requires JBoss Logging to pick up our custom
 * implementation {@code Log4DelegatingLogger} via ServiceLoader.
 *
 * @author Sanne Grinovero <sanne@hibernate.org> (C) 2015 Red Hat Inc.
 */
final class LogInspectionHelper {

	private LogInspectionHelper() {
	}

	public static ListenableDelegatingLogger convertType(BasicLogger log) {
		if ( log instanceof DelegatingBasicLogger ) {
			//Most loggers generated via the annotation processor are of this type
			DelegatingBasicLogger wrapper = (DelegatingBasicLogger) log;
			try {
				return extractFromWrapper( wrapper );
			}
			catch (Exception cause) {
				throw new RuntimeException( cause );
			}
		}
		if ( ! ( log instanceof ListenableDelegatingLogger ) ) {
			throw new IllegalStateException( "Unexpected log type: JBoss Logger didn't register the custom TestableLoggerProvider as logger provider" );
		}
		return (ListenableDelegatingLogger) log;
	}

	private static ListenableDelegatingLogger extractFromWrapper(DelegatingBasicLogger wrapper) throws Exception {
		Field field = DelegatingBasicLogger.class.getDeclaredField( "log" );
		field.setAccessible( true );
		Object object = field.get( wrapper );
		return convertType( (BasicLogger) object );
	}

}
