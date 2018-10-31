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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.cloud.tools.appengine.api.AppEngineException;
import com.google.cloud.tools.appengine.api.deploy.DeployConfiguration;
import com.google.cloud.tools.appengine.api.deploy.DeployProjectConfigurationConfiguration;
import com.google.cloud.tools.appengine.cloudsdk.process.ProcessHandlerException;
import com.google.cloud.tools.test.utils.SpyVerifier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/** Unit tests for {@link CloudSdkAppEngineDeployment}. */
@RunWith(MockitoJUnitRunner.class)
public class CloudSdkAppEngineDeploymentTest {

  @Rule public TemporaryFolder tmpDir = new TemporaryFolder();

  private Path appYaml1;
  private Path appYaml2;
  private Path stagingDirectory;

  private CloudSdkAppEngineDeployment deployment;

  @Mock private CloudSdkAppEngineDeployment mockDeployment;
  @Mock private DeployProjectConfigurationConfiguration mockProjectConfigurationConfiguration;
  @Mock private GcloudRunner gcloudRunner;

  @Before
  public void setUp() throws IOException {
    appYaml1 = tmpDir.newFile("app1.yaml").toPath();
    appYaml2 = tmpDir.newFile("app2.yaml").toPath();
    stagingDirectory = tmpDir.newFolder("appengine-staging").toPath();
    deployment = new CloudSdkAppEngineDeployment(gcloudRunner);
  }

  @Test
  public void testNullSdk() {
    try {
      new CloudSdkAppEngineDeployment(null);
      Assert.fail("allowed null runner");
    } catch (NullPointerException expected) {
      // pass
    }
  }

  @Test
  public void testDeploy_allFlags() throws Exception {

    DeployConfiguration configuration =
        Mockito.spy(
            DeployConfiguration.builder(Collections.singletonList(appYaml1))
                .bucket("gs://a-bucket")
                .imageUrl("imageUrl")
                .projectId("project")
                .promote(true)
                .server("appengine.google.com")
                .stopPreviousVersion(true)
                .version("v1")
                .build());

    SpyVerifier.newVerifier(configuration).verifyAllValuesNotNull();

    deployment.deploy(configuration);

    List<String> expectedCommand =
        ImmutableList.of(
            "app",
            "deploy",
            appYaml1.toString(),
            "--bucket",
            "gs://a-bucket",
            "--image-url",
            "imageUrl",
            "--promote",
            "--server",
            "appengine.google.com",
            "--stop-previous-version",
            "--version",
            "v1",
            "--project",
            "project");

    verify(gcloudRunner, times(1)).run(eq(expectedCommand), isNull());

    SpyVerifier.newVerifier(configuration)
        .verifyDeclaredGetters(ImmutableMap.of("getDeployables", 5));
  }

  @Test
  public void testDeploy_booleanFlags()
      throws AppEngineException, ProcessHandlerException, IOException {
    DeployConfiguration configuration =
        DeployConfiguration.builder(Collections.singletonList(appYaml1))
            .promote(false)
            .stopPreviousVersion(false)
            .build();

    deployment.deploy(configuration);

    List<String> expectedCommand =
        ImmutableList.of(
            "app", "deploy", appYaml1.toString(), "--no-promote", "--no-stop-previous-version");

    verify(gcloudRunner, times(1)).run(eq(expectedCommand), isNull());
  }

  @Test
  public void testDeploy_noFlags() throws AppEngineException, ProcessHandlerException, IOException {

    DeployConfiguration configuration =
        DeployConfiguration.builder(Collections.singletonList(appYaml1)).build();

    List<String> expectedCommand = ImmutableList.of("app", "deploy", appYaml1.toString());

    deployment.deploy(configuration);

    verify(gcloudRunner, times(1)).run(eq(expectedCommand), isNull());
  }

  @Test
  public void testDeploy_dir() throws AppEngineException, ProcessHandlerException, IOException {

    DeployConfiguration configuration =
        DeployConfiguration.builder(Collections.singletonList(stagingDirectory)).build();

    List<String> expectedCommand = ImmutableList.of("app", "deploy");

    deployment.deploy(configuration);

    verify(gcloudRunner, times(1)).run(eq(expectedCommand), eq(stagingDirectory));
  }

  @Test
  public void testDeploy_multipleDeployables()
      throws AppEngineException, ProcessHandlerException, IOException {

    DeployConfiguration configuration =
        DeployConfiguration.builder(Arrays.asList(appYaml1, appYaml2)).build();

    deployment.deploy(configuration);

    List<String> expectedCommand =
        ImmutableList.of("app", "deploy", appYaml1.toString(), appYaml2.toString());

    verify(gcloudRunner, times(1)).run(eq(expectedCommand), isNull());
  }

  @Test
  public void testDeployCron() throws Exception {
    Mockito.doCallRealMethod()
        .when(mockDeployment)
        .deployCron(mockProjectConfigurationConfiguration);
    mockDeployment.deployCron(mockProjectConfigurationConfiguration);
    verify(mockDeployment).deployConfig("cron.yaml", mockProjectConfigurationConfiguration);
  }

  @Test
  public void testDeployDispatch() throws Exception {
    Mockito.doCallRealMethod()
        .when(mockDeployment)
        .deployDispatch(mockProjectConfigurationConfiguration);
    mockDeployment.deployDispatch(mockProjectConfigurationConfiguration);
    verify(mockDeployment).deployConfig("dispatch.yaml", mockProjectConfigurationConfiguration);
  }

  @Test
  public void testDeployDos() throws Exception {
    Mockito.doCallRealMethod()
        .when(mockDeployment)
        .deployDos(mockProjectConfigurationConfiguration);
    mockDeployment.deployDos(mockProjectConfigurationConfiguration);
    verify(mockDeployment).deployConfig("dos.yaml", mockProjectConfigurationConfiguration);
  }

  @Test
  public void testDeployIndex() throws Exception {
    Mockito.doCallRealMethod()
        .when(mockDeployment)
        .deployIndex(mockProjectConfigurationConfiguration);
    mockDeployment.deployIndex(mockProjectConfigurationConfiguration);
    verify(mockDeployment).deployConfig("index.yaml", mockProjectConfigurationConfiguration);
  }

  @Test
  public void testDeployQueue() throws Exception {
    Mockito.doCallRealMethod()
        .when(mockDeployment)
        .deployQueue(mockProjectConfigurationConfiguration);
    mockDeployment.deployQueue(mockProjectConfigurationConfiguration);
    verify(mockDeployment).deployConfig("queue.yaml", mockProjectConfigurationConfiguration);
  }

  /**
   * This test uses a fake config.yaml on purpose, In the real world, that means it will be
   * interpreted as an app.yaml. The method under test has no knowledge of which configs are valid
   * and which aren't.
   */
  @Test
  public void testDeployConfig() throws Exception {
    File testConfigYaml = tmpDir.newFile("testconfig.yaml");
    DeployProjectConfigurationConfiguration configuration =
        DeployProjectConfigurationConfiguration.builder(tmpDir.getRoot().toPath())
            .server("appengine.google.com")
            .projectId("project")
            .build();
    deployment.deployConfig("testconfig.yaml", configuration);

    List<String> expectedCommand =
        ImmutableList.of(
            "app",
            "deploy",
            testConfigYaml.toString(),
            "--server",
            "appengine.google.com",
            "--project",
            "project");

    verify(gcloudRunner, times(1)).run(eq(expectedCommand), isNull());
  }

  @Test
  public void testDeployConfig_doesNotExist() throws AppEngineException {
    File testConfigYaml = new File(tmpDir.getRoot(), "testconfig.yaml");
    assertFalse(testConfigYaml.exists());
    DeployProjectConfigurationConfiguration configuration =
        DeployProjectConfigurationConfiguration.builder(tmpDir.getRoot().toPath()).build();
    try {
      deployment.deployConfig("testconfig.yaml", configuration);
      fail();
    } catch (IllegalArgumentException ex) {
      assertEquals(testConfigYaml.toString() + " does not exist.", ex.getMessage());
    }
  }
}
