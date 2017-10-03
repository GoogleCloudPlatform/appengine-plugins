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

package com.google.cloud.tools.managedcloudsdk.internal.install;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/** Tests for {@link LatestInstaller} */
public class LatestInstallerTest {

  @Mock private InstallScriptProvider mockInstallScriptProvider;
  @Mock private ProcessBuilderFactory mockProcessBuilderFactory;
  @Mock private ProcessBuilder mockProcessBuilder;
  @Mock private Process mockProcess;
  @Mock private InputStream mockInputStream;

  @Rule public TemporaryFolder tmp = new TemporaryFolder();

  @Before
  public void initializeAndConfigureMocks() throws IOException {
    MockitoAnnotations.initMocks(this);

    Mockito.when(mockInstallScriptProvider.getScriptCommandLine())
        .thenReturn(Arrays.asList("scriptexec", "test-install.script"));
    Mockito.when(mockProcessBuilderFactory.newProcessBuilder()).thenReturn(mockProcessBuilder);
    Mockito.when(mockProcessBuilder.start()).thenReturn(mockProcess);
    Mockito.when(mockProcess.getInputStream()).thenReturn(mockInputStream);
    Mockito.when(mockProcess.getErrorStream()).thenReturn(mockInputStream);
    Mockito.when(mockProcess.exitValue()).thenReturn(0);
  }

  @Test
  public void testCall() throws Exception {
    Installer installer =
        new LatestInstaller<>(
            tmp.getRoot().toPath(),
            mockInstallScriptProvider,
            false,
            null,
            mockProcessBuilderFactory);
    Path returnedSdkRoot = installer.call();

    Mockito.verify(mockProcessBuilder).command(getExpectedCommand(false));
    Mockito.verify(mockProcessBuilder).directory(tmp.getRoot());
    Mockito.verify(mockProcessBuilder).inheritIO();
    Mockito.verify(mockProcessBuilder).start();
    Mockito.verify(mockProcess).waitFor();
    Mockito.verify(mockProcess).exitValue();

    Mockito.verifyNoMoreInteractions(mockProcess, mockProcessBuilder);

    Assert.assertEquals(tmp.getRoot().toPath(), returnedSdkRoot);
  }

  @Test
  public void testCall_withUsageReporting() throws Exception {
    Installer installer =
        new LatestInstaller<>(
            tmp.getRoot().toPath(),
            mockInstallScriptProvider,
            true,
            null,
            mockProcessBuilderFactory);
    installer.call();

    Mockito.verify(mockProcessBuilder).command(getExpectedCommand(true));
  }

  @Test
  public void testCall_withListener() throws Exception {
    InstallProcessStreamHandler mockHandler = Mockito.mock(InstallProcessStreamHandler.class);
    Installer installer =
        new LatestInstaller<>(
            tmp.getRoot().toPath(),
            mockInstallScriptProvider,
            false,
            mockHandler,
            mockProcessBuilderFactory);
    installer.call();

    Mockito.verify(mockProcessBuilder, Mockito.never()).inheritIO();
    Mockito.verify(mockHandler).handleStreams(mockInputStream, mockInputStream);
  }

  @Test
  public void testCall_interrupted() throws Exception {
    Mockito.when(mockProcess.waitFor()).thenThrow(InterruptedException.class);

    Installer installer =
        new LatestInstaller<>(
            tmp.getRoot().toPath(),
            mockInstallScriptProvider,
            false,
            null,
            mockProcessBuilderFactory);
    try {
      installer.call();
      Assert.fail("ExecutionException expected but not found.");
    } catch (ExecutionException ex) {
      Assert.assertEquals("Process cancelled", ex.getMessage());
    }
    Mockito.verify(mockProcess).destroy();
  }

  @Test
  public void testCall_nonZeroExit() throws Exception {
    Mockito.when(mockProcess.exitValue()).thenReturn(10);

    Installer installer =
        new LatestInstaller<>(
            tmp.getRoot().toPath(),
            mockInstallScriptProvider,
            false,
            null,
            mockProcessBuilderFactory);
    try {
      installer.call();
      Assert.fail("ExecutionException expected but not found.");
    } catch (ExecutionException ex) {
      Assert.assertEquals("Process exited with non zero: 10", ex.getMessage());
    }
  }

  private List<String> getExpectedCommand(boolean usageReporting) {
    List<String> command = new ArrayList<>(6);
    command.add("scriptexec");
    command.add("test-install.script");
    command.add("--path-update=false");
    command.add("--command-completion=false");
    command.add("--quiet");
    command.add("--usage-reporting=" + usageReporting);
    return command;
  }
}
