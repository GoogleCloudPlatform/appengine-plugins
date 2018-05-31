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

import com.google.cloud.tools.test.utils.LogStoringHandler;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.LogRecord;
import javax.annotation.Nullable;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;

public class PathResolverTest {

  @ClassRule public static TemporaryFolder symlinkTestArea = new TemporaryFolder();
  @Nullable private static Exception symlinkException = null;

  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private PathResolver resolver = new PathResolver();

  @BeforeClass
  public static void generateSymlinkException() throws IOException {
    Path dest = symlinkTestArea.newFile().toPath();
    Path link =
        symlinkTestArea.getRoot().toPath().resolve("test-link" + System.currentTimeMillis());
    try {
      Files.createSymbolicLink(link, dest);
    } catch (Exception e) {
      symlinkException = e;
    }
  }

  @Test
  public void testResolve() {
    Assert.assertNotNull("Could not locate Cloud SDK", resolver.getCloudSdkPath());
  }

  @Test
  public void testGetRank() {
    Assert.assertTrue(resolver.getRank() > 10000);
  }

  @Test
  public void testGetLocationsFromPath() {
    List<String> paths =
        PathResolver.getLocationsFromPath(
            "\\my music & videos" + "google-cloud-sdk" + File.separator + "bin");
    Assert.assertEquals(1, paths.size());
    Assert.assertEquals("\\my music & videosgoogle-cloud-sdk", paths.get(0));
  }

  @Test
  public void testUnquote() {
    String actual = PathResolver.unquote("\"only remove \"\" end quotes\"");
    Assert.assertEquals("only remove \"\" end quotes", actual);
  }

  @Test
  public void testGetLocationFromLink_valid() throws IOException {
    Assume.assumeNoException(symlinkException);
    Path sdkHome = temporaryFolder.newFolder().toPath().toRealPath();
    Path bin = Files.createDirectory(sdkHome.resolve("bin"));
    Path gcloud = Files.createFile(bin.resolve("gcloud"));
    Files.createSymbolicLink(temporaryFolder.getRoot().toPath().resolve("gcloud"), gcloud);

    List<String> possiblePaths = new ArrayList<>();
    PathResolver.getLocationsFromLink(possiblePaths, gcloud);

    Assert.assertEquals(1, possiblePaths.size());
    Assert.assertEquals(gcloud.getParent().getParent().toString(), possiblePaths.get(0));
  }

  @Test
  public void testGetLocationFromLink_notValid() throws IOException {
    Assume.assumeNoException(symlinkException);
    Path invalidPath = temporaryFolder.newFolder().toPath();
    Files.createSymbolicLink(temporaryFolder.getRoot().toPath().resolve("gcloud"), invalidPath);

    List<String> possiblePaths = new ArrayList<>();

    PathResolver.getLocationsFromLink(possiblePaths, invalidPath);

    Assert.assertEquals(0, possiblePaths.size());
  }

  @Test
  public void testGetLocationFromLink_triggerException() throws IOException {
    Assume.assumeNoException(symlinkException);
    LogStoringHandler testHandler = LogStoringHandler.getForLogger(PathResolver.class.getName());

    Path exceptionForcingPath = Mockito.mock(Path.class);
    IOException exception = Mockito.mock(IOException.class);
    Mockito.when(exceptionForcingPath.toRealPath()).thenThrow(exception);

    List<String> possiblePaths = new ArrayList<>();
    PathResolver.getLocationsFromLink(possiblePaths, exceptionForcingPath);

    Assert.assertEquals(1, testHandler.getLogs().size());
    LogRecord logRecord = testHandler.getLogs().get(0);

    Assert.assertEquals(
        "Non-critical exception when searching for cloud-sdk", logRecord.getMessage());
    Assert.assertEquals(exception, logRecord.getThrown());
  }
}
