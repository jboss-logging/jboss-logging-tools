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

package org.jboss.logging.processor.generated;

import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageBundle;
import org.jboss.logging.annotations.ValidIdRange;

/**
 * @author James R. Perkins Jr. (jrp)
 */
@MessageBundle(projectCode = TestConstants.PROJECT_CODE)
@ValidIdRange(min = 10000, max = 10050)
public interface DefaultMessages {

    @Message(id = 10000, value = "Hello %s.")
    String hello(String name);

    @Message(id = 10001, value = "How are you %s?")
    String howAreYou(String name);

    @SuppressWarnings("unused")
    @Message(id = 10002, value = "Username %s is invalid.")
    RuntimeException invalidUser(String name);
}
