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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.cloud.tools.appengine.api.AppEngineException;
import com.google.cloud.tools.appengine.api.versions.DefaultVersionsListConfiguration;
import com.google.cloud.tools.appengine.api.versions.DefaultVersionsSelectionConfiguration;
import com.google.cloud.tools.appengine.cloudsdk.process.ProcessHandlerException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/** Unit tests for {@link com.google.cloud.tools.appengine.cloudsdk.CloudSdkAppEngineVersions} */
@RunWith(MockitoJUnitRunner.class)
public class CloudSdkAppEngineVersionsTest {

  @Mock private GcloudRunner gcloudRunner;
  CloudSdkAppEngineVersions appEngineVersion;

  @Before
  public void setUp() {
    appEngineVersion = new CloudSdkAppEngineVersions(gcloudRunner);
  }

  @Test
  public void startTest() throws ProcessHandlerException, AppEngineException, IOException {
    appEngineVersion.start(getVersionConfig());

    List<String> args =
        Arrays.asList(
            "app",
            "versions",
            "start",
            "v1",
            "v2",
            "--service",
            "myService",
            "--project",
            "myProject");

    verify(gcloudRunner, times(1)).run(eq(args), isNull());
  }

  @Test
  public void stopTest() throws ProcessHandlerException, AppEngineException, IOException {
    CloudSdkAppEngineVersions appEngineVersion = new CloudSdkAppEngineVersions(gcloudRunner);
    appEngineVersion.stop(getVersionConfig());

    List<String> args =
        Arrays.asList(
            "app",
            "versions",
            "stop",
            "v1",
            "v2",
            "--service",
            "myService",
            "--project",
            "myProject");

    verify(gcloudRunner, times(1)).run(eq(args), isNull());
  }

  @Test
  public void deleteTest() throws ProcessHandlerException, AppEngineException, IOException {
    CloudSdkAppEngineVersions appEngineVersion = new CloudSdkAppEngineVersions(gcloudRunner);
    appEngineVersion.delete(getVersionConfig());

    List<String> args =
        Arrays.asList(
            "app",
            "versions",
            "delete",
            "v1",
            "v2",
            "--service",
            "myService",
            "--project",
            "myProject");

    verify(gcloudRunner, times(1)).run(eq(args), isNull());
  }

  @Test
  public void listTest_doHideNoTraffic()
      throws ProcessHandlerException, AppEngineException, IOException {
    CloudSdkAppEngineVersions appEngineVersion = new CloudSdkAppEngineVersions(gcloudRunner);
    appEngineVersion.list(getListConfig(true));

    List<String> args =
        Arrays.asList(
            "app",
            "versions",
            "list",
            "--service",
            "myService",
            "--hide-no-traffic",
            "--project",
            "myProject");

    verify(gcloudRunner, times(1)).run(eq(args), isNull());
  }

  @Test
  public void listTest_dontHideNoTraffic()
      throws ProcessHandlerException, AppEngineException, IOException {
    CloudSdkAppEngineVersions appEngineVersion = new CloudSdkAppEngineVersions(gcloudRunner);
    appEngineVersion.list(getListConfig(false));

    List<String> args =
        Arrays.asList(
            "app",
            "versions",
            "list",
            "--service",
            "myService",
            "--no-hide-no-traffic",
            "--project",
            "myProject");

    verify(gcloudRunner, times(1)).run(eq(args), isNull());
  }

  private static DefaultVersionsSelectionConfiguration getVersionConfig() {
    DefaultVersionsSelectionConfiguration configuration =
        new DefaultVersionsSelectionConfiguration();
    configuration.setVersions(Arrays.asList("v1", "v2"));
    configuration.setService("myService");
    configuration.setProjectId("myProject");
    return configuration;
  }

  private static DefaultVersionsListConfiguration getListConfig(boolean hideNoTraffic) {
    DefaultVersionsListConfiguration listConfiguration = new DefaultVersionsListConfiguration();
    listConfiguration.setService("myService");
    listConfiguration.setProjectId("myProject");
    listConfiguration.setHideNoTraffic(hideNoTraffic);
    return listConfiguration;
  }
}
