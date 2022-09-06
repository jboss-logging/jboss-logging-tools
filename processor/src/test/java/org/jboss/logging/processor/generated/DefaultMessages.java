/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2021, Red Hat, Inc., and individual contributors
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

import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageBundle;
import org.jboss.logging.annotations.ValidIdRange;

/**
 * @author James R. Perkins Jr. (jrp)
 */
@MessageBundle(projectCode = TestConstants.PROJECT_CODE)
@ValidIdRange(min = 10000, max = 10050)
public interface DefaultMessages {

    default String language(){
        return "zh_CN";
    }

    @Message(id = 10000, value = "Hello %s.")
    String hello(String name);

    @Message(id = 10001, value = "How are you %s?")
    String howAreYou(String name);

    @SuppressWarnings("unused")
    @Message(id = 10002, value = "Username %s is invalid.")
    RuntimeException invalidUser(String name);
}
