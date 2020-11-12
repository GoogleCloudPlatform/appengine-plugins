/*
 * Copyright 2017 Google LLC.
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
import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.AbstractFuture;
import com.google.common.util.concurrent.SettableFuture;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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

  @Mock private ProcessExecutor mockProcessExecutor;
  @Mock private AsyncStreamSaver mockStdoutSaver;
  @Mock private AsyncStreamSaver mockStderrSaver;
  @Mock private AsyncStreamSaverFactory mockStreamSaverFactory;

  private final SettableFuture<String> mockStdout = SettableFuture.create();
  private final SettableFuture<String> mockStderr = SettableFuture.create();
  private List<String> fakeCommand;
  private Path fakeWorkingDirectory;
  private Map<String, String> fakeEnvironment;

  private CommandCaller testCommandCaller;

  @Before
  public void setUp() throws IOException, InterruptedException {
    fakeCommand = Arrays.asList("gcloud", "test", "--option");
    fakeWorkingDirectory = testDir.getRoot().toPath();
    fakeEnvironment = ImmutableMap.of("testKey", "testValue");

    Mockito.when(mockStreamSaverFactory.newSaver())
        .thenReturn(mockStdoutSaver)
        .thenReturn(mockStderrSaver);
    Mockito.when(
            mockProcessExecutor.run(
                fakeCommand,
                fakeWorkingDirectory,
                fakeEnvironment,
                mockStdoutSaver,
                mockStderrSaver))
        .thenReturn(0);
    Mockito.when(mockStdoutSaver.getResult()).thenReturn(mockStdout);
    Mockito.when(mockStderrSaver.getResult()).thenReturn(mockStderr);

    mockStdout.set("stdout");
    mockStderr.set("stderr");

    testCommandCaller = new CommandCaller(() -> mockProcessExecutor, mockStreamSaverFactory);
  }

  private void verifyCommandExecution() throws IOException, InterruptedException {
    Mockito.verify(mockProcessExecutor)
        .run(fakeCommand, fakeWorkingDirectory, fakeEnvironment, mockStdoutSaver, mockStderrSaver);
    Mockito.verifyNoMoreInteractions(mockProcessExecutor);
  }

  @Test
  public void testCall()
      throws IOException, InterruptedException, CommandExecutionException, CommandExitException {
    Assert.assertEquals(
        "stdout", testCommandCaller.call(fakeCommand, fakeWorkingDirectory, fakeEnvironment));
    verifyCommandExecution();
  }

  @Test
  public void testCall_nonZeroExit()
      throws IOException, InterruptedException, CommandExecutionException {
    Mockito.when(
            mockProcessExecutor.run(
                fakeCommand,
                fakeWorkingDirectory,
                fakeEnvironment,
                mockStdoutSaver,
                mockStderrSaver))
        .thenReturn(10);

    try {
      testCommandCaller.call(fakeCommand, fakeWorkingDirectory, fakeEnvironment);
      Assert.fail("CommandExitException expected but not found.");
    } catch (CommandExitException ex) {
      Assert.assertEquals("Process failed with exit code: 10", ex.getMessage());
      Assert.assertEquals(10, ex.getExitCode());
      Assert.assertEquals("stdout\nstderr", ex.getErrorLog());
    }
    verifyCommandExecution();
  }

  @Test
  public void testCall_ioException()
      throws CommandExitException, InterruptedException, IOException {
    Throwable cause = new IOException("oops");
    Mockito.when(
            mockProcessExecutor.run(
                fakeCommand,
                fakeWorkingDirectory,
                fakeEnvironment,
                mockStdoutSaver,
                mockStderrSaver))
        .thenThrow(cause);

    try {
      testCommandCaller.call(fakeCommand, fakeWorkingDirectory, fakeEnvironment);
      Assert.fail("CommandExecutionException expected but not found.");
    } catch (CommandExecutionException ex) {
      Assert.assertEquals("stdout\nstderr", ex.getMessage());
    }

    verifyCommandExecution();
  }

  @Test
  public void testCall_interruptedExceptionPassthrough()
      throws CommandExecutionException, CommandExitException, InterruptedException, IOException {

    AbstractFuture<String> future =
        new AbstractFuture<String>() {
          @Override
          public String get() throws InterruptedException {
            throw new InterruptedException();
          }
        };
    Mockito.when(mockStdoutSaver.getResult()).thenReturn(future);

    try {
      testCommandCaller.call(fakeCommand, fakeWorkingDirectory, fakeEnvironment);
      Assert.fail("InterruptedException expected but not found.");
    } catch (InterruptedException ex) {
      // pass
    }

    verifyCommandExecution();
  }
}
