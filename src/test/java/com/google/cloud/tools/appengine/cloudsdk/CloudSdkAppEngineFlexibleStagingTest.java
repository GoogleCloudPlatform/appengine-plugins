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
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

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

  @Before
  public void setUp() {
    Logger log = Logger.getLogger(CloudSdkAppEngineFlexibleStaging.class.getName());
    handler = new LogStoringHandler();
    log.addHandler(handler);
  }

  @Test
  public void testCopyDockerContext_runtimeJavaNoWarning() throws Exception {
    File dockerDirectory = new File(temporaryFolder.getRoot(), "hopefully-made-up-dir");
    Assert.assertFalse(dockerDirectory.exists());

    when(config.getDockerDirectory()).thenReturn(dockerDirectory);
    CloudSdkAppEngineFlexibleStaging.copyDockerContext(config, copyService, "java");

    List<LogRecord> logs = handler.getLogs();
    Assert.assertEquals(0, logs.size());

    verifyZeroInteractions(copyService);
  }

  @Test
  public void testCopyDockerContext_runtimeJavaWithWarning() throws Exception {
    File dockerDirectory = temporaryFolder.newFolder();
    when(config.getDockerDirectory()).thenReturn(dockerDirectory);

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
    File dockerDirectory = temporaryFolder.newFolder();
    when(config.getDockerDirectory()).thenReturn(dockerDirectory);

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

    File stagingDirectory = temporaryFolder.newFolder();
    File dockerDirectory = temporaryFolder.newFolder();
    File dockerfile = new File(dockerDirectory, "Dockerfile");
    if (!dockerfile.createNewFile()) {
      throw new IOException("Could not create Dockerfile for test");
    }
    when(config.getDockerDirectory()).thenReturn(dockerDirectory);
    when(config.getStagingDirectory()).thenReturn(stagingDirectory);

    CloudSdkAppEngineFlexibleStaging.copyDockerContext(config, copyService, "custom");

    List<LogRecord> logs = handler.getLogs();
    Assert.assertEquals(0, logs.size());

    verify(copyService).copyDirectory(dockerDirectory.toPath(), stagingDirectory.toPath());
  }
}