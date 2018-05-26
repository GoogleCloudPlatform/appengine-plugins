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
import static org.junit.Assert.fail;

import java.io.File;
import org.junit.Before;
import org.junit.Test;

public class StageFlexibleConfigurationTest {

  private StageFlexibleConfiguration configuration;
  private File file = new File("");

  @Before
  public void setUp() {
    // todo should we check these are not the same and
    // files are files and directories are directories?
    // should we use paths instead?
    configuration =
        new StageFlexibleConfiguration.Builder()
            .setAppEngineDirectory(file)
            .setArtifact(file)
            .setDockerDirectory(file)
            .setStagingDirectory(file)
            .build();
  }

  @Test
  public void testInitialValues() {
    try {
      new StageFlexibleConfiguration.Builder().build();
      fail();
    } catch (NullPointerException ex) {
    }
  }

  @Test
  public void testSetAppEngineDirectory() {
    assertEquals(file, configuration.getAppEngineDirectory());
  }

  @Test
  public void testSetArtifact() {
    assertEquals(file, configuration.getArtifact());
  }

  @Test
  public void testSetDockerDirectory() {
    assertEquals(file, configuration.getDockerDirectory());
  }

  @Test
  public void testSetStagingDirectory() {
    assertEquals(file, configuration.getStagingDirectory());
  }
}
