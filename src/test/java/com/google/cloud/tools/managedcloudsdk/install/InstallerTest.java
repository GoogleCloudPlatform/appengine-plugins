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
import com.google.cloud.tools.managedcloudsdk.command.CommandExecutionException;
import com.google.cloud.tools.managedcloudsdk.command.CommandExitException;
import com.google.cloud.tools.managedcloudsdk.command.CommandRunner;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
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
  @Mock private CommandRunner mockCommandRunner;
  @Mock private MessageListener mockMessageListener;

  @Rule public TemporaryFolder tmp = new TemporaryFolder();

  private Path fakeWorkingDirectory;
  private List<String> fakeCommand = Arrays.asList("scriptexec", "test-install.script");

  @Before
  public void setUp() throws IOException, ExecutionException, InterruptedException {
    MockitoAnnotations.initMocks(this);

    fakeWorkingDirectory = tmp.getRoot().toPath();
    Mockito.when(mockInstallScriptProvider.getScriptCommandLine()).thenReturn(fakeCommand);
  }

  private void verifyInstallerExecution(boolean usageReporting)
      throws InterruptedException, CommandExitException, CommandExecutionException {
    Mockito.verify(mockCommandRunner)
        .run(expectedCommand(usageReporting), fakeWorkingDirectory, null, mockMessageListener);
    Mockito.verifyNoMoreInteractions(mockCommandRunner);
  }

  @Test
  public void testCall() throws Exception {
    new Installer<>(
            fakeWorkingDirectory,
            mockInstallScriptProvider,
            false,
            mockMessageListener,
            mockCommandRunner)
        .install();

    verifyInstallerExecution(false);
  }

  @Test
  public void testCall_withUsageReporting() throws Exception {
    new Installer<>(
            tmp.getRoot().toPath(),
            mockInstallScriptProvider,
            true,
            mockMessageListener,
            mockCommandRunner)
        .install();

    verifyInstallerExecution(true);
  }

  private List<String> expectedCommand(boolean usageReporting) {
    List<String> command = new ArrayList<>(fakeCommand);
    command.add("--path-update=false");
    command.add("--command-completion=false");
    command.add("--quiet");
    command.add("--usage-reporting=" + usageReporting);

    return command;
  }
}
