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

import com.google.cloud.tools.managedcloudsdk.MessageListener;
import com.google.cloud.tools.managedcloudsdk.executors.SdkExecutorServiceFactory;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/** Tests for {@link AsyncCommandWrapper} */
public class AsyncCommandWrapperTest {

  @Rule public TemporaryFolder testDir = new TemporaryFolder();

  @Mock private CommandRunner mockCommandRunner;
  @Mock private CommandCaller mockCommandCaller;
  @Mock private MessageListener mockMessageListener;
  @Mock private SdkExecutorServiceFactory mockExecutorServiceFactory;
  @Mock private List<String> mockCommand;

  private ListeningExecutorService testExecutorService;

  @Before
  public void setUpFakesAndMocks()
      throws CommandExecutionException, CommandExitException, InterruptedException {
    MockitoAnnotations.initMocks(this);

    testExecutorService = Mockito.spy(MoreExecutors.newDirectExecutorService());
    Mockito.when(mockExecutorServiceFactory.newExecutorService()).thenReturn(testExecutorService);
    Mockito.when(mockCommandCaller.execute()).thenReturn("test-string");
  }

  @Test
  public void testWrap_commandRunner()
      throws CommandExitException, CommandExecutionException, InterruptedException {
    AsyncCommandWrapper testWrapper = new AsyncCommandWrapper(mockExecutorServiceFactory);
    testWrapper.execute(mockCommandRunner);

    Mockito.verify(mockExecutorServiceFactory).newExecutorService();
    Mockito.verify(testExecutorService).submit(Mockito.any(Callable.class));
    Mockito.verifyNoMoreInteractions(mockExecutorServiceFactory);
    Mockito.verify(mockCommandRunner).execute();
    Mockito.verifyNoMoreInteractions(mockCommandRunner);
  }

  @Test
  public void testWrap_commandCaller()
      throws CommandExitException, InterruptedException, CommandExecutionException,
          ExecutionException {
    AsyncCommandWrapper testWrapper = new AsyncCommandWrapper(mockExecutorServiceFactory);
    ListenableFuture<String> result = testWrapper.execute(mockCommandCaller);

    Mockito.verify(mockExecutorServiceFactory).newExecutorService();
    Mockito.verify(testExecutorService).submit(Mockito.any(Callable.class));
    Mockito.verifyNoMoreInteractions(mockExecutorServiceFactory);
    Mockito.verify(mockCommandCaller).execute();
    Mockito.verifyNoMoreInteractions(mockCommandCaller);

    Assert.assertEquals("test-string", result.get());
  }
}
