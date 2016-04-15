/**
 * Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.cloud.tools.app;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.cloud.tools.app.deploy.DeployConfiguration;
import com.google.cloud.tools.app.executor.AppExecutor;
import com.google.cloud.tools.app.executor.ExecutorException;
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
import java.util.List;

/**
 * Unit tests for {@link DeployAction}.
 */
@RunWith(MockitoJUnitRunner.class)
public class DeployActionTest {

  @Rule
  public TemporaryFolder tmpDir = new TemporaryFolder();

  private File appYaml1;
  private File appYaml2;

  @Mock
  private AppExecutor appExecutor;

  @Before
  public void setUp() throws IOException {
    appYaml1 = tmpDir.newFile("app1.yaml");
    appYaml2 = tmpDir.newFile("app2.yaml");
  }

  @Test
  public void testNewDeployAction_allFlags() throws ExecutorException {

    DeployConfiguration configuration = DefaultDeployConfiguration.newBuilder(appYaml1)
        .bucket("gs://a-bucket")
        .dockerBuild("cloud")
        .force(true)
        .imageUrl("imageUrl")
        .promote(false)
        .server("appengine.google.com")
        .stopPreviousVersion(true)
        .version("v1")
        .build();

    DeployAction action = new DeployAction(configuration, appExecutor);

    List<String> expectedCommand = ImmutableList
        .of("cloud", appYaml1.toString(), "--bucket", "gs://a-bucket", "--docker-build",
            "cloud", "--force", "--image-url", "imageUrl", "--server", "appengine.google.com",
            "--stop-previous-version", "--version", "v1", "--quiet");

    action.execute();
    verify(appExecutor, times(1)).runApp(eq(expectedCommand));
  }

  @Test
  public void testNewDeployAction_noFlags() throws ExecutorException {

    DeployConfiguration configuration = DefaultDeployConfiguration.newBuilder(appYaml1)
        .build();

    DeployAction action = new DeployAction(configuration, appExecutor);

    List<String> expectedCommand = ImmutableList.of("cloud", appYaml1.toString(), "--quiet");

    action.execute();
    verify(appExecutor, times(1)).runApp(eq(expectedCommand));
  }

  @Test
  public void testNewDeployAction_multipleDeployables() throws ExecutorException {

    DeployConfiguration configuration = DefaultDeployConfiguration.newBuilder(appYaml1, appYaml2)
        .build();

    DeployAction action = new DeployAction(configuration, appExecutor);

    List<String> expectedCommand = ImmutableList
        .of("cloud", appYaml1.toString(), appYaml2.toString(), "--quiet");

    action.execute();
    verify(appExecutor, times(1)).runApp(eq(expectedCommand));
  }
}
