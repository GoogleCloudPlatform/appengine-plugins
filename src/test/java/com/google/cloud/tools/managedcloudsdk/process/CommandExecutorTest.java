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

import com.google.cloud.tools.managedcloudsdk.MessageListener;
import com.google.common.base.Joiner;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/** Tests for CommandExecutor */
public class CommandExecutorTest {

  @Mock private CommandExecutor.ProcessBuilderFactory processBuilderFactoryMock;
  @Mock private ProcessBuilder processBuilderMock;
  @Mock private Process processMock;
  @Mock private MessageListener messageListener;
  private final List<String> command = Arrays.asList("someCommand, someOption");
  private final List<String> output = Arrays.asList("some output line 1", "some output line 2");

  private InOrder loggerInOrder;

  @Before
  public void setup() throws IOException, InterruptedException {
    MockitoAnnotations.initMocks(this);

    Mockito.when(processBuilderFactoryMock.createProcessBuilder()).thenReturn(processBuilderMock);
    Mockito.when(processBuilderMock.start()).thenReturn(processMock);
    Mockito.when(processMock.waitFor()).thenReturn(0);
    Mockito.when(processMock.getInputStream())
        .thenReturn(new ByteArrayInputStream(Joiner.on("\n").join(output).getBytes()));
    loggerInOrder = Mockito.inOrder(messageListener);
  }

  @Test
  public void testRun() throws IOException, InterruptedException, ExecutionException {
    List<String> command = Arrays.asList("someCommand", "someOption");
    List<String> expectedOutput = Arrays.asList("some output line 1", "some output line 2");

    // Mocks the environment for the processBuilderMock to put the environment map in.
    Map<String, String> environmentInput = new HashMap<>();
    environmentInput.put("ENV1", "val1");
    environmentInput.put("ENV2", "val2");
    Map<String, String> processEnvironment = new HashMap<>();
    Mockito.when(processBuilderMock.environment()).thenReturn(processEnvironment);

    Path fakeWorkingDirectory = Paths.get("/tmp/fake/working/dir");

    setProcessMockOutput(expectedOutput);

    int exitCode =
        new CommandExecutor()
            .setMessageListener(messageListener)
            .setWorkingDirectory(fakeWorkingDirectory)
            .setEnvironment(environmentInput)
            .setProcessBuilderFactory(processBuilderFactoryMock)
            .run(command);

    verifyProcessBuilding(command);
    Mockito.verify(processBuilderMock).environment();
    Mockito.verify(processBuilderMock).directory(fakeWorkingDirectory.toFile());
    Assert.assertEquals(environmentInput, processEnvironment);
    Assert.assertEquals(expectedOutput, output);

    loggerInOrder.verify(messageListener).message("Running command : someCommand someOption");
    loggerInOrder.verify(messageListener).message("some output line 1");
    loggerInOrder.verify(messageListener).message("some output line 2");
  }

  @Test
  public void testRun_badProcessOutput()
      throws IOException, InterruptedException, ExecutionException {
    List<String> command = Arrays.asList("someCommand", "someOption");

    InputStream processOutput = Mockito.mock(InputStream.class);
    Mockito.when(processOutput.read()).thenThrow(IOException.class);
    Mockito.when(processMock.getInputStream()).thenReturn(processOutput);

    new CommandExecutor()
        .setMessageListener(messageListener)
        .setProcessBuilderFactory(processBuilderFactoryMock)
        .run(command);

    loggerInOrder.verify(messageListener).message("Running command : someCommand someOption");
    loggerInOrder.verify(messageListener).message("IO Exception reading process output");
  }

  @Test
  public void testRun_nonZeroExitCodePassthrough()
      throws IOException, InterruptedException, ExecutionException {
    List<String> command = Arrays.asList("someCommand", "someOption");
    List<String> expectedOutput = Arrays.asList("some output line 1", "some output line 2");

    setProcessMockOutput(expectedOutput);
    Mockito.when(processMock.waitFor()).thenReturn(123);

    int exitCode =
        new CommandExecutor()
            .setMessageListener(messageListener)
            .setProcessBuilderFactory(processBuilderFactoryMock)
            .run(command);

    Assert.assertEquals(123, exitCode);
  }

  @Test
  public void testRun_interruptedWaitingForOutputThreads()
      throws IOException, InterruptedException, ExecutionException {
    List<String> command = Arrays.asList("someCommand", "someOption");

    // Mocks the ExecutorService to be interrupted when awaiting termination.
    CommandExecutor.ExecutorServiceFactory executorServiceFactoryMock =
        Mockito.mock(CommandExecutor.ExecutorServiceFactory.class);
    ExecutorService executorServiceMock = Mockito.mock(ExecutorService.class);
    Mockito.when(executorServiceFactoryMock.createExecutorService())
        .thenReturn(executorServiceMock);
    Mockito.when(
            executorServiceMock.awaitTermination(CommandExecutor.TIMEOUT_SECONDS, TimeUnit.SECONDS))
        .thenThrow(new InterruptedException());

    new CommandExecutor()
        .setMessageListener(messageListener)
        .setProcessBuilderFactory(processBuilderFactoryMock)
        .setExecutorServiceFactory(executorServiceFactoryMock)
        .run(command);

    loggerInOrder.verify(messageListener).message("Running command : someCommand someOption");
    loggerInOrder
        .verify(messageListener)
        .message("Process output monitor termination interrupted.");
  }

  @Test
  public void testRun_interruptedWaitingForProcess() throws IOException, InterruptedException {
    List<String> command = Arrays.asList("someCommand", "someOption");

    // force an interruption to simulate a cancel.
    Mockito.when(processMock.waitFor()).thenThrow(InterruptedException.class);

    try {
      new CommandExecutor()
          .setMessageListener(messageListener)
          .setProcessBuilderFactory(processBuilderFactoryMock)
          .run(command);
      Assert.fail("Execution exception expected but not thrown.");
    } catch (ExecutionException ex) {
      Assert.assertEquals("Process cancelled.", ex.getMessage());
    }

    Mockito.verify(processMock).destroy();
    loggerInOrder.verify(messageListener).message("Running command : someCommand someOption");
  }

  private void setProcessMockOutput(List<String> expectedOutput) {
    Mockito.when(processMock.getInputStream())
        .thenReturn(new ByteArrayInputStream(Joiner.on("\n").join(expectedOutput).getBytes()));
  }

  private void verifyProcessBuilding(List<String> command) throws IOException {
    Mockito.verify(processBuilderMock).command(command);
    Mockito.verify(processBuilderMock).redirectErrorStream(true);
    Mockito.verify(processBuilderMock).start();
    Mockito.verify(processMock).getInputStream();
  }
}
