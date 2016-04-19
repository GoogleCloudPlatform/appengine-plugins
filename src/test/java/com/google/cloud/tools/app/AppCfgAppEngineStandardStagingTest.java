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

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.cloud.tools.app.api.AppEngineException;
import com.google.cloud.tools.app.impl.appcfg.AppCfgAppEngineStandardStaging;
import com.google.cloud.tools.app.impl.appcfg.AppEngineSdk;
import com.google.cloud.tools.app.impl.config.DefaultStageStandardConfiguration;
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
import java.net.HttpURLConnection;
import java.util.List;


/**
 * Unit tests for {@link AppCfgAppEngineStandardStaging}.
 */
@RunWith(MockitoJUnitRunner.class)
public class AppCfgAppEngineStandardStagingTest {

  @Rule
  public TemporaryFolder tmpDir = new TemporaryFolder();

  @Mock
  private AppEngineSdk sdk;

  private File source;
  private File destination;
  private File dockerfile;

  private AppCfgAppEngineStandardStaging staging;

  @Before
  public void setUp() throws IOException {
    source = tmpDir.newFile("app1.yaml");
    destination = tmpDir.newFolder("destination");
    dockerfile = tmpDir.newFile("dockerfile");

    staging = new AppCfgAppEngineStandardStaging(sdk);
  }

  @Test
  public void testCheckFlags_allFlags() throws IOException, AppEngineException {

    DefaultStageStandardConfiguration configuration = new DefaultStageStandardConfiguration();
    configuration.setSourceDirectory(source);
    configuration.setStagingDirectory(destination);
    configuration.setDockerfile(dockerfile);
    configuration.setEnableQuickstart(true);
    configuration.setDisableUpdateCheck(true);
    configuration.setVersion("v1");
    configuration.setApplicationId("project");
    configuration.setEnableJarSplitting(true);
    configuration.setJarSplittingExcludes("suffix1,suffix2");
    configuration.setCompileEncoding("UTF8");
    configuration.setDeleteJsps(true);
    configuration.setEnableJarClasses(true);

    List<String> expected = ImmutableList
        .of("stage", source.toPath().toString(), destination.toPath().toString(),
            "--enable_quickstart", "--disable_update_check", "--version",
            "v1", "-A", "project", "--enable_jar_splitting", "--jar_splitting_excludes",
            "suffix1,suffix2", "--compile_encoding", "UTF8", "--delete_jsps",
            "--enable_jar_classes");

    staging.stageStandard(configuration);

    verify(sdk, times(1)).runCommand(eq(expected));
  }

  @Test
  public void testCheckFlags_noFlags() throws IOException, AppEngineException {

    DefaultStageStandardConfiguration configuration = new DefaultStageStandardConfiguration();
    configuration.setSourceDirectory(source);
    configuration.setStagingDirectory(destination);

    List<String> expected = ImmutableList
        .of("stage", source.toPath().toString(), destination.toPath().toString());

    staging.stageStandard(configuration);

    verify(sdk, times(1)).runCommand(eq(expected));
  }

  @Test
  public void testStop() throws IOException {

    HttpURLConnection connection = mock(HttpURLConnection.class);

    doNothing().when(connection).setReadTimeout(anyInt());
    doNothing().when(connection).connect();
    doNothing().when(connection).disconnect();
    when(connection.getResponseMessage()).thenReturn("response");

    // TODO : write a new test here
  }

}
