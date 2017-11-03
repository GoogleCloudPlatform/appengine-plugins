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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
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
  private final List<String> command = Arrays.asList("someCommand", "someOption");
  private final String stdout = "some output line 1\nsome output line 2\n";
  private final String stderr = "some err line 1\nsome err line 2\n";

  private InOrder loggerInOrder;

  @Before
  public void setup() throws IOException, InterruptedException {
    MockitoAnnotations.initMocks(this);

    Mockito.when(processBuilderFactoryMock.createProcessBuilder()).thenReturn(processBuilderMock);
    Mockito.when(processBuilderMock.start()).thenReturn(processMock);
    Mockito.when(processMock.waitFor()).thenReturn(0);
    Mockito.when(processMock.getInputStream()).thenReturn(new ByteArrayInputStream(stdout.getBytes()));
    Mockito.when(processMock.getErrorStream()).thenReturn(new ByteArrayInputStream(stderr.getBytes()));
    loggerInOrder = Mockito.inOrder(messageListener);
  }

  @Test
  public void testRun() throws IOException, InterruptedException, ExecutionException {
    // Mocks the environment for the processBuilderMock to put the environment map in.
    Map<String, String> environmentInput = new HashMap<>();
    environmentInput.put("ENV1", "val1");
    environmentInput.put("ENV2", "val2");
    Map<String, String> processEnvironment = new HashMap<>();
    Mockito.when(processBuilderMock.environment()).thenReturn(processEnvironment);

    Path fakeWorkingDirectory = Paths.get("/tmp/fake/working/dir");

    CommandExecutor.Result result =
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
    Assert.assertEquals(stdout, result.getStdout());
    Assert.assertEquals(stderr, result.getStderr());

    Mockito.verify(messageListener).message("Running command : someCommand someOption");
    Mockito.verify(messageListener).message("some output line 1");
    Mockito.verify(messageListener).message("some output line 2");
    Mockito.verify(messageListener).message("some err line 1");
    Mockito.verify(messageListener).message("some err line 2");
    Mockito.verifyNoMoreInteractions(messageListener);
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

    Mockito.when(processMock.waitFor()).thenReturn(123);

    CommandExecutor.Result result =
        new CommandExecutor()
            .setMessageListener(messageListener)
            .setProcessBuilderFactory(processBuilderFactoryMock)
            .run(command);

    Assert.assertEquals(123, result.getExitCode());
  }

  @Test
  public void testRun_interruptedWaitingForProcess() throws IOException, InterruptedException {

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

  private void verifyProcessBuilding(List<String> command) throws IOException {
    Mockito.verify(processBuilderMock).command(command);
    Mockito.verify(processBuilderMock).start();
    Mockito.verify(processMock).getInputStream();
    Mockito.verify(processMock).getErrorStream();
  }
}
