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

import com.google.cloud.tools.managedcloudsdk.ConsoleListener;
import com.google.cloud.tools.managedcloudsdk.ProgressListener;
import com.google.cloud.tools.managedcloudsdk.command.CommandExecutionException;
import com.google.cloud.tools.managedcloudsdk.command.CommandExitException;
import com.google.cloud.tools.managedcloudsdk.command.CommandRunner;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/** Tests for {@link UnixUpdater} */
public class UnixUpdaterTest {

  @Rule public TemporaryFolder testDir = new TemporaryFolder();

  @Mock private ConsoleListener mockConsoleListener;
  @Mock private ProgressListener mockProgressListener;
  @Mock private CommandRunner mockCommandRunner;

  private Path fakeGcloud;
  private List<String> fakeUpdaterParams;

  @Before
  public void setUpFakesAndMocks() {
    MockitoAnnotations.initMocks(this);
    fakeGcloud = testDir.getRoot().toPath().resolve("fake-gcloud");
    fakeUpdaterParams = Arrays.asList("fake1", "fake2");
  }

  @Test
  public void testUpdate_successRun()
      throws InterruptedException, CommandExitException, CommandExecutionException {
    SdkUpdater testUpdater = new UnixUpdater(fakeGcloud, mockCommandRunner, fakeUpdaterParams);
    testUpdater.update(mockProgressListener, mockConsoleListener);
    Mockito.verify(mockProgressListener).start(Mockito.anyString(), Mockito.eq(-1L));
    Mockito.verify(mockProgressListener).done();
    Mockito.verify(mockCommandRunner).run(expectedCommand(), null, null, mockConsoleListener);
  }

  private List<String> expectedCommand() {
    List<String> expectedCommand = new ArrayList<>();
    expectedCommand.add(fakeGcloud.toString());
    expectedCommand.addAll(fakeUpdaterParams);
    return expectedCommand;
  }
}
