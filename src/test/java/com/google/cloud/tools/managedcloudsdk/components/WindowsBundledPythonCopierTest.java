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
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class WindowsBundledPythonCopierTest {

  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

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

  @Test
  public void testDeleteCopiedPython() throws IOException {
    File pythonHome = temporaryFolder.newFolder("python");
    File executable = temporaryFolder.newFile("python/python.exe");
    Assert.assertTrue(executable.exists());

    WindowsBundledPythonCopier.deleteCopiedPython(executable.toString());
    Assert.assertFalse(executable.exists());
    Assert.assertFalse(pythonHome.exists());
  }

  @Test
  public void testDeleteCopiedPython_caseInsensitivity() throws IOException {
    File pythonHome = temporaryFolder.newFolder("python");
    File executable = temporaryFolder.newFile("python/PyThOn.EXE");
    Assert.assertTrue(executable.exists());
    Assert.assertThat(executable.toString(), Matchers.endsWith("PyThOn.EXE"));

    WindowsBundledPythonCopier.deleteCopiedPython(executable.toString());
    Assert.assertFalse(executable.exists());
    Assert.assertFalse(pythonHome.exists());
  }

  @Test
  public void testDeleteCopiedPython_unexpectedLocation() throws IOException {
    temporaryFolder.newFolder("unexpected");
    File unexpected = temporaryFolder.newFile("unexpected/file.ext");
    Assert.assertTrue(unexpected.exists());

    WindowsBundledPythonCopier.deleteCopiedPython(unexpected.toString());
    Assert.assertTrue(unexpected.exists());
  }

  @Test
  public void testDeleteCopiedPython_nonExistingDirectory() {
    Path executable = temporaryFolder.getRoot().toPath().resolve("python/python.exe");
    Assert.assertFalse(Files.exists(executable));

    WindowsBundledPythonCopier.deleteCopiedPython("python/python.exe");
    // Ensure no runtime exception is thrown.
  }

  @Test
  public void testIsUnderTempDirectory_variableTemp() {
    Assert.assertTrue(
        WindowsBundledPythonCopier.isUnderTempDirectory(
            "/temp/prefix/some/file.ext", ImmutableMap.of("TEMP", "/temp/prefix")));
  }

  @Test
  public void testIsUnderTempDirectory_variableTmp() {
    Assert.assertTrue(
        WindowsBundledPythonCopier.isUnderTempDirectory(
            "/tmp/prefix/some/file.ext", ImmutableMap.of("TMP", "/tmp/prefix")));
  }

  @Test
  public void testIsUnderTempDirectory_noTempVariables() {
    Assert.assertFalse(
        WindowsBundledPythonCopier.isUnderTempDirectory(
            "/tmp/prefix/some/file.ext", ImmutableMap.of()));
  }
}
