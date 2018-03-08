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

import com.google.cloud.tools.appengine.api.AppEngineException;
import com.google.cloud.tools.appengine.api.versions.DefaultVersionsListConfiguration;
import com.google.cloud.tools.appengine.api.versions.DefaultVersionsSelectionConfiguration;
import com.google.cloud.tools.appengine.cloudsdk.internal.process.ProcessRunnerException;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/** Unit tests for {@link com.google.cloud.tools.appengine.cloudsdk.CloudSdkAppEngineVersions} */
@RunWith(MockitoJUnitRunner.class)
public class CloudSdkAppEngineVersionsTest {

  @Mock private CloudSdk sdk;

  @Test
  public void startTest() throws ProcessRunnerException, AppEngineException {
    CloudSdkAppEngineVersions appEngineVersion = new CloudSdkAppEngineVersions(sdk);
    appEngineVersion.start(getVersionConfig());

    List<String> args =
        Arrays.asList(
            "versions", "start", "v1", "v2", "--service", "myService", "--project", "myProject");

    verify(sdk, times(1)).runAppCommand(eq(args));
  }

  @Test
  public void stopTest() throws ProcessRunnerException, AppEngineException {
    CloudSdkAppEngineVersions appEngineVersion = new CloudSdkAppEngineVersions(sdk);
    appEngineVersion.stop(getVersionConfig());

    List<String> args =
        Arrays.asList(
            "versions", "stop", "v1", "v2", "--service", "myService", "--project", "myProject");

    verify(sdk, times(1)).runAppCommand(eq(args));
  }

  @Test
  public void deleteTest() throws ProcessRunnerException, AppEngineException {
    CloudSdkAppEngineVersions appEngineVersion = new CloudSdkAppEngineVersions(sdk);
    appEngineVersion.delete(getVersionConfig());

    List<String> args =
        Arrays.asList(
            "versions", "delete", "v1", "v2", "--service", "myService", "--project", "myProject");

    verify(sdk, times(1)).runAppCommand(eq(args));
  }

  @Test
  public void listTest_doHideNoTraffic() throws ProcessRunnerException, AppEngineException {
    CloudSdkAppEngineVersions appEngineVersion = new CloudSdkAppEngineVersions(sdk);
    appEngineVersion.list(getListConfig(true));

    List<String> args =
        Arrays.asList(
            "versions",
            "list",
            "--service",
            "myService",
            "--hide-no-traffic",
            "--project",
            "myProject");

    verify(sdk, times(1)).runAppCommand(eq(args));
  }

  @Test
  public void listTest_dontHideNoTraffic() throws ProcessRunnerException, AppEngineException {
    CloudSdkAppEngineVersions appEngineVersion = new CloudSdkAppEngineVersions(sdk);
    appEngineVersion.list(getListConfig(false));

    List<String> args =
        Arrays.asList(
            "versions",
            "list",
            "--service",
            "myService",
            "--no-hide-no-traffic",
            "--project",
            "myProject");

    verify(sdk, times(1)).runAppCommand(eq(args));
  }

  private static DefaultVersionsSelectionConfiguration getVersionConfig() {
    DefaultVersionsSelectionConfiguration configuration =
        new DefaultVersionsSelectionConfiguration();
    configuration.setVersions(Arrays.asList("v1", "v2"));
    configuration.setService("myService");
    configuration.setProject("myProject");
    return configuration;
  }

  private static DefaultVersionsListConfiguration getListConfig(boolean hideNoTraffic) {
    DefaultVersionsListConfiguration listConfiguration = new DefaultVersionsListConfiguration();
    listConfiguration.setService("myService");
    listConfiguration.setProject("myProject");
    listConfiguration.setHideNoTraffic(hideNoTraffic);
    return listConfiguration;
  }
}
