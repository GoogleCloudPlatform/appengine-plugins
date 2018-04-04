/*
 * Copyright 2017 Google Inc.
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

package com.google.cloud.tools.appengine.cloudsdk.process;

import com.google.cloud.tools.appengine.api.AppEngineException;
import org.junit.Assert;
import org.junit.Test;

public class NonZeroExceptionExitListenerTest {

  private NonZeroExceptionExitListener listener = new NonZeroExceptionExitListener();

  @Test
  public void testOnExit_exception() {
    try {
      listener.onExit(18);
      Assert.fail();
    } catch (AppEngineException ex) {
      Assert.assertNotNull(ex.getMessage());
    }
  }

  @Test
  public void testOnExit_zero() throws AppEngineException {
    listener.onExit(0);
    // no exception
  }
}
