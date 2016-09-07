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

package com.google.cloud.tools.appengine.experimental.internal.cloudsdk;

import static org.mockito.Mockito.when;

import com.google.common.collect.Maps;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class CloudSdkAppEngineRequestFactoryTest {

  @Rule
  public TemporaryFolder testFolder = new TemporaryFolder();

  private Path fakeGcloud;
  private Path fakeCredentialFile;

  @Mock
  private CloudSdkV2 sdk;

  @Before
  public void configureMocks() throws IOException {
    fakeGcloud = testFolder.newFile("gcloud").toPath();
    fakeCredentialFile = testFolder.newFile("credentials").toPath();
    when(sdk.getGCloudPath()).thenReturn(fakeGcloud);
  }

  @Test
  public void testGetEnvironment_nothingSet() {
    Map<String, String> env = new CloudSdkAppEngineRequestFactory(sdk, null, null, null)
        .getEnvironment();

    Map<String, String> expected = Maps.newHashMap();

    Assert.assertEquals(expected, env);
  }

  @Test
  public void testGetEnvironment_metricEnvironmentSet() {
    Map<String, String> env = new CloudSdkAppEngineRequestFactory(sdk, null, "test-environment",
        "test-version").getEnvironment();

    Map<String, String> expected = Maps.newHashMap();
    expected.put("CLOUDSDK_METRICS_ENVIRONMENT", "test-environment");
    expected.put("CLOUDSDK_METRICS_ENVIRONMENT_VERSION", "test-version");

    Assert.assertEquals(expected, env);
  }

  @Test
  public void testGetEnvironment_credentialFile() {

    Map<String, String> env = new CloudSdkAppEngineRequestFactory(sdk, fakeCredentialFile, null,
        null).getEnvironment();

    Map<String, String> expected = Maps.newHashMap();
    expected.put("CLOUDSDK_APP_USE_GSUTIL", "0");

    Assert.assertEquals(expected, env);
  }

  @Test
  public void testGetEnvironment_all() {

    Map<String, String> env = new CloudSdkAppEngineRequestFactory(sdk, fakeCredentialFile,
        "test-environment", "test-version").getEnvironment();

    Map<String, String> expected = Maps.newHashMap();
    expected.put("CLOUDSDK_METRICS_ENVIRONMENT", "test-environment");
    expected.put("CLOUDSDK_METRICS_ENVIRONMENT_VERSION", "test-version");
    expected.put("CLOUDSDK_APP_USE_GSUTIL", "0");

    Assert.assertEquals(expected, env);
  }

  @Test
  public void testGetAppCommand_defaults() {

    List<String> command = new CloudSdkAppEngineRequestFactory(sdk, null, null, null)
        .getAppCommand(Arrays.asList("command", "--param=value"));

    List<String> expected = Arrays
        .asList(fakeGcloud.toString(), "app", "command", "--param=value", "--format=yaml",
            "--quiet");

    Assert.assertEquals(expected, command);
  }

  @Test
  public void testGetAppCommand_credentialFile() {

    List<String> command = new CloudSdkAppEngineRequestFactory(sdk, fakeCredentialFile, null, null)
        .getAppCommand(Arrays.asList("command", "--param=value"));

    List<String> expected = Arrays
        .asList(fakeGcloud.toString(), "app", "command", "--param=value", "--format=yaml",
            "--quiet", "--credential-file-override", fakeCredentialFile.toString());

    Assert.assertEquals(expected, command);
  }
}