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

package com.google.cloud.tools.managedcloudsdk.gcloud;

import com.google.cloud.tools.managedcloudsdk.MessageListener;
import com.google.cloud.tools.managedcloudsdk.executors.SdkExecutorServiceFactory;
import com.google.cloud.tools.managedcloudsdk.process.CommandExitException;
import com.google.cloud.tools.managedcloudsdk.process.CommandRunner;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/** Tests for {@link com.google.cloud.tools.managedcloudsdk.gcloud.AsyncGcloudRunnerWrapper} */
public class AsyncGcloudRunnerWrapperTest {

  @Rule public TemporaryFolder testDir = new TemporaryFolder();

  @Mock private GcloudCommandFactory mockGcloudCommandFactory;
  @Mock private CommandRunner mockGcloudRunner;
  @Mock private MessageListener mockMessageListener;
  @Mock private SdkExecutorServiceFactory mockExecutorServiceFactory;
  @Mock private List<String> mockCommand;

  private ListeningExecutorService testExecutorService;

  @Before
  public void setUpFakesAndMocks() throws IOException {
    MockitoAnnotations.initMocks(this);

    testExecutorService = Mockito.spy(MoreExecutors.newDirectExecutorService());
    Mockito.when(mockExecutorServiceFactory.newExecutorService()).thenReturn(testExecutorService);
    Mockito.when(
            mockGcloudCommandFactory.newRunner(
                Mockito.<String>anyList(), Mockito.eq(mockMessageListener)))
        .thenReturn(mockGcloudRunner);
  }

  @Test
  public void testRunCommand_smokeTest()
      throws CommandExitException, ExecutionException, IOException {
    AsyncGcloudRunnerWrapper testWrapper =
        new AsyncGcloudRunnerWrapper(mockGcloudCommandFactory, mockExecutorServiceFactory);
    testWrapper.runCommand(mockCommand, mockMessageListener);

    Mockito.verify(mockExecutorServiceFactory).newExecutorService();
    Mockito.verify(testExecutorService).submit(Mockito.any(Callable.class));
    Mockito.verify(mockGcloudCommandFactory).newRunner(mockCommand, mockMessageListener);
    Mockito.verifyNoMoreInteractions(mockExecutorServiceFactory);
    Mockito.verify(mockGcloudRunner).run();
    Mockito.verifyNoMoreInteractions(mockGcloudRunner);
  }
}
