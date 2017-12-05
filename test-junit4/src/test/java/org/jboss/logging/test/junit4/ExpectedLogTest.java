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

import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jboss.logging.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class ExpectedLogTest {

	@Test
	public void expectEvent_customMatcher() throws Throwable {
		final String expectedMessage = "<expected log message>";
		final String matcherDescription = "<matcher description>";
		final Matcher<LoggingEvent> matcher = new TypeSafeMatcher<LoggingEvent>() {
			@Override
			protected boolean matchesSafely(LoggingEvent item) {
				return item.getRenderedMessage().equals(expectedMessage);
			}

			@Override
			public void describeTo(org.hamcrest.Description description) {
				description.appendText(matcherDescription);
			}
		};

		// Log present
		assertSuccess(new TestStatement() {
			@Override
			public void execute(Logger logger, ExpectedLog rule) {
				rule.expectEvent(matcher);
				logger.info(expectedMessage);
			}
		});

		// No matching log
		assertFailure(new TestStatement() {
			@Override
			public void execute(Logger logger, ExpectedLog rule) {
				rule.expectEvent(matcher);
				logger.info("non-matching");
				logger.error("other non-matching");
			}
		});

		// No log at all
		assertFailure(new TestStatement() {
			@Override
			public void execute(Logger logger, ExpectedLog rule) {
				rule.expectEvent(matcher);
			}
		});
	}

	@Test
	public void expectEventMissing_customMatcher() throws Throwable {
		final String expectedMessage = "<expected log message>";
		final String matcherDescription = "<matcher description>";
		final Matcher<LoggingEvent> matcher = new TypeSafeMatcher<LoggingEvent>() {
			@Override
			protected boolean matchesSafely(LoggingEvent item) {
				return item.getRenderedMessage().equals(expectedMessage);
			}

			@Override
			public void describeTo(org.hamcrest.Description description) {
				description.appendText(matcherDescription);
			}
		};

		// Log present
		assertFailure(new TestStatement() {
			@Override
			public void execute(Logger logger, ExpectedLog rule) {
				rule.expectEventMissing(matcher);
				logger.info(expectedMessage);
			}
		});

		// No matching log
		assertSuccess(new TestStatement() {
			@Override
			public void execute(Logger logger, ExpectedLog rule) {
				rule.expectEventMissing(matcher);
				logger.info("non-matching");
				logger.error("other non-matching");
			}
		});

		// No log at all
		assertSuccess(new TestStatement() {
			@Override
			public void execute(Logger logger, ExpectedLog rule) {
				rule.expectEventMissing(matcher);
			}
		});
	}

	@Test
	public void expectLevelOrHigherMissing() throws Throwable {
		// Exact level
		assertFailure(new TestStatement() {
			@Override
			public void execute(Logger logger, ExpectedLog rule) {
				rule.expectLevelOrHigherMissing(Logger.Level.WARN);
				logger.warn("exact level");
			}
		});

		// Higher level
		assertFailure(new TestStatement() {
			@Override
			public void execute(Logger logger, ExpectedLog rule) {
				rule.expectLevelOrHigherMissing(Logger.Level.WARN);
				logger.error("higher level");
			}
		});

		// Lower level
		assertSuccess(new TestStatement() {
			@Override
			public void execute(Logger logger, ExpectedLog rule) {
				rule.expectLevelOrHigherMissing(Logger.Level.WARN);
				logger.info("lower level");
			}
		});

		// No log at all
		assertSuccess(new TestStatement() {
			@Override
			public void execute(Logger logger, ExpectedLog rule) {
				rule.expectLevelOrHigherMissing(Logger.Level.WARN);
			}
		});
	}

	@Test
	public void expectMessage_singleString() throws Throwable {
		final String expectedMessage = "<expected log message>";

		// Log present
		assertSuccess(new TestStatement() {
			@Override
			public void execute(Logger logger, ExpectedLog rule) {
				rule.expectMessage(expectedMessage);
				logger.info("foo " + expectedMessage + " bar");
			}
		});

		// No matching log
		assertFailure(new TestStatement() {
			@Override
			public void execute(Logger logger, ExpectedLog rule) {
				rule.expectMessage(expectedMessage);
				logger.info("non-matching");
				logger.error("other non-matching");
			}
		});

		// No log at all
		assertFailure(new TestStatement() {
			@Override
			public void execute(Logger logger, ExpectedLog rule) {
				rule.expectMessage(expectedMessage);
			}
		});
	}

	@Test
	public void expectMessageMissing_singleString() throws Throwable {
		final String expectedMessage = "<expected log message>";

		// Log present
		assertFailure(new TestStatement() {
			@Override
			public void execute(Logger logger, ExpectedLog rule) {
				rule.expectMessageMissing(expectedMessage);
				logger.info("foo " + expectedMessage + " bar");
			}
		});

		// No matching log
		assertSuccess(new TestStatement() {
			@Override
			public void execute(Logger logger, ExpectedLog rule) {
				rule.expectMessageMissing(expectedMessage);
				logger.info("non-matching");
				logger.error("other non-matching");
			}
		});

		// No log at all
		assertSuccess(new TestStatement() {
			@Override
			public void execute(Logger logger, ExpectedLog rule) {
				rule.expectMessageMissing(expectedMessage);
			}
		});
	}

	@Test
	public void expectMessage_multipleStrings() throws Throwable {
		final String expectedMessagePart1 = "<expected log message part 1>";
		final String expectedMessagePart2 = "<expected log message part 2>";

		// Log present
		assertSuccess(new TestStatement() {
			@Override
			public void execute(Logger logger, ExpectedLog rule) {
				rule.expectMessage(expectedMessagePart1, expectedMessagePart2);
				logger.info("foo " + expectedMessagePart2 + " bar " + expectedMessagePart1 + " foobar");
			}
		});

		// Log only partially present
		assertFailure(new TestStatement() {
			@Override
			public void execute(Logger logger, ExpectedLog rule) {
				rule.expectMessage(expectedMessagePart1, expectedMessagePart2);
				logger.info("foo " + expectedMessagePart2 + " bar");
			}
		});

		// No matching log
		assertFailure(new TestStatement() {
			@Override
			public void execute(Logger logger, ExpectedLog rule) {
				rule.expectMessage(expectedMessagePart1, expectedMessagePart2);
				logger.info("non-matching");
				logger.error("other non-matching");
			}
		});

		// No log at all
		assertFailure(new TestStatement() {
			@Override
			public void execute(Logger logger, ExpectedLog rule) {
				rule.expectMessage(expectedMessagePart1, expectedMessagePart2);
			}
		});
	}

	@Test
	public void expectMessageMissing_multipleStrings() throws Throwable {
		final String expectedMessagePart1 = "<expected log message part 1>";
		final String expectedMessagePart2 = "<expected log message part 2>";

		// Log present
		assertFailure(new TestStatement() {
			@Override
			public void execute(Logger logger, ExpectedLog rule) {
				rule.expectMessageMissing(expectedMessagePart1, expectedMessagePart2);
				logger.info("foo " + expectedMessagePart2 + " bar " + expectedMessagePart1 + " foobar");
			}
		});

		// Log only partially present
		assertSuccess(new TestStatement() {
			@Override
			public void execute(Logger logger, ExpectedLog rule) {
				rule.expectMessageMissing(expectedMessagePart1, expectedMessagePart2);
				logger.info("foo " + expectedMessagePart2 + " bar");
			}
		});

		// No matching log
		assertSuccess(new TestStatement() {
			@Override
			public void execute(Logger logger, ExpectedLog rule) {
				rule.expectMessageMissing(expectedMessagePart1, expectedMessagePart2);
				logger.info("non-matching");
				logger.error("other non-matching");
			}
		});

		// No log at all
		assertSuccess(new TestStatement() {
			@Override
			public void execute(Logger logger, ExpectedLog rule) {
				rule.expectMessageMissing(expectedMessagePart1, expectedMessagePart2);
			}
		});
	}

	@Test
	public void expectEvent_levelAndMessages() throws Throwable {
		final String expectedMessagePart1 = "<expected log message part 1>";
		final String expectedMessagePart2 = "<expected log message part 2>";

		// Log present
		assertSuccess(new TestStatement() {
			@Override
			public void execute(Logger logger, ExpectedLog rule) {
				rule.expectEvent(Logger.Level.INFO, expectedMessagePart1, expectedMessagePart2);
				logger.info("foo " + expectedMessagePart2 + " bar " + expectedMessagePart1 + " foobar");
			}
		});

		// Log only partially present (partial message)
		assertFailure(new TestStatement() {
			@Override
			public void execute(Logger logger, ExpectedLog rule) {
				rule.expectEvent(Logger.Level.INFO, expectedMessagePart1, expectedMessagePart2);
				logger.info("foo " + expectedMessagePart2 + " bar");
			}
		});

		// Log only partially present (wrong level)
		assertFailure(new TestStatement() {
			@Override
			public void execute(Logger logger, ExpectedLog rule) {
				rule.expectEvent(Logger.Level.INFO, expectedMessagePart1, expectedMessagePart2);
				logger.warn("foo " + expectedMessagePart2 + " bar " + expectedMessagePart1 + " foobar");
			}
		});

		// No matching log
		assertFailure(new TestStatement() {
			@Override
			public void execute(Logger logger, ExpectedLog rule) {
				rule.expectEvent(Logger.Level.INFO, expectedMessagePart1, expectedMessagePart2);
				logger.info("non-matching");
				logger.error("other non-matching");
			}
		});

		// No log at all
		assertFailure(new TestStatement() {
			@Override
			public void execute(Logger logger, ExpectedLog rule) {
				rule.expectEvent(Logger.Level.INFO, expectedMessagePart1, expectedMessagePart2);
			}
		});
	}

	@Test
	public void expectEventMissing_levelAndMessages() throws Throwable {
		final String expectedMessagePart1 = "<expected log message part 1>";
		final String expectedMessagePart2 = "<expected log message part 2>";

		// Log present
		assertFailure(new TestStatement() {
			@Override
			public void execute(Logger logger, ExpectedLog rule) {
				rule.expectEventMissing(Logger.Level.INFO, expectedMessagePart1, expectedMessagePart2);
				logger.info("foo " + expectedMessagePart2 + " bar " + expectedMessagePart1 + " foobar");
			}
		});

		// Log only partially present (partial message)
		assertSuccess(new TestStatement() {
			@Override
			public void execute(Logger logger, ExpectedLog rule) {
				rule.expectEventMissing(Logger.Level.INFO, expectedMessagePart1, expectedMessagePart2);
				logger.info("foo " + expectedMessagePart2 + " bar");
			}
		});

		// Log only partially present (wrong level)
		assertSuccess(new TestStatement() {
			@Override
			public void execute(Logger logger, ExpectedLog rule) {
				rule.expectEventMissing(Logger.Level.INFO, expectedMessagePart1, expectedMessagePart2);
				logger.warn("foo " + expectedMessagePart2 + " bar " + expectedMessagePart1 + " foobar");
			}
		});

		// No matching log
		assertSuccess(new TestStatement() {
			@Override
			public void execute(Logger logger, ExpectedLog rule) {
				rule.expectEventMissing(Logger.Level.INFO, expectedMessagePart1, expectedMessagePart2);
				logger.info("non-matching");
				logger.error("other non-matching");
			}
		});

		// No log at all
		assertSuccess(new TestStatement() {
			@Override
			public void execute(Logger logger, ExpectedLog rule) {
				rule.expectEventMissing(Logger.Level.INFO, expectedMessagePart1, expectedMessagePart2);
			}
		});
	}

	@Test
	public void expectMessage_customMatcher() throws Throwable {
		final String expectedMessage = "<expected log message>";
		final String matcherDescription = "<matcher description>";
		final Matcher<String> matcher = new TypeSafeMatcher<String>() {
			@Override
			protected boolean matchesSafely(String item) {
				return item.equals(expectedMessage);
			}

			@Override
			public void describeTo(org.hamcrest.Description description) {
				description.appendText(matcherDescription);
			}
		};

		// Log present
		assertSuccess(new TestStatement() {
			@Override
			public void execute(Logger logger, ExpectedLog rule) {
				rule.expectMessage(matcher);
				logger.info(expectedMessage);
			}
		});

		// No matching log
		assertFailure(new TestStatement() {
			@Override
			public void execute(Logger logger, ExpectedLog rule) {
				rule.expectMessage(matcher);
				logger.info("non-matching");
				logger.error("other non-matching");
			}
		});

		// No log at all
		assertFailure(new TestStatement() {
			@Override
			public void execute(Logger logger, ExpectedLog rule) {
				rule.expectMessage(matcher);
			}
		});
	}

	@Test
	public void expectMessageMissing_customMatcher() throws Throwable {
		final String expectedMessage = "<expected log message>";
		final String matcherDescription = "<matcher description>";
		final Matcher<String> matcher = new TypeSafeMatcher<String>() {
			@Override
			protected boolean matchesSafely(String item) {
				return item.equals(expectedMessage);
			}

			@Override
			public void describeTo(org.hamcrest.Description description) {
				description.appendText(matcherDescription);
			}
		};

		// Log present
		assertFailure(new TestStatement() {
			@Override
			public void execute(Logger logger, ExpectedLog rule) {
				rule.expectMessageMissing(matcher);
				logger.info(expectedMessage);
			}
		});

		// No matching log
		assertSuccess(new TestStatement() {
			@Override
			public void execute(Logger logger, ExpectedLog rule) {
				rule.expectMessageMissing(matcher);
				logger.info("non-matching");
				logger.error("other non-matching");
			}
		});

		// No log at all
		assertSuccess(new TestStatement() {
			@Override
			public void execute(Logger logger, ExpectedLog rule) {
				rule.expectMessageMissing(matcher);
			}
		});
	}

	private void assertSuccess(TestStatement statement) throws Throwable {
		AssertionError error = runSafely(statement);
		Assert.assertNull("Expected no error, got " + error, error);
	}

	private void assertFailure(TestStatement statement) throws Throwable {
		AssertionError error = runSafely(statement);
		Assert.assertNotNull("Expected an error, got success", error);
	}

	private AssertionError runSafely(TestStatement statement) throws Throwable {
		try {
			Logger logger = Logger.getLogger(getClass());
			ExpectedLog testedRule = ExpectedLog.create();
			Statement junitStatement = new Statement() {
				@Override
				public void evaluate() throws Throwable {
					statement.execute(logger, testedRule);
				}
			};
			testedRule.apply(junitStatement, Description.EMPTY).evaluate();
			return null; // No failure
		}
		catch (AssertionError assertionError) {
			return assertionError;
		}
	}

	private interface TestStatement {
		void execute(Logger logger, ExpectedLog rule);
	}
}
