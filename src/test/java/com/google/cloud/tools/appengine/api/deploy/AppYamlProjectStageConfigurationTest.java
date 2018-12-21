/*
 * Copyright 2017 Google Inc.
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

package com.google.cloud.tools.appengine.api.deploy;

import static org.junit.Assert.assertEquals;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.Before;
import org.junit.Test;

public class AppYamlProjectStageConfigurationTest {

  private AppYamlProjectStageConfiguration configuration;
  private Path file = Paths.get("");

  @Before
  public void setUp() {
    // todo should we check these are not the same and
    // files are files and directories are directories?
    // should we use paths instead?
    configuration =
        AppYamlProjectStageConfiguration.builder(file, file, file).dockerDirectory(file).build();
  }

  @Test
  public void testDockerNotRequired() {
    AppYamlProjectStageConfiguration.builder(file, file, file).build();
  }

  @Test
  public void testGetAppEngineDirectory() {
    assertEquals(file, configuration.getAppEngineDirectory());
  }

  @Test
  public void testGetArtifact() {
    assertEquals(file, configuration.getArtifact());
  }

  @Test
  public void testGetDockerDirectory() {
    assertEquals(file, configuration.getDockerDirectory());
  }

  @Test
  public void testGetStagingDirectory() {
    assertEquals(file, configuration.getStagingDirectory());
  }
}
