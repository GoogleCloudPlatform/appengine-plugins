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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import com.google.cloud.tools.appengine.api.AppEngineException;
import com.google.cloud.tools.appengine.api.deploy.StageFlexibleConfiguration;
import com.google.cloud.tools.appengine.cloudsdk.CloudSdkAppEngineFlexibleStaging.CopyService;
import com.google.cloud.tools.test.utils.LogStoringHandler;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.logging.LogRecord;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

/** Test the CloudSdkAppEngineFlexibleStaging functionality */
@RunWith(MockitoJUnitRunner.class)
public class CloudSdkAppEngineFlexibleStagingTest {

  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Mock public StageFlexibleConfiguration config;
  @Mock public CopyService copyService;

  private LogStoringHandler handler;
  private File stagingDirectory;
  private File dockerDirectory;
  private File appEngineDirectory;

  @Before
  public void setUp() {
    handler = LogStoringHandler.getForLogger(CloudSdkAppEngineFlexibleStaging.class.getName());
  }

  @Test
  public void testCopyDockerContext_runtimeJavaNoWarning() throws AppEngineException, IOException {
    new FlexibleStagingContext().withNonExistantDockerDirectory();

    CloudSdkAppEngineFlexibleStaging.copyDockerContext(config, copyService, "java");

    List<LogRecord> logs = handler.getLogs();
    assertEquals(0, logs.size());

    verifyZeroInteractions(copyService);
  }

  @Test
  public void testCopyDockerContext_runtimeJavaWithWarning()
      throws AppEngineException, IOException {
    new FlexibleStagingContext().withDockerDirectory();

    CloudSdkAppEngineFlexibleStaging.copyDockerContext(config, copyService, "java");

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
    new FlexibleStagingContext().withDockerDirectory();

    try {
      CloudSdkAppEngineFlexibleStaging.copyDockerContext(config, copyService, "custom");
      fail();
    } catch (AppEngineException ex) {
      assertEquals(
          "Docker directory "
              + config.getDockerDirectory().toPath()
              + " does not contain Dockerfile.",
          ex.getMessage());
    }

    List<LogRecord> logs = handler.getLogs();
    assertEquals(0, logs.size());

    verifyZeroInteractions(copyService);
  }

  @Test
  public void testCopyDockerContext_runtimeNotJavaWithDockerfile()
      throws AppEngineException, IOException {
    new FlexibleStagingContext().withStagingDirectory().withDockerDirectory().withDockerFile();

    CloudSdkAppEngineFlexibleStaging.copyDockerContext(config, copyService, "custom");

    List<LogRecord> logs = handler.getLogs();
    assertEquals(0, logs.size());

    verify(copyService).copyDirectory(dockerDirectory.toPath(), stagingDirectory.toPath());
  }

  @Test
  public void testCopyDockerContext_runtimeNullNoDockerfile() throws IOException {
    new FlexibleStagingContext().withDockerDirectory();

    try {
      CloudSdkAppEngineFlexibleStaging.copyDockerContext(config, copyService, null);
      fail();
    } catch (AppEngineException ex) {
      assertEquals(
          "Docker directory "
              + config.getDockerDirectory().toPath()
              + " does not contain Dockerfile.",
          ex.getMessage());
    }

    List<LogRecord> logs = handler.getLogs();
    assertEquals(0, logs.size());

    verifyZeroInteractions(copyService);
  }

  @Test
  public void testCopyDockerContext_runtimeNull() throws AppEngineException, IOException {
    new FlexibleStagingContext().withStagingDirectory().withDockerDirectory().withDockerFile();

    CloudSdkAppEngineFlexibleStaging.copyDockerContext(config, copyService, null);

    List<LogRecord> logs = handler.getLogs();
    assertEquals(0, logs.size());

    verify(copyService).copyDirectory(dockerDirectory.toPath(), stagingDirectory.toPath());
  }

  @Test
  public void testCopyAppEngineContext_nonExistentAppEngineDirectory() throws IOException {
    new FlexibleStagingContext().withNonExistentAppEngineDirectory();

    try {
      CloudSdkAppEngineFlexibleStaging.copyAppEngineContext(config, copyService);
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
    new FlexibleStagingContext().withAppEngineDirectory();

    try {
      CloudSdkAppEngineFlexibleStaging.copyAppEngineContext(config, copyService);
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
    new FlexibleStagingContext()
        .withStagingDirectory()
        .withAppEngineDirectory()
        .withFileInAppEngineDirectory("app.yaml", null);

    CloudSdkAppEngineFlexibleStaging.copyAppEngineContext(config, copyService);

    List<LogRecord> logs = handler.getLogs();
    assertEquals(0, logs.size());
    verify(copyService)
        .copyFileAndReplace(
            appEngineDirectory.toPath().resolve("app.yaml"),
            stagingDirectory.toPath().resolve("app.yaml"));
  }

  @Test
  public void testFindRuntime_malformedAppYaml() throws IOException {
    new FlexibleStagingContext()
        .withAppEngineDirectory()
        .withFileInAppEngineDirectory("app.yaml", ": m a l f o r m e d !");

    try {
      assertNull(CloudSdkAppEngineFlexibleStaging.findRuntime(config));
    } catch (AppEngineException ex) {
      assertEquals("Malformed 'app.yaml'.", ex.getMessage());
    }
  }

  /**
   * Private class for creating test file system structures. It writes to the test class members.
   */
  private class FlexibleStagingContext {
    private FlexibleStagingContext withStagingDirectory() throws IOException {
      stagingDirectory = temporaryFolder.newFolder();
      when(config.getStagingDirectory()).thenReturn(stagingDirectory);
      return this;
    }

    private FlexibleStagingContext withNonExistantDockerDirectory() {
      dockerDirectory = new File(temporaryFolder.getRoot(), "hopefully-made-up-dir");
      assertFalse(dockerDirectory.exists());
      when(config.getDockerDirectory()).thenReturn(dockerDirectory);
      return this;
    }

    private FlexibleStagingContext withDockerDirectory() throws IOException {
      dockerDirectory = temporaryFolder.newFolder();
      when(config.getDockerDirectory()).thenReturn(dockerDirectory);
      return this;
    }

    private FlexibleStagingContext withDockerFile() throws IOException {
      Assert.assertNotNull("needs withDockerDirectory to be called first", dockerDirectory);
      assertTrue("needs withDockerDirectory to be called first", dockerDirectory.exists());
      File dockerFile = new File(dockerDirectory, "Dockerfile");
      if (!dockerFile.createNewFile()) {
        throw new IOException("Could not create Dockerfile for test");
      }
      return this;
    }

    private FlexibleStagingContext withNonExistentAppEngineDirectory() {
      appEngineDirectory = new File(temporaryFolder.getRoot(), "non-existent-directory");
      assertFalse(appEngineDirectory.exists());
      when(config.getAppEngineDirectory()).thenReturn(appEngineDirectory);
      return this;
    }

    private FlexibleStagingContext withAppEngineDirectory() throws IOException {
      appEngineDirectory = temporaryFolder.newFolder();
      when(config.getAppEngineDirectory()).thenReturn(appEngineDirectory);
      return this;
    }

    private FlexibleStagingContext withFileInAppEngineDirectory(String fileName, String contents)
        throws IOException {
      Assert.assertNotNull("needs withAppEngineDirectory to be called first", appEngineDirectory);
      assertTrue("needs withAppEngineDirectory to be called first", appEngineDirectory.exists());

      File file = new File(appEngineDirectory, fileName);
      if (!file.createNewFile()) {
        throw new IOException("Could not create " + fileName + " for test");
      }
      if (contents != null) {
        Files.write(file.toPath(), contents.getBytes(StandardCharsets.UTF_8));
      }
      return this;
    }
  }
}
