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

import com.google.cloud.tools.managedcloudsdk.ConsoleListener;
import com.google.cloud.tools.managedcloudsdk.ProgressListener;
import com.google.cloud.tools.managedcloudsdk.command.CommandRunner;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/** Tests for {@link Installer} */
@RunWith(MockitoJUnitRunner.class)
public class InstallerTest {

  @Mock private InstallScriptProvider mockInstallScriptProvider;
  @Mock private CommandRunner mockCommandRunner;
  @Mock private ProgressListener mockProgressListener;
  @Mock private ConsoleListener mockConsoleListener;

  @Rule public TemporaryFolder tmp = new TemporaryFolder();

  private Path sdkParentDirectory;
  private Path fakeSdkRoot;
  private List<String> fakeCommand = Arrays.asList("scriptexec", "test-install.script");
  private Map<String, String> fakeEnv = ImmutableMap.of("PROPERTY", "value");

  @Before
  public void setUp() throws IOException {
    sdkParentDirectory = tmp.getRoot().toPath();
    fakeSdkRoot = tmp.newFolder().toPath();
    Mockito.when(mockInstallScriptProvider.getScriptCommandLine(fakeSdkRoot))
        .thenReturn(fakeCommand);
    Mockito.when(mockInstallScriptProvider.getScriptEnvironment()).thenReturn(fakeEnv);
  }

  @Test
  public void testCall() throws Exception {
    new Installer(
            fakeSdkRoot,
            mockInstallScriptProvider,
            false,
            mockProgressListener,
            mockConsoleListener,
            mockCommandRunner)
        .install();

    Mockito.verify(mockCommandRunner)
        .run(expectedCommand(false), sdkParentDirectory, fakeEnv, mockConsoleListener);
    Mockito.verifyNoMoreInteractions(mockCommandRunner);

    ProgressVerifier.verifyUnknownProgress(mockProgressListener, "Installing Cloud SDK");
  }

  @Test
  public void testCall_withUsageReporting() throws Exception {
    new Installer(
            fakeSdkRoot,
            mockInstallScriptProvider,
            true,
            mockProgressListener,
            mockConsoleListener,
            mockCommandRunner)
        .install();

    Mockito.verify(mockCommandRunner)
        .run(expectedCommand(true), sdkParentDirectory, fakeEnv, mockConsoleListener);
    Mockito.verifyNoMoreInteractions(mockCommandRunner);
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
