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

import com.google.cloud.tools.appengine.api.AppEngineException;
import com.google.cloud.tools.appengine.api.deploy.DefaultDeployConfiguration;
import com.google.cloud.tools.appengine.api.deploy.DefaultDeployCronConfiguration;
import com.google.cloud.tools.appengine.api.deploy.DefaultDeployDispatchConfiguration;
import com.google.cloud.tools.appengine.api.deploy.DefaultDeployDosConfiguration;
import com.google.cloud.tools.appengine.api.deploy.DefaultDeployIndexesConfiguration;
import com.google.cloud.tools.appengine.api.deploy.DefaultDeployQueueConfiguration;
import com.google.cloud.tools.appengine.cloudsdk.internal.process.ProcessRunnerException;
import com.google.cloud.tools.test.utils.SpyVerifier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link CloudSdkAppEngineDeployment}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CloudSdkAppEngineDeploymentTest {

  @Mock
  private CloudSdk sdk;

  @Rule
  public TemporaryFolder tmpDir = new TemporaryFolder();

  @Rule
  public final ExpectedException exception = ExpectedException.none();

  private File appYaml1;
  private File appYaml2;
  private File stagingDirectory;

  private CloudSdkAppEngineDeployment deployment;

  @Before
  public void setUp() throws IOException {
    appYaml1 = tmpDir.newFile("app1.yaml");
    appYaml2 = tmpDir.newFile("app2.yaml");
    stagingDirectory = tmpDir.newFolder("appengine-staging");
    deployment = new CloudSdkAppEngineDeployment(sdk);
  }
  
  @Test
  public void testNullSdk() {
    try {
      new CloudSdkAppEngineDeployment(null);
      Assert.fail("allowed null SDK");
    } catch (NullPointerException expected) {
    }
  }
  
  @Test
  public void testNewDeployAction_allFlags() throws Exception {

    DefaultDeployConfiguration configuration = Mockito.spy(new DefaultDeployConfiguration());
    configuration.setDeployables(Arrays.asList(appYaml1));
    configuration.setBucket("gs://a-bucket");
    configuration.setImageUrl("imageUrl");
    configuration.setProject("project");
    configuration.setPromote(true);
    configuration.setServer("appengine.google.com");
    configuration.setStopPreviousVersion(true);
    configuration.setVersion("v1");

    SpyVerifier.newVerifier(configuration).verifyDeclaredSetters();

    deployment.deploy(configuration);

    List<String> expectedCommand = ImmutableList
        .of("deploy", appYaml1.toString(), "--bucket", "gs://a-bucket", "--image-url", "imageUrl",
            "--promote", "--server", "appengine.google.com", "--stop-previous-version", "--version",
            "v1", "--project", "project");

    verify(sdk, times(1)).runAppCommand(eq(expectedCommand));

    SpyVerifier.newVerifier(configuration).verifyDeclaredGetters(ImmutableMap.of("getDeployables", 5));
  }

  @Test
  public void testNewDeployAction_booleanFlags() throws AppEngineException, ProcessRunnerException {
    DefaultDeployConfiguration configuration = new DefaultDeployConfiguration();
    configuration.setDeployables(Arrays.asList(appYaml1));
    configuration.setPromote(false);
    configuration.setStopPreviousVersion(false);

    deployment.deploy(configuration);

    List<String> expectedCommand = ImmutableList
        .of("deploy", appYaml1.toString(), "--no-promote", "--no-stop-previous-version");

    verify(sdk, times(1)).runAppCommand(eq(expectedCommand));
  }

  @Test
  public void testNewDeployAction_noFlags() throws AppEngineException, ProcessRunnerException {

    DefaultDeployConfiguration configuration = new DefaultDeployConfiguration();
    configuration.setDeployables(Arrays.asList(appYaml1));

    List<String> expectedCommand = ImmutableList.of("deploy", appYaml1.toString());

    deployment.deploy(configuration);

    verify(sdk, times(1)).runAppCommand(eq(expectedCommand));
  }

  @Test
  public void testNewDeployAction_dir() throws AppEngineException, ProcessRunnerException {

    DefaultDeployConfiguration configuration = new DefaultDeployConfiguration();
    configuration.setDeployables(Arrays.asList(stagingDirectory));

    List<String> expectedCommand = ImmutableList.of("deploy");

    deployment.deploy(configuration);

    verify(sdk, times(1)).runAppCommandInWorkingDirectory(
        eq(expectedCommand), eq(stagingDirectory));
  }

  @Test
  public void testNewDeployAction_multipleDeployables()
      throws AppEngineException, ProcessRunnerException {

    DefaultDeployConfiguration configuration = new DefaultDeployConfiguration();
    configuration.setDeployables(Arrays.asList(appYaml1, appYaml2));

    deployment.deploy(configuration);

    List<String> expectedCommand = ImmutableList
        .of("deploy", appYaml1.toString(), appYaml2.toString());

    verify(sdk, times(1)).runAppCommand(eq(expectedCommand));

  }

  @Test
  public void testNewDeployCronAction() throws Exception {

    DefaultDeployCronConfiguration configuration = new DefaultDeployCronConfiguration();
    File cronYaml = tmpDir.newFile("cron.yaml");
    configuration.setCronYaml(cronYaml);
    configuration.setProject("project");

    deployment.deployCron(configuration);

    List<String> expectedCommand = ImmutableList
        .of("deploy", cronYaml.toString(), "--project", "project");

    verify(sdk, times(1)).runAppCommand(eq(expectedCommand));
  }

  @Test
  public void testNewDeployCronAction_badName() throws Exception {

    DefaultDeployCronConfiguration configuration = new DefaultDeployCronConfiguration();
    configuration.setCronYaml(tmpDir.newFile("another.yaml"));

    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Expecting cron.yaml");
    deployment.deployCron(configuration);
  }

  @Test
  public void testNewDeployDispatchAction() throws Exception {

    DefaultDeployDispatchConfiguration configuration = new DefaultDeployDispatchConfiguration();
    File dispatchYaml = tmpDir.newFile("dispatch.yaml");
    configuration.setDispatchYaml(dispatchYaml);
    configuration.setProject("project");

    deployment.deployDispatch(configuration);

    List<String> expectedCommand = ImmutableList
        .of("deploy", dispatchYaml.toString(), "--project", "project");

    verify(sdk, times(1)).runAppCommand(eq(expectedCommand));
  }

  @Test
  public void testNewDeployDispatchAction_badName() throws Exception {

    DefaultDeployDispatchConfiguration configuration = new DefaultDeployDispatchConfiguration();
    configuration.setDispatchYaml(tmpDir.newFile("another.yaml"));

    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Expecting dispatch.yaml");
    deployment.deployDispatch(configuration);
  }

  @Test
  public void testNewDeployDosAction() throws Exception {

    DefaultDeployDosConfiguration configuration = new DefaultDeployDosConfiguration();
    File dosYaml = tmpDir.newFile("dos.yaml");
    configuration.setDosYaml(dosYaml);
    configuration.setProject("project");

    deployment.deployDos(configuration);

    List<String> expectedCommand = ImmutableList
        .of("deploy", dosYaml.toString(), "--project", "project");

    verify(sdk, times(1)).runAppCommand(eq(expectedCommand));
  }

  @Test
  public void testNewDeployDosAction_badName() throws Exception {

    DefaultDeployDosConfiguration configuration = new DefaultDeployDosConfiguration();
    configuration.setDosYaml(tmpDir.newFile("another.yaml"));

    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Expecting dos.yaml");
    deployment.deployDos(configuration);
  }

  @Test
  public void testNewDeployIndexesAction() throws Exception {

    DefaultDeployIndexesConfiguration configuration = new DefaultDeployIndexesConfiguration();
    File indexesYaml = tmpDir.newFile("indexes.yaml");
    configuration.setIndexesYaml(indexesYaml);
    configuration.setProject("project");

    deployment.deployIndexes(configuration);

    List<String> expectedCommand = ImmutableList
        .of("deploy", indexesYaml.toString(), "--project", "project");

    verify(sdk, times(1)).runAppCommand(eq(expectedCommand));
  }

  @Test
  public void testNewDeployIndexesAction_badName() throws Exception {

    DefaultDeployIndexesConfiguration configuration = new DefaultDeployIndexesConfiguration();
    configuration.setIndexesYaml(tmpDir.newFile("another.yaml"));

    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Expecting indexes.yaml");
    deployment.deployIndexes(configuration);
  }

  @Test
  public void testNewDeployQueueAction() throws Exception {

    DefaultDeployQueueConfiguration configuration = new DefaultDeployQueueConfiguration();
    File queueYaml = tmpDir.newFile("queue.yaml");
    configuration.setQueueYaml(queueYaml);
    configuration.setProject("project");

    deployment.deployQueue(configuration);

    List<String> expectedCommand = ImmutableList
        .of("deploy", queueYaml.toString(), "--project", "project");

    verify(sdk, times(1)).runAppCommand(eq(expectedCommand));
  }

  @Test
  public void testNewDeployQueueAction_badName() throws Exception {

    DefaultDeployQueueConfiguration configuration = new DefaultDeployQueueConfiguration();
    configuration.setQueueYaml(tmpDir.newFile("another.yaml"));

    exception.expect(IllegalArgumentException.class);
    exception.expectMessage("Expecting queue.yaml");
    deployment.deployQueue(configuration);
  }
}
