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

package com.google.cloud.tools.managedcloudsdk.components;

import com.google.cloud.tools.managedcloudsdk.MessageListener;
import com.google.cloud.tools.managedcloudsdk.command.CommandExecutionException;
import com.google.cloud.tools.managedcloudsdk.command.CommandExitException;
import com.google.cloud.tools.managedcloudsdk.command.CommandRunner;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/** Tests for {@link SdkComponentInstaller} */
public class SdkComponentInstallerTest {

  @Rule public TemporaryFolder testDir = new TemporaryFolder();

  @Mock private MessageListener mockMessageListener;
  @Mock private CommandRunner mockCommandRunner;

  private Path fakeGcloud;
  private SdkComponent testComponent = SdkComponent.APP_ENGINE_JAVA;

  @Before
  public void setUpMocks() throws IOException {
    MockitoAnnotations.initMocks(this);
    fakeGcloud = testDir.getRoot().toPath().resolve("fake-gcloud");
  }

  @Test
  public void testInstallComponent_successRun()
      throws InterruptedException, CommandExitException, CommandExecutionException {
    SdkComponentInstaller testInstaller = new SdkComponentInstaller(fakeGcloud, mockCommandRunner);
    testInstaller.installComponent(testComponent, mockMessageListener);
    Mockito.verify(mockCommandRunner).run(expectedCommand(), null, null, mockMessageListener);
  }

  private List<String> expectedCommand() {
    return Arrays.asList(
        fakeGcloud.toString(), "components", "install", testComponent.toString(), "--quiet");
  }
}
