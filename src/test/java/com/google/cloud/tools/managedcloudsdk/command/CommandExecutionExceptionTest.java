/*
 * Copyright 2019 Google LLC.
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

package com.google.cloud.tools.managedcloudsdk.command;

import org.junit.Assert;
import org.junit.Test;

public class CommandExecutionExceptionTest {

  @Test
  public void testStdErr() {
    String stderr = "bad result";
    Throwable cause = new RuntimeException();
    CommandExecutionException ex = new CommandExecutionException("a message", cause, stderr);
    Assert.assertEquals("a message", ex.getMessage());
    Assert.assertEquals("bad result", ex.getErrorLog());
  }
  
  @Test
  public void testConstructor() {
    Throwable cause = new RuntimeException();
    CommandExecutionException ex = new CommandExecutionException(cause);
    Assert.assertNull(ex.getErrorLog());
  }

}
