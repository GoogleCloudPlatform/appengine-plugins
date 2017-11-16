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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
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
  @Mock private InputStream mockStdOut;
  @Mock private InputStream mockStdErr;
  @Mock private AsyncStreamHandler mockStreamHandler;
  private final List<String> command = Arrays.asList("someCommand", "someOption");

  private InOrder loggerInOrder;

  @Before
  public void setup() throws IOException, InterruptedException {
    MockitoAnnotations.initMocks(this);

    Mockito.when(processBuilderFactoryMock.createProcessBuilder()).thenReturn(processBuilderMock);
    Mockito.when(processBuilderMock.start()).thenReturn(processMock);
    Mockito.when(processMock.waitFor()).thenReturn(0);
    Mockito.when(processMock.getInputStream()).thenReturn(mockStdOut);
    Mockito.when(processMock.getErrorStream()).thenReturn(mockStdErr);
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

    int result =
        new CommandExecutor()
            .setWorkingDirectory(fakeWorkingDirectory)
            .setEnvironment(environmentInput)
            .setProcessBuilderFactory(processBuilderFactoryMock)
            .run(command, mockStreamHandler, mockStreamHandler);

    verifyProcessBuilding(command);
    Mockito.verify(processBuilderMock).environment();
    Mockito.verify(processBuilderMock).directory(fakeWorkingDirectory.toFile());
    Assert.assertEquals(environmentInput, processEnvironment);

    Mockito.verify(mockStreamHandler).handleStream(mockStdOut);
    Mockito.verify(mockStreamHandler).handleStream(mockStdErr);
    Mockito.verifyNoMoreInteractions(mockStreamHandler);
  }

  @Test
  public void testRun_nonZeroExitCodePassthrough()
      throws IOException, InterruptedException, ExecutionException {

    Mockito.when(processMock.waitFor()).thenReturn(123);

    int exitCode =
        new CommandExecutor()
            .setProcessBuilderFactory(processBuilderFactoryMock)
            .run(command, mockStreamHandler, mockStreamHandler);

    Assert.assertEquals(123, exitCode);
  }

  @Test
  public void testRun_interruptedWaitingForProcess() throws IOException, InterruptedException {

    // force an interruption to simulate a cancel.
    Mockito.when(processMock.waitFor()).thenThrow(InterruptedException.class);

    try {
      new CommandExecutor()
          .setProcessBuilderFactory(processBuilderFactoryMock)
          .run(command, mockStreamHandler, mockStreamHandler);
      Assert.fail("Execution exception expected but not thrown.");
    } catch (ExecutionException ex) {
      Assert.assertEquals("Process cancelled.", ex.getMessage());
    }

    Mockito.verify(processMock).destroy();
  }

  private void verifyProcessBuilding(List<String> command) throws IOException {
    Mockito.verify(processBuilderMock).command(command);
    Mockito.verify(processBuilderMock).start();
    Mockito.verify(processMock).getInputStream();
    Mockito.verify(processMock).getErrorStream();
  }
}
