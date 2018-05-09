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
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/** Tests for ProcessExecutor */
@RunWith(MockitoJUnitRunner.class)
public class ProcessExecutorTest {

  @Mock private ProcessExecutor.ProcessBuilderFactory mockProcessBuilderFactory;
  @Mock private ProcessBuilder mockProcessBuilder;
  @Mock private Process mockProcess;
  @Mock private InputStream mockStdOut;
  @Mock private InputStream mockStdErr;
  @Mock private AsyncStreamHandler mockStreamHandler;
  private final List<String> command = Arrays.asList("someCommand", "someOption");

  @Before
  public void setup() throws IOException, InterruptedException {
    Mockito.when(mockProcessBuilderFactory.createProcessBuilder()).thenReturn(mockProcessBuilder);
    Mockito.when(mockProcessBuilder.start()).thenReturn(mockProcess);
    Mockito.when(mockProcess.waitFor()).thenReturn(0);
    Mockito.when(mockProcess.getInputStream()).thenReturn(mockStdOut);
    Mockito.when(mockProcess.getErrorStream()).thenReturn(mockStdErr);
  }

  @Test
  public void testRun() throws IOException, InterruptedException {
    // Mocks the environment for the mockProcessBuilder to put the environment map in.
    Map<String, String> environmentInput = new HashMap<>();
    environmentInput.put("ENV1", "val1");
    environmentInput.put("ENV2", "val2");
    Map<String, String> processEnvironment = new HashMap<>();
    Mockito.when(mockProcessBuilder.environment()).thenReturn(processEnvironment);

    Path fakeWorkingDirectory = Paths.get("/tmp/fake/working/dir");

    new ProcessExecutor()
        .setProcessBuilderFactory(mockProcessBuilderFactory)
        .run(command, fakeWorkingDirectory, environmentInput, mockStreamHandler, mockStreamHandler);

    verifyProcessBuilding(command);
    Mockito.verify(mockProcessBuilder).environment();
    Mockito.verify(mockProcessBuilder).directory(fakeWorkingDirectory.toFile());
    Assert.assertEquals(environmentInput, processEnvironment);

    Mockito.verify(mockStreamHandler).handleStream(mockStdOut);
    Mockito.verify(mockStreamHandler).handleStream(mockStdErr);
    Mockito.verifyNoMoreInteractions(mockStreamHandler);
  }

  @Test
  public void testRun_nonZeroExitCodePassthrough() throws IOException, InterruptedException {

    Mockito.when(mockProcess.waitFor()).thenReturn(123);

    int exitCode =
        new ProcessExecutor()
            .setProcessBuilderFactory(mockProcessBuilderFactory)
            .run(command, null, null, mockStreamHandler, mockStreamHandler);

    Assert.assertEquals(123, exitCode);
  }

  @Test
  public void testRun_interruptedWaitingForProcess() throws IOException, InterruptedException {

    // force an interruption to simulate a cancel.
    Mockito.when(mockProcess.waitFor()).thenThrow(InterruptedException.class);

    try {
      new ProcessExecutor()
          .setProcessBuilderFactory(mockProcessBuilderFactory)
          .run(command, null, null, mockStreamHandler, mockStreamHandler);
      Assert.fail("Interrupted exception expected but not thrown.");
    } catch (InterruptedException ex) {
      // pass
    }

    Mockito.verify(mockProcess).destroy();
  }

  private void verifyProcessBuilding(List<String> command) throws IOException {
    Mockito.verify(mockProcessBuilder).command(command);
    Mockito.verify(mockProcessBuilder).start();
    Mockito.verify(mockProcess).getInputStream();
    Mockito.verify(mockProcess).getErrorStream();
  }
}
