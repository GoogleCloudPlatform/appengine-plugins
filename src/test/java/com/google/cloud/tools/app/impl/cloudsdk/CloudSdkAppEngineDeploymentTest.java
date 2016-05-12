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

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.cloud.tools.app.api.AppEngineException;
import com.google.cloud.tools.app.impl.cloudsdk.internal.process.ProcessRunnerException;
import com.google.cloud.tools.app.impl.config.DefaultDeployConfiguration;
import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for {@link CloudSdkAppEngineDeployment}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CloudSdkAppEngineDeploymentTest {

  @Mock
  private CloudSdkCli sdkCli;

  @Rule
  public TemporaryFolder tmpDir = new TemporaryFolder();

  private File appYaml1;
  private File appYaml2;

  private CloudSdkAppEngineDeployment deployment;

  @Before
  public void setUp() throws IOException {
    appYaml1 = tmpDir.newFile("app1.yaml");
    appYaml2 = tmpDir.newFile("app2.yaml");
    deployment = new CloudSdkAppEngineDeployment(sdkCli);
  }

  @Test
  public void testNewDeployAction_allFlags() throws AppEngineException, ProcessRunnerException {

    DefaultDeployConfiguration configuration = new DefaultDeployConfiguration();
    configuration.setDeployables(Arrays.asList(appYaml1));
    configuration.setBucket("gs://a-bucket");
    configuration.setDockerBuild("cloud");
    configuration.setForce(true);
    configuration.setImageUrl("imageUrl");
    configuration.setProject("project");
    configuration.setPromote(true);
    configuration.setServer("appengine.google.com");
    configuration.setStopPreviousVersion(true);
    configuration.setVersion("v1");

    deployment.deploy(configuration);

    List<String> expectedCommand = ImmutableList
        .of("deploy", appYaml1.toString(), "--bucket", "gs://a-bucket", "--docker-build", "cloud",
            "--force", "--image-url", "imageUrl", "--project", "project", "--promote",
            "--server", "appengine.google.com", "--stop-previous-version",
            "--version", "v1");

    verify(sdkCli, times(1)).runGcloudAppCommand(eq(expectedCommand));
  }

  @Test
  public void testNewDeployAction_booleanFlags() throws AppEngineException, ProcessRunnerException {
    DefaultDeployConfiguration configuration = new DefaultDeployConfiguration();
    configuration.setDeployables(Arrays.asList(appYaml1));
    configuration.setForce(false);
    configuration.setPromote(false);
    configuration.setStopPreviousVersion(false);

    deployment.deploy(configuration);

    List<String> expectedCommand = ImmutableList
        .of("deploy", appYaml1.toString(), "--no-force", "--no-promote",
            "--no-stop-previous-version");

    verify(sdkCli, times(1)).runGcloudAppCommand(eq(expectedCommand));
  }

  @Test
  public void testNewDeployAction_noFlags() throws AppEngineException, ProcessRunnerException {

    DefaultDeployConfiguration configuration = new DefaultDeployConfiguration();
    configuration.setDeployables(Arrays.asList(appYaml1));

    List<String> expectedCommand = ImmutableList.of("deploy", appYaml1.toString());

    deployment.deploy(configuration);

    verify(sdkCli, times(1)).runGcloudAppCommand(eq(expectedCommand));
  }

  @Test
  public void testNewDeployAction_multipleDeployables()
      throws AppEngineException, ProcessRunnerException {

    DefaultDeployConfiguration configuration = new DefaultDeployConfiguration();
    configuration.setDeployables(Arrays.asList(appYaml1, appYaml2));

    deployment.deploy(configuration);

    List<String> expectedCommand = ImmutableList
        .of("deploy", appYaml1.toString(), appYaml2.toString());

    verify(sdkCli, times(1)).runGcloudAppCommand(eq(expectedCommand));

  }

}
