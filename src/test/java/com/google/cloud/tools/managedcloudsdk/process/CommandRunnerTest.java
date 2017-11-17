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

package com.google.cloud.tools.managedcloudsdk.process;

import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/** Tests for {@link CommandRunner} */
public class CommandRunnerTest {

  @Rule public TemporaryFolder testDir = new TemporaryFolder();

  @Mock private CommandExecutorFactory mockCommandExecutorFactory;
  @Mock private CommandExecutor mockCommandExecutor;
  @Mock private AsyncStreamHandler mockStreamHandler;

  private List<String> fakeCommand;
  private Path fakeWorkingDirectory;
  private Map<String, String> fakeEnvironment;

  private CommandRunner testCommandRunner;

  @Before
  public void setUp() throws IOException, ExecutionException, InterruptedException {
    MockitoAnnotations.initMocks(this);

    fakeCommand = Arrays.asList("gcloud", "test", "--option");
    fakeWorkingDirectory = testDir.getRoot().toPath();
    fakeEnvironment = ImmutableMap.of("testKey", "testValue");

    Mockito.when(mockCommandExecutorFactory.newCommandExecutor()).thenReturn(mockCommandExecutor);
    Mockito.when(
            mockCommandExecutor.run(
                fakeCommand,
                fakeWorkingDirectory,
                fakeEnvironment,
                mockStreamHandler,
                mockStreamHandler))
        .thenReturn(0);

    testCommandRunner =
        new CommandRunner(
            fakeCommand,
            fakeWorkingDirectory,
            fakeEnvironment,
            mockCommandExecutorFactory,
            mockStreamHandler,
            mockStreamHandler);
  }

  private void verifyCommandExecution() throws IOException, ExecutionException {
    Mockito.verify(mockCommandExecutor)
        .run(
            fakeCommand,
            fakeWorkingDirectory,
            fakeEnvironment,
            mockStreamHandler,
            mockStreamHandler);
    Mockito.verifyNoMoreInteractions(mockCommandExecutor);
  }

  @Test
  public void testRun() throws CommandExitException, ExecutionException, IOException {
    testCommandRunner.run();
    verifyCommandExecution();
  }

  @Test
  public void testCall_nonZeroExit() throws Exception {
    Mockito.when(
            mockCommandExecutor.run(
                fakeCommand,
                fakeWorkingDirectory,
                fakeEnvironment,
                mockStreamHandler,
                mockStreamHandler))
        .thenReturn(10);

    try {
      testCommandRunner.run();
      Assert.fail("CommandExitException expected but not found.");
    } catch (CommandExitException ex) {
      Assert.assertEquals("Process exited with non-zero exit code: 10", ex.getMessage());
    }
    verifyCommandExecution();
  }
}
