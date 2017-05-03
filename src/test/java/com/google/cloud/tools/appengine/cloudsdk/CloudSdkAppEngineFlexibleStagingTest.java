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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import com.google.cloud.tools.appengine.api.AppEngineException;
import com.google.cloud.tools.appengine.api.deploy.StageFlexibleConfiguration;
import com.google.cloud.tools.appengine.cloudsdk.CloudSdkAppEngineFlexibleStaging.CopyService;
import com.google.cloud.tools.test.utils.LogStoringHandler;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.verification.VerificationMode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.LogRecord;

/**
 * Test the CloudSdkAppEngineFlexibleStaging functionality
 */
@RunWith(MockitoJUnitRunner.class)
public class CloudSdkAppEngineFlexibleStagingTest {

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Rule
  public ExpectedException exception = ExpectedException.none();

  @Mock
  public StageFlexibleConfiguration config;
  @Mock
  public CopyService copyService;

  private LogStoringHandler handler;
  private Path stagingDirectory;
  private Path dockerDirectory;
  private Path appEngineDirectory;

  @Before
  public void setUp() {
    handler = LogStoringHandler.getForLogger(CloudSdkAppEngineFlexibleStaging.class.getName());
  }

  @Test
  public void testCopyDockerContext_runtimeJavaNoWarning() throws Exception {
    new FlexibleStagingContext().withNonExistantDockerDirectory();

    CloudSdkAppEngineFlexibleStaging.copyDockerContext(config, copyService, "java");

    List<LogRecord> logs = handler.getLogs();
    Assert.assertEquals(0, logs.size());

    verifyZeroInteractions(copyService);
  }

  @Test
  public void testCopyDockerContext_runtimeJavaWithWarning() throws Exception {
    new FlexibleStagingContext().withDockerDirectory();

    CloudSdkAppEngineFlexibleStaging.copyDockerContext(config, copyService, "java");

    List<LogRecord> logs = handler.getLogs();
    Assert.assertEquals(1, logs.size());
    Assert.assertEquals(logs.get(0).getMessage(),
        "WARNING: 'runtime 'java' detected, any docker configuration in "
            + config.getDockerDirectory() + " will be ignored. If you wish to specify"
            + "docker configuration, please use 'runtime: custom'");

    verifyZeroInteractions(copyService);
  }

  @Test
  public void testCopyDockerContext_runtimeNotJavaNoDockerfile() throws Exception {
    new FlexibleStagingContext().withDockerDirectory();

    exception.expect(AppEngineException.class);
    exception.expectMessage("Docker directory " + config.getDockerDirectory().toPath()
        + " does not contain Dockerfile");

    CloudSdkAppEngineFlexibleStaging.copyDockerContext(config, copyService, "custom");

    List<LogRecord> logs = handler.getLogs();
    Assert.assertEquals(0, logs.size());

    verifyZeroInteractions(copyService);
  }

  @Test
  public void testCopyDockerContext_runtimeNotJavaWithDockerfile() throws IOException {
    new FlexibleStagingContext()
        .withStagingDirectory()
        .withDockerDirectory()
        .withDockerFile();

    CloudSdkAppEngineFlexibleStaging.copyDockerContext(config, copyService, "custom");

    List<LogRecord> logs = handler.getLogs();
    Assert.assertEquals(0, logs.size());

    verify(copyService).copyDirectory(dockerDirectory, stagingDirectory);
  }

  @Test
  public void testCopyDockerContext_runtimeNullNoDockerfile() throws Exception {
    new FlexibleStagingContext().withDockerDirectory();

    exception.expect(AppEngineException.class);
    exception.expectMessage("Docker directory " + config.getDockerDirectory().toPath()
        + " does not contain Dockerfile");

    CloudSdkAppEngineFlexibleStaging.copyDockerContext(config, copyService, null);

    List<LogRecord> logs = handler.getLogs();
    Assert.assertEquals(0, logs.size());

    verifyZeroInteractions(copyService);
  }

  @Test
  public void testCopyDockerContext_runtimeNull() throws IOException {
    new FlexibleStagingContext()
        .withStagingDirectory()
        .withDockerDirectory()
        .withDockerFile();

    CloudSdkAppEngineFlexibleStaging.copyDockerContext(config, copyService, null);

    List<LogRecord> logs = handler.getLogs();
    Assert.assertEquals(0, logs.size());

    verify(copyService).copyDirectory(dockerDirectory, stagingDirectory);
  }

  @Test
  public void testCopyAppEngineContext_nonExistentAppEngineDirectory() throws Exception {
    new FlexibleStagingContext().withNonExistentAppEngineDirectory();

    exception.expect(AppEngineException.class);
    exception.expectMessage("app.yaml not found in the App Engine directory.");

    CloudSdkAppEngineFlexibleStaging.copyAppEngineContext(config, copyService);

    List<LogRecord> logs = handler.getLogs();
    Assert.assertEquals(0, logs.size());
    verifyZeroInteractions(copyService);
  }

  @Test
  public void testCopyAppEngineContext_emptyAppEngineDirectory() throws Exception {
    new FlexibleStagingContext().withAppEngineDirectory();

    exception.expect(AppEngineException.class);
    exception.expectMessage("app.yaml not found in the App Engine directory.");

    CloudSdkAppEngineFlexibleStaging.copyAppEngineContext(config, copyService);

    List<LogRecord> logs = handler.getLogs();
    Assert.assertEquals(0, logs.size());
    verifyZeroInteractions(copyService);
  }

  @Test
  public void testCopyAppEngineContext_appYamlInAppEngineDirectory() throws Exception {
    new FlexibleStagingContext()
        .withStagingDirectory()
        .withAppEngineDirectory()
        .withFileInAppEngineDirectory("app.yaml");

    CloudSdkAppEngineFlexibleStaging.copyAppEngineContext(config, copyService);

    List<LogRecord> logs = handler.getLogs();
    Assert.assertEquals(0, logs.size());
    verify(copyService).copyFileAndReplace(appEngineDirectory.resolve("app.yaml"),
        stagingDirectory.resolve("app.yaml"));
  }

  @Test
  public void testCopyAppEngineContext_includeCronYaml() throws Exception {
    verifyOptionalConfigurationFileStaged("cron.yaml", true /* includeSet */);
  }

  @Test
  public void testCopyAppEngineContext_doNotIncludeCronYaml() throws Exception {
    verifyOptionalConfigurationFileStaged("cron.yaml", false /* includeSet */);
  }

  @Test
  public void testCopyAppEngineContext_includeDosYaml() throws Exception {
    verifyOptionalConfigurationFileStaged("cron.yaml", true /* includeSet */);
  }

  @Test
  public void testCopyAppEngineContext_doNotIncludeDosYaml() throws Exception {
    verifyOptionalConfigurationFileStaged("cron.yaml", false /* includeSet */);
  }

  @Test
  public void testCopyAppEngineContext_includeDispatchYaml()
      throws Exception {
    verifyOptionalConfigurationFileStaged("dispatch.yaml", true /* includeSet */);
  }

  @Test
  public void testCopyAppEngineContext_doNotIncludeDispatchYaml() throws Exception {
    verifyOptionalConfigurationFileStaged("dispatch.yaml", false /* includeSet */);
  }

  @Test
  public void testCopyAppEngineContext_includeIndexYaml()
      throws Exception {
    verifyOptionalConfigurationFileStaged("index.yaml", true /* includeSet */);
  }

  @Test
  public void testCopyAppEngineContext_doNotIncludeIndexYaml() throws Exception {
    verifyOptionalConfigurationFileStaged("index.yaml", false /* includeSet */);
  }

  @Test
  public void testCopyAppEngineContext_includeQueueYaml()
      throws Exception {
    verifyOptionalConfigurationFileStaged("queue.yaml", true /* includeSet */);
  }

  @Test
  public void testCopyAppEngineContext_doNotIncludeQueueYaml() throws Exception {
    verifyOptionalConfigurationFileStaged("queue.yaml", false /* includeSet */);
  }

  private void verifyOptionalConfigurationFileStaged(String filename, boolean includeSet)
      throws IOException {
    new FlexibleStagingContext().withStagingDirectory().withAppEngineDirectory()
        .withFileInAppEngineDirectory("app.yaml")
        .withFileInAppEngineDirectory(filename);
    when(config.getIncludeOptionalConfigurationFiles()).thenReturn(includeSet);

    CloudSdkAppEngineFlexibleStaging.copyAppEngineContext(config, copyService);

    assertTrue(handler.getLogs().isEmpty());
    verify(copyService, times(1)).copyFileAndReplace(
        appEngineDirectory.resolve("app.yaml"), stagingDirectory.resolve("app.yaml"));
    VerificationMode mode = includeSet ? times(1) : never();
    verify(copyService, mode).copyFileAndReplace(
        appEngineDirectory.resolve(filename), stagingDirectory.resolve(filename));
  }

  @Test
  public void testCopyFileAndReplaceIfSourceFileExists_fileExists() throws IOException {
    temporaryFolder.newFile("concrete-file");
    Path fromDirectory = temporaryFolder.getRoot().toPath();
    Path toDirectory = temporaryFolder.newFolder().toPath();
    CloudSdkAppEngineFlexibleStaging.copyFileAndReplaceIfFileExists(
        fromDirectory, toDirectory, "concrete-file", copyService);

    verify(copyService).copyFileAndReplace(fromDirectory.resolve("concrete-file"),
        toDirectory.resolve("concrete-file"));
  }

  @Test
  public void testCopyFileAndReplaceIfSourceFileExists_fileDoesNotExist() throws IOException {
    Path fromDirectory = temporaryFolder.getRoot().toPath();
    Path toDirectory = temporaryFolder.newFolder().toPath();
    CloudSdkAppEngineFlexibleStaging.copyFileAndReplaceIfFileExists(
        fromDirectory, toDirectory, "non-existing-file", copyService);

    verify(copyService, never()).copyFileAndReplace(fromDirectory.resolve("non-existing-file"),
        toDirectory.resolve("non-existing-file"));
  }

  /**
   * Private class for creating test file system structures, it will
   * write to the test class members
   */
  private class FlexibleStagingContext {
    private FlexibleStagingContext withStagingDirectory() throws IOException {
      stagingDirectory = temporaryFolder.newFolder().toPath();
      when(config.getStagingDirectory()).thenReturn(stagingDirectory.toFile());
      return this;
    }
    private FlexibleStagingContext withNonExistantDockerDirectory() {
      dockerDirectory = temporaryFolder.getRoot().toPath().resolve("hopefully-made-up-dir");
      assertFalse(dockerDirectory.toFile().exists());
      when(config.getDockerDirectory()).thenReturn(dockerDirectory.toFile());
      return this;
    }
    private FlexibleStagingContext withDockerDirectory() throws IOException {
      dockerDirectory = temporaryFolder.newFolder().toPath();
      when(config.getDockerDirectory()).thenReturn(dockerDirectory.toFile());
      return this;
    }
    private FlexibleStagingContext withDockerFile() throws IOException {
      Assert.assertNotNull("needs withDockerDirectory to be called first", dockerDirectory);
      assertTrue("needs withDockerDirectory to be called first", dockerDirectory.toFile().exists());
      File dockerFile = dockerDirectory.resolve("Dockerfile").toFile();
      if (!dockerFile.createNewFile()) {
        throw new IOException("Could not create Dockerfile for test");
      }
      return this;
    }
    private FlexibleStagingContext withNonExistentAppEngineDirectory() throws IOException {
      appEngineDirectory = temporaryFolder.getRoot().toPath().resolve("non-existent-directory");
      assertFalse(appEngineDirectory.toFile().exists());
      when(config.getAppEngineDirectory()).thenReturn(appEngineDirectory.toFile());
      return this;
    }
    private FlexibleStagingContext withAppEngineDirectory() throws IOException {
      appEngineDirectory = temporaryFolder.newFolder().toPath();
      when(config.getAppEngineDirectory()).thenReturn(appEngineDirectory.toFile());
      return this;
    }
    private FlexibleStagingContext withFileInAppEngineDirectory(String fileName)
        throws IOException {
      Assert.assertNotNull("needs withAppEngineDirectory to be called first", appEngineDirectory);
      assertTrue("needs withAppEngineDirectory to be called first",
          appEngineDirectory.toFile().exists());

      File file = appEngineDirectory.resolve(fileName).toFile();
      if (!file.createNewFile()) {
        throw new IOException("Could not create " + fileName + " for test");
      }
      return this;
    }
  }
}