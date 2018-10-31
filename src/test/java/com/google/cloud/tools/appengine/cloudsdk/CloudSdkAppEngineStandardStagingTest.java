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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.cloud.tools.appengine.api.AppEngineException;
import com.google.cloud.tools.appengine.api.deploy.StageStandardConfiguration;
import com.google.cloud.tools.appengine.cloudsdk.process.ProcessHandlerException;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/** Unit tests for {@link CloudSdkAppEngineStandardStaging}. */
@RunWith(MockitoJUnitRunner.class)
public class CloudSdkAppEngineStandardStagingTest {

  @Rule public TemporaryFolder tmpDir = new TemporaryFolder();

  @Mock private AppCfgRunner appCfgRunner;

  private Path source;
  private Path destination;
  private Path dockerfile;
  private CloudSdkAppEngineStandardStaging staging;
  private StageStandardConfiguration.Builder builder;

  @Before
  public void setUp()
      throws IOException, InvalidJavaSdkException, ProcessHandlerException,
          AppEngineJavaComponentsNotInstalledException {
    source = tmpDir.newFolder("source").toPath();
    destination = tmpDir.newFolder("destination").toPath();
    dockerfile = tmpDir.newFile("dockerfile").toPath();

    staging = new CloudSdkAppEngineStandardStaging(appCfgRunner);

    builder = StageStandardConfiguration.builder(source, destination);

    // create an app.yaml in staging output when we run.
    Mockito.doAnswer(
            ignored -> {
              Files.createFile(destination.resolve("app.yaml"));
              return null;
            })
        .when(appCfgRunner)
        .run(Mockito.anyList());
  }

  @Test
  public void testCheckFlags_allFlags() throws Exception {
    builder
        .dockerfile(dockerfile)
        .enableQuickstart(true)
        .disableUpdateCheck(true)
        .enableJarSplitting(true)
        .jarSplittingExcludes("suffix1,suffix2")
        .compileEncoding("UTF8")
        .deleteJsps(true)
        .enableJarClasses(true)
        .disableJarJsps(true)
        .runtime("java");

    StageStandardConfiguration configuration = builder.build();

    List<String> expected =
        ImmutableList.of(
            "--enable_quickstart",
            "--disable_update_check",
            "--enable_jar_splitting",
            "--jar_splitting_excludes=suffix1,suffix2",
            "--compile_encoding=UTF8",
            "--delete_jsps",
            "--enable_jar_classes",
            "--disable_jar_jsps",
            "--allow_any_runtime",
            "--runtime=java",
            "stage",
            source.toString(),
            destination.toString());

    staging.stageStandard(configuration);

    verify(appCfgRunner, times(1)).run(eq(expected));
  }

  @Test
  public void testCheckFlags_booleanFlags()
      throws AppEngineException, ProcessHandlerException, IOException {
    builder.enableQuickstart(false);
    builder.disableUpdateCheck(false);
    builder.enableJarSplitting(false);
    builder.deleteJsps(false);
    builder.enableJarClasses(false);
    builder.disableJarJsps(false);

    StageStandardConfiguration configuration = builder.build();

    List<String> expected = ImmutableList.of("stage", source.toString(), destination.toString());

    staging.stageStandard(configuration);

    verify(appCfgRunner, times(1)).run(eq(expected));
  }

  @Test
  public void testCheckFlags_noFlags()
      throws AppEngineException, ProcessHandlerException, IOException {

    List<String> expected = ImmutableList.of("stage", source.toString(), destination.toString());

    staging.stageStandard(builder.build());

    verify(appCfgRunner, times(1)).run(eq(expected));
  }
}
