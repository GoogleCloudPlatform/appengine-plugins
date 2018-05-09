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
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/** Tests for {@link CommandCaller} */
@RunWith(MockitoJUnitRunner.class)
public class CommandCallerTest {

  @Rule public TemporaryFolder testDir = new TemporaryFolder();

  @Mock private ProcessExecutorFactory mockProcessExecutorFactory;
  @Mock private ProcessExecutor mockProcessExecutor;
  @Mock private AsyncStreamSaver mockStreamSaver;
  @Mock private AsyncStreamSaverFactory mockStreamSaverFactory;
  @Mock private ListenableFuture<String> mockResult;

  private List<String> fakeCommand;
  private Path fakeWorkingDirectory;
  private Map<String, String> fakeEnvironment;

  private CommandCaller testCommandCaller;

  @Before
  public void setUp() throws IOException, InterruptedException, ExecutionException {
    fakeCommand = Arrays.asList("gcloud", "test", "--option");
    fakeWorkingDirectory = testDir.getRoot().toPath();
    fakeEnvironment = ImmutableMap.of("testKey", "testValue");

    Mockito.when(mockProcessExecutorFactory.newProcessExecutor()).thenReturn(mockProcessExecutor);
    Mockito.when(mockStreamSaverFactory.newSaver()).thenReturn(mockStreamSaver);
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

    testCommandCaller = new CommandCaller(mockProcessExecutorFactory, mockStreamSaverFactory);
  }

  private void verifyCommandExecution() throws IOException, InterruptedException {
    Mockito.verify(mockProcessExecutor)
        .run(fakeCommand, fakeWorkingDirectory, fakeEnvironment, mockStreamSaver, mockStreamSaver);
    Mockito.verifyNoMoreInteractions(mockProcessExecutor);
  }

  @Test
  public void testCall()
      throws IOException, InterruptedException, CommandExecutionException, CommandExitException {
    Assert.assertEquals(
        "testAnswer", testCommandCaller.call(fakeCommand, fakeWorkingDirectory, fakeEnvironment));
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
      testCommandCaller.call(fakeCommand, fakeWorkingDirectory, fakeEnvironment);
      Assert.fail("CommandExitException expected but not found.");
    } catch (CommandExitException ex) {
      Assert.assertEquals("Process failed with exit code: 10", ex.getMessage());
      Assert.assertEquals(10, ex.getExitCode());
      Assert.assertEquals("testAnswer\ntestAnswer", ex.getErrorLog());
    }
    verifyCommandExecution();
  }

  @Test
  public void testCall_interruptedExceptionPassthrough()
      throws CommandExecutionException, CommandExitException, ExecutionException,
          InterruptedException, IOException {
    Mockito.when(mockResult.get()).thenThrow(InterruptedException.class);

    try {
      testCommandCaller.call(fakeCommand, fakeWorkingDirectory, fakeEnvironment);
      Assert.fail("InterruptedException expected but not found.");
    } catch (InterruptedException ex) {
      // pass
    }

    verifyCommandExecution();
  }
}
