/*
 * Copyright 2018 Google LLC. All Rights Reserved.
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

package com.google.cloud.tools.appengine.cloudsdk;

import com.google.cloud.tools.appengine.cloudsdk.process.ProcessHandler;
import java.io.File;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class GcloudTest {

  @Mock private GcloudRunner.Factory gcloudRunnerFactory;
  @Mock private CloudSdk sdk;
  @Mock private ProcessHandler processHandler;
  @Mock private File credentialFile;

  @Test
  public void testGetRunner_parametersPassedToFactory() {
    String metricsEnvironment = "env123";
    String metricsEnvironmentVersion = "ver123";
    String outputFormat = "format123";
    String showStructuredLogs = "logs123";
    new Gcloud.Builder(sdk, gcloudRunnerFactory)
        .setCredentialFile(credentialFile)
        .setMetricsEnvironment(metricsEnvironment, metricsEnvironmentVersion)
        .setOutputFormat(outputFormat)
        .setShowStructuredLogs(showStructuredLogs)
        .build()
        .getRunner(processHandler);

    Mockito.verify(gcloudRunnerFactory)
        .newRunner(
            sdk,
            metricsEnvironment,
            metricsEnvironmentVersion,
            credentialFile,
            outputFormat,
            showStructuredLogs,
            processHandler);
  }
}
