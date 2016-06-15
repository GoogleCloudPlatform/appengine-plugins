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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class DefaultProcessRunnerTest {

  private String originalOs;

  @Before
  public void saveOriginalOS() {
    originalOs = System.getProperty("os.name");
  }

  @After
  public void restoreOriginalOS() {
    System.setProperty("os.name", originalOs);
  }

  @Test
  public void testMakeOsSpecific_Unix() {
    DefaultProcessRunner runner = new DefaultProcessRunner(false, null, null, null, null);

    System.setProperty("os.name", "Linux");
    String[] command = {"gcloud", "help"};
    String[] osSpecificCommand = runner.makeOsSpecific(command);

    assertArrayEquals(new String[]{"gcloud", "help"}, osSpecificCommand);
  }

  @Test
  public void testMakeOsSpecific_Windows() {
    DefaultProcessRunner runner = new DefaultProcessRunner(false, null, null, null, null);

    System.setProperty("os.name", "Windows");
    String[] command = {"gcloud", "help"};
    String[] osSpecificCommand = runner.makeOsSpecific(command);

    assertArrayEquals(new String[]{"cmd.exe", "/c", "gcloud", "help"}, osSpecificCommand);
  }

}
