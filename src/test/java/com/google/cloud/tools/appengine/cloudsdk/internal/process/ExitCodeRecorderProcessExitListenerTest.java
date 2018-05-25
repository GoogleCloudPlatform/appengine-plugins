/*
 * Copyright 2016 Google Inc.
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

package com.google.cloud.tools.appengine.cloudsdk.internal.process;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

/** Unit tests for {@link ExitCodeRecorderProcessExitListener} */
@RunWith(MockitoJUnitRunner.class)
public class ExitCodeRecorderProcessExitListenerTest {

  private ExitCodeRecorderProcessExitListener listener;

  @Before
  public void setup() {
    listener = new ExitCodeRecorderProcessExitListener();
  }

  @Test
  public void testGetMostRecentExitCode_null() {
    assertNull(listener.getMostRecentExitCode());
  }

  @Test
  public void testGetMostRecentExitCode_notNull() {
    int code = 0;
    listener.onExit(code);
    Integer mostRecentExitCode = listener.getMostRecentExitCode();
    assertNotNull(mostRecentExitCode);
    assertEquals(0, mostRecentExitCode.intValue());
  }
}
