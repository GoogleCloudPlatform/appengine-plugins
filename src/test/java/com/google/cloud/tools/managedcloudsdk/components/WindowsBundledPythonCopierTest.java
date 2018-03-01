/*
 * Copyright 2018 Google Inc.
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

package com.google.cloud.tools.managedcloudsdk.components;

import com.google.cloud.tools.managedcloudsdk.command.CommandCaller;
import com.google.cloud.tools.managedcloudsdk.command.CommandExecutionException;
import com.google.cloud.tools.managedcloudsdk.command.CommandExitException;
import com.google.common.collect.ImmutableMap;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class WindowsBundledPythonCopierTest {

  @Mock private CommandCaller mockCommandCaller;
  private Path fakeGcloud;

  public WindowsBundledPythonCopierTest()
      throws InterruptedException, CommandExitException, CommandExecutionException {
    MockitoAnnotations.initMocks(this);
    fakeGcloud = Paths.get("my/path/to/fake-gcloud");
    Mockito.when(
            mockCommandCaller.call(
                Arrays.asList(fakeGcloud.toString(), "components", "copy-bundled-python"),
                null,
                null))
        .thenReturn("copied-python");
  }

  @Test
  public void testCopyPython()
      throws InterruptedException, CommandExitException, CommandExecutionException {
    WindowsBundledPythonCopier testCopier =
        new WindowsBundledPythonCopier(fakeGcloud, mockCommandCaller);
    Map<String, String> testEnv = testCopier.copyPython();

    Assert.assertEquals(ImmutableMap.of("CLOUDSDK_PYTHON", "copied-python"), testEnv);
  }
}
