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

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PathResolverTest {

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private PathResolver resolver = new PathResolver();
  
  @Test
  public void testResolve() {
    Assert.assertNotNull("Could not locate Cloud SDK", resolver.getCloudSdkPath());
  }

  @Test
  public void testGetRank() {
    Assert.assertTrue(resolver.getRank() > 0);
  }

  @Test
  public void testGetLocationFromLink_valid() throws IOException {
    Path sdkHome = temporaryFolder.newFolder().toPath();
    Path bin = Files.createDirectory(sdkHome.resolve("bin"));
    Path gcloud =  Files.createFile(bin.resolve("gcloud"));
    Files.createSymbolicLink(temporaryFolder.getRoot().toPath().resolve("gcloud"), gcloud);

    List<String> possiblePaths = new ArrayList<>();

    PathResolver.getLocationsFromLink(possiblePaths, gcloud);

    Assert.assertEquals(1, possiblePaths.size());
    Assert.assertEquals(gcloud.getParent().getParent().toString(), possiblePaths.get(0));
  }

  @Test
  public void testGetLocationFromLink_notValid() throws IOException {
    Path invalidPath = temporaryFolder.newFolder().toPath();
    Files.createSymbolicLink(temporaryFolder.getRoot().toPath().resolve("gcloud"), invalidPath);

    List<String> possiblePaths = new ArrayList<>();

    PathResolver.getLocationsFromLink(possiblePaths, invalidPath);

    Assert.assertEquals(0, possiblePaths.size());
  }
}
