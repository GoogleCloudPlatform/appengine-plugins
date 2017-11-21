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

package com.google.cloud.tools.managedcloudsdk.command;

import com.google.cloud.tools.managedcloudsdk.process.AsyncStreamSaver;
import com.google.cloud.tools.managedcloudsdk.process.ProcessExecutor;
import com.google.cloud.tools.managedcloudsdk.process.ProcessExecutorFactory;
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.ListenableFuture;
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

/** Tests for {@link CommandCaller} */
public class CommandCallerTest {

  @Rule public TemporaryFolder testDir = new TemporaryFolder();

  @Mock private ProcessExecutorFactory mockProcessExecutorFactory;
  @Mock private ProcessExecutor mockProcessExecutor;
  @Mock private AsyncStreamSaver mockStreamSaver;
  @Mock private ListenableFuture<String> mockResult;

  private List<String> fakeCommand;
  private Path fakeWorkingDirectory;
  private Map<String, String> fakeEnvironment;

  private CommandCaller testCommandCaller;

  @Before
  public void setUp() throws IOException, ExecutionException, InterruptedException {
    MockitoAnnotations.initMocks(this);

    fakeCommand = Arrays.asList("gcloud", "test", "--option");
    fakeWorkingDirectory = testDir.getRoot().toPath();
    fakeEnvironment = ImmutableMap.of("testKey", "testValue");

    Mockito.when(mockProcessExecutorFactory.newCommandExecutor()).thenReturn(mockProcessExecutor);
    Mockito.when(
            mockProcessExecutor.run(
                fakeCommand,
                fakeWorkingDirectory,
                fakeEnvironment,
                mockStreamSaver,
                mockStreamSaver))
        .thenReturn(0);
    Mockito.when(mockStreamSaver.getResult()).thenReturn(mockResult);
    Mockito.when(mockResult.get()).thenReturn("testAnswer");

    testCommandCaller =
        new CommandCaller(
            fakeCommand,
            fakeWorkingDirectory,
            fakeEnvironment,
            mockProcessExecutorFactory,
            mockStreamSaver,
            mockStreamSaver);
  }

  private void verifyCommandExecution() throws IOException, ExecutionException {
    Mockito.verify(mockProcessExecutor)
        .run(fakeCommand, fakeWorkingDirectory, fakeEnvironment, mockStreamSaver, mockStreamSaver);
    Mockito.verifyNoMoreInteractions(mockProcessExecutor);
  }

  @Test
  public void testCall() throws CommandExitException, ExecutionException, IOException {
    Assert.assertEquals("testAnswer", testCommandCaller.execute());
    verifyCommandExecution();
  }

  @Test
  public void testCall_nonZeroExit() throws Exception {
    Mockito.when(
            mockProcessExecutor.run(
                fakeCommand,
                fakeWorkingDirectory,
                fakeEnvironment,
                mockStreamSaver,
                mockStreamSaver))
        .thenReturn(10);

    try {
      testCommandCaller.execute();
      Assert.fail("CommandExitException expected but not found.");
    } catch (CommandExitException ex) {
      Assert.assertEquals("Process exited with non-zero exit code: 10", ex.getMessage());
    }
    verifyCommandExecution();
  }

  @Test
  public void testCall_outputConsumptionInterrupted() throws Exception {
    Mockito.when(mockResult.get()).thenThrow(InterruptedException.class);

    try {
      testCommandCaller.execute();
      Assert.fail("ExecutionException expected but not found.");
    } catch (ExecutionException ex) {
      Assert.assertEquals("Interrupted obtaining result.", ex.getMessage());
    }
    verifyCommandExecution();
  }
}
