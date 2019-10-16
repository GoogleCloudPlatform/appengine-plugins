/*
 * Copyright 2016 Google LLC.
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

package com.google.cloud.tools.appengine.operations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import com.google.cloud.tools.appengine.AppEngineException;
import com.google.cloud.tools.appengine.configuration.AppYamlProjectStageConfiguration;
import com.google.cloud.tools.test.utils.LogStoringHandler;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/** Test the {@link AppYamlProjectStaging} functionality. */
@RunWith(MockitoJUnitRunner.class)
public class AppYamlProjectStagingTest {

  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private AppYamlProjectStageConfiguration config;
  @Mock private AppYamlProjectStaging.CopyService copyService;

  private LogStoringHandler handler;
  private Path stagingDirectory;
  private Path dockerDirectory;
  private List<Path> extraFilesDirectories;
  private Path appEngineDirectory;
  private Path dockerFile;
  private Path artifact;

  @Before
  public void setUp() throws IOException {
    handler = LogStoringHandler.getForLogger(AppYamlProjectStaging.class.getName());
    appEngineDirectory = temporaryFolder.newFolder().toPath();
    dockerDirectory = temporaryFolder.newFolder().toPath();
    extraFilesDirectories =
        ImmutableList.of(
            temporaryFolder.newFolder().toPath(), temporaryFolder.newFolder().toPath());
    stagingDirectory = temporaryFolder.newFolder().toPath();
    artifact = temporaryFolder.newFile("artifact").toPath();

    dockerFile = dockerDirectory.resolve("Dockerfile");
    Files.createFile(dockerFile);

    config =
        AppYamlProjectStageConfiguration.builder(appEngineDirectory, artifact, stagingDirectory)
            .dockerDirectory(dockerDirectory)
            .extraFilesDirectories(extraFilesDirectories)
            .build();
  }

  @Test
  public void testStageArchive_flexPath() throws IOException, AppEngineException {
    Files.write(
        appEngineDirectory.resolve("app.yaml"),
        "env: flex\nruntime: test_runtime\n".getBytes(StandardCharsets.UTF_8),
        StandardOpenOption.CREATE_NEW);

    // mock to watch internal calls
    AppYamlProjectStaging mock = Mockito.mock(AppYamlProjectStaging.class);
    Mockito.doCallRealMethod().when(mock).stageArchive(config);

    mock.stageArchive(config);
    verify(mock).stageFlexibleArchive(config, "test_runtime");
  }

  @Test
  public void testStageArchive_standardPath() throws IOException, AppEngineException {
    Files.write(
        appEngineDirectory.resolve("app.yaml"),
        "runtime: java11\n".getBytes(StandardCharsets.UTF_8),
        StandardOpenOption.CREATE_NEW);

    // mock to watch internal calls
    AppYamlProjectStaging mock = Mockito.mock(AppYamlProjectStaging.class);
    Mockito.doCallRealMethod().when(mock).stageArchive(config);

    mock.stageArchive(config);
    verify(mock).stageStandardArchive(config);
  }

  @Test
  public void testStageArchive_unknown() throws IOException {
    Files.write(
        appEngineDirectory.resolve("app.yaml"),
        "runtime: moose\n".getBytes(StandardCharsets.UTF_8),
        StandardOpenOption.CREATE_NEW);

    AppYamlProjectStaging testStaging = new AppYamlProjectStaging();

    try {
      testStaging.stageArchive(config);
      fail();
    } catch (AppEngineException ex) {
      Assert.assertEquals("Cannot process application with runtime: moose", ex.getMessage());
    }
  }

  @Test
  public void testCopyDockerContext_runtimeJavaNoWarning() throws AppEngineException, IOException {
    dockerDirectory = temporaryFolder.getRoot().toPath().resolve("hopefully-made-up-dir");
    AppYamlProjectStageConfiguration invalidDockerDirConfig =
        AppYamlProjectStageConfiguration.builder(appEngineDirectory, artifact, stagingDirectory)
            .dockerDirectory(dockerDirectory)
            .build();
    assertFalse(Files.exists(dockerDirectory));

    AppYamlProjectStaging.copyDockerContext(invalidDockerDirConfig, copyService, "java");

    List<LogRecord> logs = handler.getLogs();
    assertEquals(0, logs.size());

    verifyZeroInteractions(copyService);
  }

  @Test
  public void testCopyDockerContext_noDocker() throws AppEngineException, IOException {
    AppYamlProjectStageConfiguration noDockerDirConfig =
        AppYamlProjectStageConfiguration.builder(appEngineDirectory, artifact, stagingDirectory)
            .build();
    AppYamlProjectStaging.copyDockerContext(noDockerDirConfig, copyService, "java");

    List<LogRecord> logs = handler.getLogs();
    assertEquals(0, logs.size());

    verifyZeroInteractions(copyService);
  }

  @Test
  public void testCopyDockerContext_runtimeJavaWithWarning()
      throws AppEngineException, IOException {

    AppYamlProjectStaging.copyDockerContext(config, copyService, "java");

    List<LogRecord> logs = handler.getLogs();
    assertEquals(1, logs.size());
    assertEquals(
        logs.get(0).getMessage(),
        "WARNING: runtime 'java' detected, any docker configuration in "
            + config.getDockerDirectory()
            + " will be ignored. If you wish to specify "
            + "a docker configuration, please use 'runtime: custom'.");

    verifyZeroInteractions(copyService);
  }

  @Test
  public void testCopyDockerContext_runtimeNotJavaNoDockerfile() throws IOException {

    Files.delete(dockerFile);
    try {
      AppYamlProjectStaging.copyDockerContext(config, copyService, "custom");
      fail();
    } catch (AppEngineException ex) {
      assertEquals(
          "Docker directory " + config.getDockerDirectory() + " does not contain Dockerfile.",
          ex.getMessage());
    }

    List<LogRecord> logs = handler.getLogs();
    assertEquals(0, logs.size());

    verifyZeroInteractions(copyService);
  }

  @Test
  public void testCopyDockerContext_runtimeNotJavaWithDockerfile()
      throws AppEngineException, IOException {
    AppYamlProjectStaging.copyDockerContext(config, copyService, "custom");

    List<LogRecord> logs = handler.getLogs();
    assertEquals(0, logs.size());
    verify(copyService).copyDirectory(dockerDirectory, stagingDirectory);
  }

  @Test
  public void testCopyDockerContext_runtimeNullNoDockerfile() throws IOException {

    Files.delete(dockerFile);
    try {
      AppYamlProjectStaging.copyDockerContext(config, copyService, null);
      fail();
    } catch (AppEngineException ex) {
      assertEquals(
          "Docker directory " + config.getDockerDirectory() + " does not contain Dockerfile.",
          ex.getMessage());
    }

    List<LogRecord> logs = handler.getLogs();
    assertEquals(0, logs.size());

    verifyZeroInteractions(copyService);
  }

  @Test
  public void testCopyDockerContext_runtimeNull() throws AppEngineException, IOException {

    AppYamlProjectStaging.copyDockerContext(config, copyService, null);

    List<LogRecord> logs = handler.getLogs();
    assertEquals(0, logs.size());

    verify(copyService).copyDirectory(dockerDirectory, stagingDirectory);
  }

  @Test
  public void testCopyExtraFiles_nullConfig() throws AppEngineException, IOException {
    AppYamlProjectStageConfiguration nullExtraFilesConfig =
        AppYamlProjectStageConfiguration.builder(appEngineDirectory, artifact, stagingDirectory)
            .extraFilesDirectories(null)
            .build();

    AppYamlProjectStaging.copyExtraFiles(nullExtraFilesConfig, copyService);
    verifyNoMoreInteractions(copyService);
  }

  @Test
  public void testCopyExtraFiles_nonExistantDirectory() throws IOException {
    Path extraFilesDirectory = temporaryFolder.getRoot().toPath().resolve("non-existant-directory");
    assertFalse(Files.exists(extraFilesDirectory));

    AppYamlProjectStageConfiguration badExtraFilesConfig =
        AppYamlProjectStageConfiguration.builder(appEngineDirectory, artifact, stagingDirectory)
            .extraFilesDirectories(ImmutableList.of(extraFilesDirectory))
            .build();

    try {
      AppYamlProjectStaging.copyExtraFiles(badExtraFilesConfig, copyService);
      fail();
    } catch (AppEngineException ex) {
      Assert.assertEquals(
          "Extra files directory does not exist. Location: " + extraFilesDirectory,
          ex.getMessage());
    }
  }

  @Test
  public void testCopyExtraFiles_directoryIsActuallyAFile() throws IOException {
    Path extraFilesDirectory = temporaryFolder.newFile().toPath();
    assertTrue(Files.isRegularFile(extraFilesDirectory));

    AppYamlProjectStageConfiguration badExtraFilesConfig =
        AppYamlProjectStageConfiguration.builder(appEngineDirectory, artifact, stagingDirectory)
            .extraFilesDirectories(ImmutableList.of(extraFilesDirectory))
            .build();

    try {
      AppYamlProjectStaging.copyExtraFiles(badExtraFilesConfig, copyService);
      fail();
    } catch (AppEngineException ex) {
      Assert.assertEquals(
          "Extra files location is not a directory. Location: " + extraFilesDirectory,
          ex.getMessage());
    }
  }

  @Test
  public void testCopyExtraFiles_doCopy() throws IOException, AppEngineException {
    AppYamlProjectStaging.copyExtraFiles(config, copyService);
    verify(copyService).copyDirectory(extraFilesDirectories.get(0), stagingDirectory);
    verify(copyService).copyDirectory(extraFilesDirectories.get(1), stagingDirectory);
    verifyNoMoreInteractions(copyService);
  }

  @Test
  public void testCopyAppEngineContext_nonExistentAppEngineDirectory() throws IOException {
    appEngineDirectory = temporaryFolder.getRoot().toPath().resolve("non-existent-directory");
    assertFalse(Files.exists(appEngineDirectory));

    AppYamlProjectStageConfiguration noAppYamlConfig =
        AppYamlProjectStageConfiguration.builder(appEngineDirectory, artifact, stagingDirectory)
            .build();
    try {
      AppYamlProjectStaging.copyAppEngineContext(noAppYamlConfig, copyService);
      fail();
    } catch (AppEngineException ex) {
      assertEquals("app.yaml not found in the App Engine directory.", ex.getMessage());
    }
    List<LogRecord> logs = handler.getLogs();
    assertEquals(0, logs.size());
    verifyZeroInteractions(copyService);
  }

  @Test
  public void testCopyAppEngineContext_emptyAppEngineDirectory() throws IOException {
    try {
      AppYamlProjectStaging.copyAppEngineContext(config, copyService);
      fail();
    } catch (AppEngineException ex) {
      assertEquals("app.yaml not found in the App Engine directory.", ex.getMessage());
    }
    List<LogRecord> logs = handler.getLogs();
    assertEquals(0, logs.size());
    verifyZeroInteractions(copyService);
  }

  @Test
  public void testCopyAppEngineContext_appYamlInAppEngineDirectory()
      throws AppEngineException, IOException {
    Path file = appEngineDirectory.resolve("app.yaml");
    Files.createFile(file);

    AppYamlProjectStaging.copyAppEngineContext(config, copyService);

    List<LogRecord> logs = handler.getLogs();
    assertEquals(0, logs.size());
    verify(copyService)
        .copyFileAndReplace(
            appEngineDirectory.resolve("app.yaml"), stagingDirectory.resolve("app.yaml"));
  }

  @Test
  public void testFindRuntime_malformedAppYaml() throws IOException {

    Path file = appEngineDirectory.resolve("app.yaml");
    Files.createFile(file);
    Files.write(file, ": m a l f o r m e d !".getBytes(StandardCharsets.UTF_8));

    try {
      AppYamlProjectStaging.findRuntime(config);
      fail();
    } catch (AppEngineException ex) {
      assertEquals("Malformed 'app.yaml'.", ex.getMessage());
    }
  }

  @Test
  public void testCopyArtifactJarClasspath_noClasspath() throws IOException {
    AppYamlProjectStaging.copyArtifactJarClasspath(
        AppYamlProjectStageConfiguration.builder(
                appEngineDirectory,
                Paths.get("src/test/resources/jars/libs/simpleLib.jar"),
                stagingDirectory)
            .build(),
        copyService);

    verifyZeroInteractions(copyService);

    Assert.assertEquals(0, handler.getLogs().size());
  }

  @Test
  public void testCopyArtifactJarClasspath_withClasspathEntries() throws IOException {
    AppYamlProjectStaging.copyArtifactJarClasspath(
        AppYamlProjectStageConfiguration.builder(
                appEngineDirectory,
                Paths.get("src/test/resources/jars/complexLib.jar"),
                stagingDirectory)
            .build(),
        copyService);

    verify(copyService)
        .copyFileAndReplace(
            Paths.get("src/test/resources/jars/libs/simpleLib.jar"),
            stagingDirectory.resolve("libs/simpleLib.jar"));
    verifyNoMoreInteractions(copyService);

    Assert.assertEquals(0, handler.getLogs().size());
  }

  @Test
  public void testCopyArtifactJarClasspath_withMissingClasspathEntries() throws IOException {
    AppYamlProjectStaging.copyArtifactJarClasspath(
        AppYamlProjectStageConfiguration.builder(
                appEngineDirectory,
                Paths.get("src/test/resources/jars/complexLibMissingEntryManifest.jar"),
                stagingDirectory)
            .build(),
        copyService);

    verify(copyService)
        .copyFileAndReplace(
            Paths.get("src/test/resources/jars/libs/simpleLib.jar"),
            stagingDirectory.resolve("libs/simpleLib.jar"));
    verifyNoMoreInteractions(copyService);

    // check for warning about missing jars
    List<LogRecord> logs = handler.getLogs();
    Assert.assertEquals(1, logs.size());
    Assert.assertEquals(Level.WARNING, logs.get(0).getLevel());
    Assert.assertEquals(
        "Could not copy 'Class-Path' jar: "
            + Paths.get("src/test/resources/jars/libs/missing.jar")
            + " referenced in MANIFEST.MF",
        logs.get(0).getMessage());
  }

  @Test
  public void testCopyArtifactJarClasspath_targetAlreadyExists() throws IOException {

    Path simpleLib = Paths.get("src/test/resources/jars/libs/simpleLib.jar");
    Path simpleLibTarget = stagingDirectory.resolve("libs/simpleLib.jar");
    Files.createDirectories(simpleLibTarget.getParent());
    Files.createFile(simpleLibTarget);

    AppYamlProjectStaging.copyArtifactJarClasspath(
        AppYamlProjectStageConfiguration.builder(
                appEngineDirectory,
                Paths.get("src/test/resources/jars/complexLib.jar"),
                stagingDirectory)
            .build(),
        copyService);

    verify(copyService).copyFileAndReplace(simpleLib, simpleLibTarget);
    verifyNoMoreInteractions(copyService);

    // check for warning about overwriting jars
    List<LogRecord> logs = handler.getLogs();
    Assert.assertEquals(1, logs.size());
    Assert.assertEquals(Level.WARNING, logs.get(0).getLevel());
    Assert.assertEquals(
        "Overwriting 'Class-Path' jar: "
            + simpleLibTarget
            + " with "
            + simpleLib
            + " referenced in MANIFEST.MF",
        logs.get(0).getMessage());
  }
}
