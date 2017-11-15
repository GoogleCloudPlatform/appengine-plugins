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
import com.google.cloud.tools.managedcloudsdk.process.AsyncStreamHandler;
import com.google.cloud.tools.managedcloudsdk.process.CommandExecutor;
import com.google.cloud.tools.managedcloudsdk.process.CommandExecutorFactory;
import com.google.common.util.concurrent.ListenableFuture;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/** Tests for {@link Updater} */
public class UpdaterTest {

  @Rule public TemporaryFolder tmp = new TemporaryFolder();

  @Mock private CommandExecutorFactory mockCommandExecutorFactory;
  @Mock private CommandExecutor mockCommandExecutor;
  @Mock private MessageListener mockMessageListener;
  @Mock private AsyncStreamHandler<Void> mockStreamHandler;
  @Mock private ListenableFuture<Void> mockResult;

  private Path fakeGcloud;

  @Before
  public void setUpFakesAndMocks() throws IOException, ExecutionException, InterruptedException {
    MockitoAnnotations.initMocks(this);

    Mockito.when(mockCommandExecutorFactory.newCommandExecutor()).thenReturn(mockCommandExecutor);
    Mockito.when(
            mockCommandExecutor.run(
                Mockito.<String>anyList(),
                Mockito.any(AsyncStreamHandler.class),
                Mockito.any(AsyncStreamHandler.class)))
        .thenReturn(0);
    Mockito.when(mockStreamHandler.getResult()).thenReturn(mockResult);
    Mockito.when(mockResult.get()).thenReturn(null);

    fakeGcloud = tmp.newFile("gcloud").toPath();
  }

  @Test
  public void testCall() throws Exception {
    Updater updater =
        new Updater(
            fakeGcloud,
            mockMessageListener,
            mockCommandExecutorFactory,
            mockStreamHandler,
            mockStreamHandler);
    updater.update();

    Mockito.verify(mockCommandExecutor)
        .run(getExpectedCommand(fakeGcloud), mockStreamHandler, mockStreamHandler);
    Mockito.verifyNoMoreInteractions(mockCommandExecutor);
  }

  @Test
  public void testCall_nonZeroExit() throws Exception {
    Mockito.when(
            mockCommandExecutor.run(
                Mockito.<String>anyList(),
                Mockito.eq(mockStreamHandler),
                Mockito.eq(mockStreamHandler)))
        .thenReturn(10);

    Updater updater =
        new Updater(
            fakeGcloud,
            mockMessageListener,
            mockCommandExecutorFactory,
            mockStreamHandler,
            mockStreamHandler);
    try {
      updater.update();
      Assert.fail("ExecutionException expected but not found.");
    } catch (ExecutionException ex) {
      Assert.assertEquals("Updater exited with non-zero exit code: 10", ex.getMessage());
    }
    Mockito.verify(mockCommandExecutor)
        .run(getExpectedCommand(fakeGcloud), mockStreamHandler, mockStreamHandler);
    Mockito.verifyNoMoreInteractions(mockCommandExecutor);
  }

  private List<String> getExpectedCommand(Path sdkHome) {
    List<String> command = new ArrayList<>(6);
    command.add(fakeGcloud.toString());
    command.add("components");
    command.add("update");
    command.add("--quiet");
    return command;
  }
}
