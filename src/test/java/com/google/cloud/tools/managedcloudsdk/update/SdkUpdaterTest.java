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
import com.google.cloud.tools.managedcloudsdk.gcloud.AsyncGcloudRunnerWrapper;
import com.google.cloud.tools.managedcloudsdk.process.CommandExitException;
import com.google.common.util.concurrent.ListeningExecutorService;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/** Tests for {@link SdkUpdater} */
public class SdkUpdaterTest {

  @Rule public TemporaryFolder testDir = new TemporaryFolder();

  @Mock private MessageListener mockMessageListener;
  @Mock private AsyncGcloudRunnerWrapper mockAsyncGcloudRunnerWrapper;

  private ListeningExecutorService testExecutorService;

  @Before
  public void setUpFakesAndMocks() throws IOException {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testUpdate_successRun() throws CommandExitException, ExecutionException, IOException {
    SdkUpdater testUpdater = new SdkUpdater(mockAsyncGcloudRunnerWrapper);
    testUpdater.update(mockMessageListener);
    Mockito.verify(mockAsyncGcloudRunnerWrapper)
        .runCommand(testUpdater.getParameters(), mockMessageListener);
  }
}
