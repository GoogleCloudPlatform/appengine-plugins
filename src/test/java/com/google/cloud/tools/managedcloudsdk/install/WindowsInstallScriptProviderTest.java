/*
 * Copyright 2018 Google LLC
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

package com.google.cloud.tools.managedcloudsdk.install;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

public class WindowsInstallScriptProviderTest {

  @Test
  public void testGetScriptCommandLine_nonAbsoluteSdkRoot() {
    try {
      new UnixInstallScriptProvider().getScriptCommandLine(Paths.get("relative/path"));
      Assert.fail();
    } catch (IllegalArgumentException e) {
      Assert.assertEquals("non-absolute SDK path", e.getMessage());
    }
  }

  @Test
  public void testGetScriptCommandLine() {
    Assume.assumeTrue(System.getProperty("os.name").startsWith("Windows"));

    Path sdkRoot = Paths.get("C:\\path\\to\\sdk");
    List<String> commandLine = new WindowsInstallScriptProvider().getScriptCommandLine(sdkRoot);

    Assert.assertEquals(3, commandLine.size());
    Assert.assertEquals("cmd.exe", commandLine.get(0));
    Assert.assertEquals("/c", commandLine.get(1));

    Path scriptPath = Paths.get(commandLine.get(2));
    Assert.assertTrue(scriptPath.isAbsolute());
    Assert.assertEquals(Paths.get("C:\\path\\to\\sdk\\install.bat"), scriptPath);
  }
}
