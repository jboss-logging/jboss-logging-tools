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

import org.jboss.logging.Logger;

public final class LoggingEvent {
	private final String loggerName;
	private final String loggerFqcn;
	private final Logger.Level level;
	private final String renderedMessage;
	private final Throwable thrown;

	LoggingEvent(
			String loggerName,
			String loggerFqcn,
			Logger.Level level,
			String renderedMessage,
			Throwable thrown) {
		this.loggerName = loggerName;
		this.loggerFqcn = loggerFqcn;
		this.level = level;
		this.renderedMessage = renderedMessage;
		this.thrown = thrown;
	}

	public String getLoggerName() {
		return loggerName;
	}

	public String getLoggerFqcn() {
		return loggerFqcn;
	}

	public Logger.Level getLevel() {
		return level;
	}

	public String getRenderedMessage() {
		return renderedMessage;
	}

	public Throwable getThrown() {
		return thrown;
	}
}
