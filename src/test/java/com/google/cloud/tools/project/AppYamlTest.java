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

package com.google.cloud.tools.project;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Tests for AppYaml parsing */
public class AppYamlTest {

  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  @Test
  public void testGetRuntime_success() throws IOException {
    Path appYaml = writeFile("runtime: java\np2: v2");
    Assert.assertEquals("java", new AppYaml(appYaml).getRuntime());
  }

  @Test
  public void testGetRuntime_failureBecauseWrongType() throws IOException {
    Path appYaml = writeFile("runtime: [goose, moose]\np2: v2");
    Assert.assertNull(new AppYaml(appYaml).getRuntime());
  }

  @Test
  public void testGetRuntime_failureBecauseNotPresent() throws IOException {
    Path appYaml = writeFile("p1: v1\np2: v2");
    Assert.assertNull(new AppYaml(appYaml).getRuntime());
  }

  // https://github.com/GoogleCloudPlatform/appengine-plugins-core/issues/405
  @Test
  public void testGetRuntime_emptyAppYaml() throws IOException {
    Path appYaml = writeFile("");
    Assert.assertNull(new AppYaml(appYaml).getRuntime());
  }

  private Path writeFile(String contents) throws IOException {
    File destination = temporaryFolder.newFile();
    return Files.write(destination.toPath(), contents.getBytes(StandardCharsets.UTF_8));
  }
}
