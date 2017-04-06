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
import static org.junit.Assert.assertNull;

import java.io.File;
import org.junit.Test;

public class DefaultStageFlexibleConfigurationTest {

  @Test
  public void testInitialValues() {
    DefaultStageFlexibleConfiguration configuration = new DefaultStageFlexibleConfiguration();
    assertNull(configuration.getAppEngineDirectory());
    assertNull(configuration.getArtifact());
    assertNull(configuration.getDockerDirectory());
    assertNull(configuration.getStagingDirectory());
  }

  @Test
  public void testSetAppEngineDirectory() {
    File file = new File("");
    DefaultStageFlexibleConfiguration configuration = new DefaultStageFlexibleConfiguration();
    configuration.setAppEngineDirectory(file);
    assertEquals(file, configuration.getAppEngineDirectory());
  }

  @Test
  public void testSetArtifact() {
    File file = new File("");
    DefaultStageFlexibleConfiguration configuration = new DefaultStageFlexibleConfiguration();
    configuration.setArtifact(file);
    assertEquals(file, configuration.getArtifact());
  }

  @Test
  public void testSetDockerDirectory() {
    File file = new File("");
    DefaultStageFlexibleConfiguration configuration = new DefaultStageFlexibleConfiguration();
    configuration.setDockerDirectory(file);
    assertEquals(file, configuration.getDockerDirectory());
  }

  @Test
  public void testSetStagingDirectory() {
    File file = new File("");
    DefaultStageFlexibleConfiguration configuration = new DefaultStageFlexibleConfiguration();
    configuration.setStagingDirectory(file);
    assertEquals(file, configuration.getStagingDirectory());
  }
}
