/*
 * Copyright 2018 Google LLC.
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import com.google.common.io.Files;
import java.io.File;
import org.junit.Before;
import org.junit.Test;

public class StageStandardConfigurationTest {

  private StageStandardConfiguration configuration;
  private File stagingDirectory = Files.createTempDir();
  private File sourceDirectory = Files.createTempDir();

  @Before
  public void setUp() {
    // todo should we check these are not the same and
    // files are files and directories are directories?
    // should we use paths instead?
    configuration =
        new DefaultStageStandardConfiguration.Builder()
            .setStagingDirectory(stagingDirectory)
            .setSourceDirectory(sourceDirectory)
            .build();
  }

  @Test
  public void testInitialValuesRequired() {
    try {
      new DefaultStageStandardConfiguration.Builder().build();
      fail();
    } catch (NullPointerException ex) {
      assertNotNull(ex.getMessage());
    }
  }

  @Test
  public void testGetStagingDirectory() {
    assertEquals(stagingDirectory, configuration.getStagingDirectory());
  }

  @Test
  public void testGetSourceDirectory() {
    assertEquals(sourceDirectory, configuration.getSourceDirectory());
  }

  @Test
  public void testDefaultsToNull() {
    assertNull(configuration.getCompileEncoding());
    assertNull(configuration.getDeleteJsps());
    assertNull(configuration.getDisableJarJsps());
    assertNull(configuration.getDisableUpdateCheck());
    assertNull(configuration.getDockerfile());
    assertNull(configuration.getEnableJarClasses());
    assertNull(configuration.getEnableJarSplitting());
    assertNull(configuration.getEnableQuickstart());
    assertNull(configuration.getJarSplittingExcludes());
    assertNull(configuration.getRuntime());
  }
}
