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

package com.google.cloud.tools.appengine.cloudsdk;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.cloud.tools.appengine.api.services.DefaultTrafficSplitConfiguration;
import com.google.cloud.tools.appengine.cloudsdk.internal.process.ProcessRunnerException;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/** Unit tests for {@link com.google.cloud.tools.appengine.cloudsdk.CloudSdkAppEngineServices} */
@RunWith(MockitoJUnitRunner.class)
public class CloudSdkAppEngineServicesTest {

  @Mock private CloudSdk sdk;

  @Test
  public void setTrafficTest() throws ProcessRunnerException {
    CloudSdkAppEngineServices appEngineService = new CloudSdkAppEngineServices(sdk);

    DefaultTrafficSplitConfiguration configuration = new DefaultTrafficSplitConfiguration();
    Map<String, Double> versionToSplitMap = new LinkedHashMap<>();
    versionToSplitMap.put("v1", 0.3);
    versionToSplitMap.put("v2", 0.7);

    configuration.setServices(Collections.singletonList("myService"));
    configuration.setVersionToTrafficSplit(versionToSplitMap);
    configuration.setProject("myProject");

    appEngineService.setTraffic(configuration);

    List<String> args =
        Arrays.asList(
            "services",
            "set-traffic",
            "myService",
            "--splits",
            "v1=0.3,v2=0.7",
            "--project",
            "myProject");

    verify(sdk, times(1)).runAppCommand(eq(args));
  }
}
