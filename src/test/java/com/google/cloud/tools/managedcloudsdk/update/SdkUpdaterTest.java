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

package com.google.cloud.tools.managedcloudsdk.update;

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

/** Tests for {@link SdkUpdater} */
public class SdkUpdaterTest {

  @Rule public TemporaryFolder testDir = new TemporaryFolder();

  @Mock private MessageListener mockMessageListener;
  @Mock private CommandRunner mockCommandRunner;

  private Path fakeGcloud;

  @Before
  public void setUpFakesAndMocks() throws IOException {
    MockitoAnnotations.initMocks(this);
    fakeGcloud = testDir.getRoot().toPath().resolve("fake-gcloud");
  }

  @Test
  public void testUpdate_successRun()
      throws InterruptedException, CommandExitException, CommandExecutionException {
    SdkUpdater testUpdater = new SdkUpdater(fakeGcloud, mockCommandRunner);
    testUpdater.update(mockMessageListener);
    Mockito.verify(mockCommandRunner).run(expectedCommand(), null, null, mockMessageListener);
  }

  private List<String> expectedCommand() {
    return Arrays.asList(fakeGcloud.toString(), "components", "update", "--quiet");
  }
}
