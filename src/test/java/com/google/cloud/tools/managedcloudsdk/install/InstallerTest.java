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

package com.google.cloud.tools.managedcloudsdk.install;

import com.google.cloud.tools.managedcloudsdk.MessageListener;
import com.google.cloud.tools.managedcloudsdk.command.CommandFactory;
import com.google.cloud.tools.managedcloudsdk.command.CommandRunner;
import java.io.IOException;
import java.nio.file.Path;
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

/** Tests for {@link Installer} */
public class InstallerTest {

  @Mock private InstallScriptProvider mockInstallScriptProvider;
  @Mock private CommandFactory mockCommandFactory;
  @Mock private CommandRunner mockCommandRunner;
  @Mock private MessageListener mockMessageListener;

  @Rule public TemporaryFolder tmp = new TemporaryFolder();

  private Path fakeWorkingDirectory;
  private List<String> fakeCommand = Arrays.asList("scriptexec", "test-install.script");

  private Installer<?> testInstaller;

  @Before
  public void setUp() throws IOException, ExecutionException, InterruptedException {
    MockitoAnnotations.initMocks(this);

    fakeWorkingDirectory = tmp.getRoot().toPath();
    Mockito.when(mockInstallScriptProvider.getScriptCommandLine()).thenReturn(fakeCommand);

    testInstaller =
        new Installer<>(
            fakeWorkingDirectory,
            mockInstallScriptProvider,
            false,
            mockCommandFactory,
            mockMessageListener);

    Mockito.when(
            mockCommandFactory.newRunner(
                testInstaller.getCommand(), fakeWorkingDirectory, null, mockMessageListener))
        .thenReturn(mockCommandRunner);
  }

  private void verifyInstallerExecution() throws IOException, ExecutionException {
    Mockito.verify(mockCommandFactory)
        .newRunner(testInstaller.getCommand(), fakeWorkingDirectory, null, mockMessageListener);
    Mockito.verifyNoMoreInteractions(mockCommandFactory);
  }

  @Test
  public void testCall() throws Exception {
    testInstaller.install();
    verifyInstallerExecution();
  }

  @Test
  public void testGetCommand() {
    Assert.assertTrue(testInstaller.getCommand().contains("--usage-reporting=false"));
  }

  @Test
  public void testGetCommand_usageReportingTrue() throws Exception {
    Installer installer =
        new Installer<>(
            tmp.getRoot().toPath(),
            mockInstallScriptProvider,
            true,
            mockCommandFactory,
            mockMessageListener);

    Assert.assertTrue(installer.getCommand().contains("--usage-reporting=true"));
  }
}
