/*
 * Copyright 2016 Google Inc.
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

package com.google.cloud.tools.app.impl.cloudsdk;

import com.google.cloud.tools.app.impl.cloudsdk.internal.process.ProcessRunnerException;
import com.google.cloud.tools.app.impl.cloudsdk.internal.sdk.CloudSdk;
import com.google.cloud.tools.app.impl.config.DefaultLogsConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link CloudSdkAppEngineLogs}
 */
@RunWith(MockitoJUnitRunner.class)
public class CloudSdkAppEngineLogsTest {

  @Mock
  private CloudSdkCli sdkCli;

  @Test
  public void readTest() throws ProcessRunnerException {
    CloudSdkAppEngineLogs appEngineLogs = new CloudSdkAppEngineLogs(sdkCli);
    DefaultLogsConfiguration configuration = new DefaultLogsConfiguration();
    configuration.setLevel("warning");
    configuration.setVersion("v1");
    configuration.setService("myService");
    configuration.setLimit(10);

    appEngineLogs.read(configuration);

    List<String> args =
        Arrays.asList("logs", "read", "--level", "warning", "--version", "v1", "--service",
            "myService", "--limit", "10");

    verify(sdkCli, times(1)).runGcloudAppCommand(eq(args));
  }
}
