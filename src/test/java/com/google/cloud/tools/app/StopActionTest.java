/**
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
package com.google.cloud.tools.app;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import com.google.cloud.tools.app.action.StopAction;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * Unit tests for {@link StopAction}.
 */
@RunWith(MockitoJUnitRunner.class)
public class StopActionTest {

  @Mock
  HttpURLConnection connection;

  @Before
  public void setUp() throws IOException {
    doNothing().when(connection).setReadTimeout(anyInt());
    doNothing().when(connection).connect();
    doNothing().when(connection).disconnect();
    when(connection.getResponseMessage()).thenReturn("response");
  }

  @Test
  public void testCheckFlags() {

    // TODO : write a new test here
  }

}
