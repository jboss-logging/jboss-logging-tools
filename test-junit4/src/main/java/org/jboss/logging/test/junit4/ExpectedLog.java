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

import org.hamcrest.*;
import org.jboss.logging.Logger;
import org.junit.rules.TestRule;
import org.junit.runners.model.Statement;

import java.util.*;

import static org.junit.Assert.fail;

/**
 * @author Yoann Rodiere
 */
public class ExpectedLog implements TestRule {

	/**
	 * Returns a {@linkplain TestRule rule} that does not mandate any particular log to be produced (identical to
	 * behavior without this rule).
	 */
	public static ExpectedLog create() {
		return new ExpectedLog();
	}

	private final ListenableDelegatingLogger logger;

	private List<Matcher<?>> expectations = new ArrayList<>();

	private List<Matcher<?>> absenceExpectations = new ArrayList<>();

	private ExpectedLog() {
		this(Logger.getLogger(""));
	}

	public ExpectedLog(Logger logger) {
		this.logger = LogInspectionHelper.convertType(logger);
	}

	@Override
	public Statement apply(Statement base, org.junit.runner.Description description) {
		return new ExpectedLogStatement(base);
	}

	/**
	 * Verify that your code produces a log event matching the given matcher.
	 */
	public void expectEvent(Matcher<? extends LoggingEvent> matcher) {
		expectations.add(matcher);
	}

	/**
	 * Verify that your code <strong>doesn't</strong> produce a log event matching the given matcher.
	 */
	public void expectEventMissing(Matcher<? extends LoggingEvent> matcher) {
		absenceExpectations.add(matcher);
	}

	/**
	 * Verify that your code <strong>doesn't</strong> produce a log event matching the given level or higher.
	 */
	public void expectLevelOrHigherMissing(Logger.Level level) {
		expectEventMissing(hasLevelOrHigher(level));
	}

	/**
	 * Verify that your code produces a log message containing the given string.
	 */
	public void expectMessage(String containedString) {
		expectMessage(CoreMatchers.containsString(containedString));
	}

	/**
	 * Verify that your code <strong>doesn't</strong> produce a log message containing the given string.
	 */
	public void expectMessageMissing(String containedString) {
		expectMessageMissing(CoreMatchers.containsString(containedString));
	}

	/**
	 * Verify that your code produces a log message containing all of the given strings.
	 */
	public void expectMessage(String containedString, String... otherContainedStrings) {
		expectMessage(containsAllStrings(containedString, otherContainedStrings));
	}

	/**
	 * Verify that your code <strong>doesn't</strong> produce a log message containing all of the given strings.
	 */
	public void expectMessageMissing(String containedString, String... otherContainedStrings) {
		expectMessageMissing(containsAllStrings(containedString, otherContainedStrings));
	}

	/**
	 * Verify that your code produces a log event at the given level and with a message containing all of the given strings.
	 */
	public void expectEvent(Logger.Level level, String containedString, String... otherContainedStrings) {
		expectEvent(CoreMatchers.allOf(hasLevel(level),
				eventMessageMatcher(containsAllStrings(containedString, otherContainedStrings))));
	}

	/**
	 * Verify that your code <strong>doesn't</strong> produce a log event at the given level and
	 * with a message containing all of the given strings.
	 */
	public void expectEventMissing(Logger.Level level, String containedString, String... otherContainedStrings) {
		expectEventMissing(CoreMatchers.allOf(hasLevel(level),
				eventMessageMatcher(containsAllStrings(containedString, otherContainedStrings))));
	}

	/**
	 * Verify that your code produces a log message matches the given Hamcrest matcher.
	 */
	public void expectMessage(Matcher<String> matcher) {
		expectEvent(eventMessageMatcher(matcher));
	}

	/**
	 * Verify that your code <strong>doesn't</strong> produce a log message matches the given Hamcrest matcher.
	 */
	public void expectMessageMissing(Matcher<String> matcher) {
		expectEventMissing(eventMessageMatcher(matcher));
	}

	private Matcher<LoggingEvent> hasLevelOrHigher(Logger.Level level) {
		return new TypeSafeMatcher<LoggingEvent>() {
			@Override
			public void describeTo(Description description) {
				description.appendText( "a LoggingEvent with " ).appendValue( level ).appendText( " level or higher" );
			}
			@Override
			protected boolean matchesSafely(LoggingEvent item) {
				return level.compareTo( item.getLevel() ) >= 0;
			}
		};
	}

	private Matcher<LoggingEvent> hasLevel(Logger.Level level) {
		return new TypeSafeMatcher<LoggingEvent>() {
			@Override
			public void describeTo(Description description) {
				description.appendText( "a LoggingEvent with " ).appendValue( level ).appendText( " level" );
			}
			@Override
			protected boolean matchesSafely(LoggingEvent item) {
				return level.equals( item.getLevel() );
			}
		};
	}

	private Matcher<String> containsAllStrings(String containedString, String... otherContainedStrings) {
		Collection<Matcher<? super String>> matchers = new ArrayList<>();
		matchers.add(CoreMatchers.containsString(containedString));
		for (String otherContainedString : otherContainedStrings) {
			matchers.add(CoreMatchers.containsString(otherContainedString));
		}
		return CoreMatchers.allOf(matchers);
	}

	private Matcher<LoggingEvent> eventMessageMatcher(final Matcher<String> messageMatcher) {
		return new TypeSafeMatcher<LoggingEvent>() {

			@Override
			public void describeTo(Description description) {
				description.appendText("a LoggingEvent with message matching ");
				messageMatcher.describeTo(description);
			}

			@Override
			protected boolean matchesSafely(LoggingEvent item) {
				return messageMatcher.matches(item.getRenderedMessage());
			}
		};
	}

	private class Listener implements LogListener {
		private final Set<Matcher<?>> expectationsMet = new HashSet<>();
		private final Set<LoggingEvent> unexpectedEvents = new HashSet<>();

		@Override
		public void loggedEvent(LoggingEvent event) {
			for (Matcher<?> expectation : ExpectedLog.this.expectations) {
				if (!expectationsMet.contains(expectation) && expectation.matches(event)) {
					expectationsMet.add(expectation);
				}
			}
			for (Matcher<?> absenceExpectation : ExpectedLog.this.absenceExpectations) {
				if (absenceExpectation.matches(event)) {
					unexpectedEvents.add(event);
				}
			}
		}

		public Set<Matcher<?>> getExpectationsNotMet() {
			Set<Matcher<?>> expectationsNotMet = new HashSet<>();
			expectationsNotMet.addAll(expectations);
			expectationsNotMet.removeAll(expectationsMet);
			return expectationsNotMet;
		}

		public Set<LoggingEvent> getUnexpectedEvents() {
			return unexpectedEvents;
		}

	}

	private class ExpectedLogStatement extends Statement {

		private final Statement next;

		public ExpectedLogStatement(Statement base) {
			next = base;
		}

		@Override
		public void evaluate() throws Throwable {
			Listener listener = new Listener();
			logger.getListenable().registerListener(listener);
			try {
				next.evaluate();
			} finally {
				logger.getListenable().unregisterListener(listener);
			}
			Set<Matcher<?>> expectationsNotMet = listener.getExpectationsNotMet();
			Set<LoggingEvent> unexpectedEvents = listener.getUnexpectedEvents();
			if (!expectationsNotMet.isEmpty() || !unexpectedEvents.isEmpty()) {
				fail(buildFailureMessage(expectationsNotMet, unexpectedEvents));
			}
		}
	}

	private static String buildFailureMessage(Set<Matcher<?>> missingSet, Set<LoggingEvent> unexpectedEvents) {
		Description description = new StringDescription();
		description.appendText("Produced logs did not meet the expectations.");
		if (!missingSet.isEmpty()) {
			description.appendText("\nMissing logs:");
			for (Matcher<?> missing : missingSet) {
				description.appendText("\n\t");
				missing.describeTo(description);
			}
		}
		if (!unexpectedEvents.isEmpty()) {
			description.appendText("\nUnexpected logs:");
			for (LoggingEvent unexpected : unexpectedEvents) {
				description.appendText("\n\t");
				description.appendText(unexpected.getRenderedMessage());
			}
		}
		return description.toString();
	}

}
