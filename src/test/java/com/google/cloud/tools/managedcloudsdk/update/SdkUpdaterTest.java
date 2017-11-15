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
import com.google.cloud.tools.managedcloudsdk.executors.SdkExecutorServiceFactory;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Callable;
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

  @Mock private UpdaterFactory updaterFactory;
  @Mock private MessageListener messageListener;
  @Mock private SdkExecutorServiceFactory executorServiceFactory;

  private ListeningExecutorService testExecutorService;
  private Path fakeGcloud;

  @Before
  public void setUpFakesAndMocks() throws IOException {
    MockitoAnnotations.initMocks(this);

    fakeGcloud = testDir.newFolder("gcloud").toPath();
    testExecutorService = Mockito.spy(MoreExecutors.newDirectExecutorService());
    Mockito.when(executorServiceFactory.newExecutorService()).thenReturn(testExecutorService);
  }

  @Test
  public void testUpdate_successRun() {
    SdkUpdater testUpdater = new SdkUpdater(fakeGcloud, updaterFactory, executorServiceFactory);
    testUpdater.update(messageListener);

    Mockito.verify(executorServiceFactory).newExecutorService();
    Mockito.verify(testExecutorService).submit(Mockito.any(Callable.class));
    Mockito.verify(updaterFactory).newUpdater(fakeGcloud, messageListener);
  }
}
