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

package com.google.cloud.tools.managedcloudsdk.components;

import com.google.cloud.tools.managedcloudsdk.MessageListener;
import com.google.cloud.tools.managedcloudsdk.executors.SdkExecutorServiceFactory;
import com.google.cloud.tools.managedcloudsdk.gcloud.GcloudCommandFactory;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import java.io.IOException;
import java.util.concurrent.Callable;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/** Tests for {@link SdkComponentInstaller} */
public class SdkComponentInstallerTest {

  @Rule public TemporaryFolder testDir = new TemporaryFolder();

  @Mock private GcloudCommandFactory mockGcloudCommandFactory;
  @Mock private MessageListener mockMessageListener;
  @Mock private SdkExecutorServiceFactory mockExecutorServiceFactory;

  private ListeningExecutorService testExecutorService;
  private SdkComponent testComponent = SdkComponent.APP_ENGINE_JAVA;

  @Before
  public void setUpFakesAndMocks() throws IOException {
    MockitoAnnotations.initMocks(this);

    testExecutorService = Mockito.spy(MoreExecutors.newDirectExecutorService());
    Mockito.when(mockExecutorServiceFactory.newExecutorService()).thenReturn(testExecutorService);
  }

  @Test
  public void testInstallComponent_successRun() {
    SdkComponentInstaller testInstaller =
        new SdkComponentInstaller(mockGcloudCommandFactory, mockExecutorServiceFactory);
    testInstaller.installComponent(testComponent, mockMessageListener);

    Mockito.verify(mockExecutorServiceFactory).newExecutorService();
    Mockito.verify(testExecutorService).submit(Mockito.any(Callable.class));
    Mockito.verify(mockGcloudCommandFactory)
        .newCommand(testInstaller.getParameters(testComponent), mockMessageListener);
  }
}
