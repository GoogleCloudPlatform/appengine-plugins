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

import static org.hamcrest.core.AnyOf.anyOf;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.StringEndsWith.endsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.cloud.tools.appengine.cloudsdk.serialization.CloudSdkVersion;
import com.google.common.io.Files;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

/** Unit tests for {@link CloudSdk}. */
@RunWith(MockitoJUnitRunner.class)
public class CloudSdkTest {

  private Path root;
  private CloudSdk sdk;

  @Before
  public void setup() throws CloudSdkNotFoundException {
    root = Paths.get(Files.createTempDir().toString());
    sdk = new CloudSdk.Builder().sdkPath(root).build();
  }

  private void writeVersionFile(String contents) throws IOException {
    Files.asCharSink(root.resolve("VERSION").toFile(), StandardCharsets.UTF_8).write(contents);
  }

  @Test
  public void testGetSdkPath() {
    assertEquals(root, sdk.getPath());
  }

  @Test
  public void testValidateCloudSdk()
      throws CloudSdkNotFoundException, CloudSdkOutOfDateException, CloudSdkVersionFileException {
    new CloudSdk.Builder().build().validateCloudSdk();
  }

  @Test
  public void testValidateCloudSdk_doesNotThrowInvalidJdkException()
      throws CloudSdkNotFoundException, CloudSdkOutOfDateException, CloudSdkVersionFileException {
    new CloudSdk.Builder().javaHome(Paths.get("/fake/path")).build().validateCloudSdk();
  }

  @Test
  public void testMinimumCloudSdkVersion() {
    // 160.0 through 170.0 have serious bugs on Windows
    assertTrue(CloudSdk.MINIMUM_VERSION.compareTo(new CloudSdkVersion("170.0.0")) > 0);
  }

  @Test
  public void testGetVersion_fileNotExists() {
    try {
      sdk.getVersion();
      fail();
    } catch (CloudSdkVersionFileException e) {
      assertEquals(
          "Cloud SDK version file not found at " + root.resolve("VERSION"), e.getMessage());
    }
  }

  @Test
  public void testGetVersion_fileContentInvalid() throws IOException {
    String fileContents = "this is not a valid version string";
    writeVersionFile(fileContents);
    try {
      sdk.getVersion();
      fail();
    } catch (CloudSdkVersionFileException ex) {
      assertEquals(
          "Pattern found in the Cloud SDK version file could not be parsed: " + fileContents,
          ex.getMessage());
    }
  }

  @Test
  public void testValidateCloudSdk_versionFileContentInvalid()
      throws IOException, CloudSdkNotFoundException, CloudSdkOutOfDateException,
          CloudSdkVersionFileException {
    String fileContents = "this is not a valid version string";
    writeVersionFile(fileContents);
    root.resolve("bin").toFile().mkdir();
    // fake SDK contents
    root.resolve("bin/gcloud").toFile().createNewFile();
    root.resolve("bin/gcloud.cmd").toFile().createNewFile(); // for Windows
    root.resolve("bin/dev_appserver.py").toFile().createNewFile();
    sdk.validateCloudSdk();
  }

  @Test
  public void testGetVersion_fileContentValid() throws IOException, CloudSdkVersionFileException {
    String version = "136.0.0";
    writeVersionFile(version);
    assertEquals(version, sdk.getVersion().toString());
  }

  @Test
  public void testValidateAppEngineJavaComponents()
      throws AppEngineJavaComponentsNotInstalledException, CloudSdkNotFoundException {
    new CloudSdk.Builder().build().validateAppEngineJavaComponents();
  }

  @Test
  public void testGetWindowsPythonPath() {
    assertThat(sdk.getWindowsPythonPath().toString(), anyOf(is("python"), endsWith("python.exe")));
  }

  @Test
  public void testAppEngineSdkForJavaPath() {
    assertEquals(
        root.resolve("platform/google_appengine/google/appengine/tools/java/lib"),
        sdk.getAppEngineSdkForJavaPath());
  }

  @Test
  public void testGetJarPathJavaTools() {
    assertEquals(
        root.resolve(
            "platform/google_appengine/google/appengine"
                + "/tools/java/lib/appengine-tools-api.jar"),
        sdk.getJarPath("appengine-tools-api.jar"));
  }

  @Test
  public void testResolversOrdering() throws CloudSdkNotFoundException {
    CloudSdkResolver r1 = mock(CloudSdkResolver.class, "r1");
    when(r1.getRank()).thenReturn(0);
    when(r1.getCloudSdkPath()).thenReturn(Paths.get("/r1"));
    CloudSdkResolver r2 = mock(CloudSdkResolver.class, "r2");
    when(r2.getRank()).thenReturn(10);
    CloudSdkResolver r3 = mock(CloudSdkResolver.class, "r3");
    when(r3.getRank()).thenReturn(100);

    CloudSdk.Builder builder = new CloudSdk.Builder().resolvers(Arrays.asList(r3, r2, r1));
    List<CloudSdkResolver> resolvers = builder.getResolvers();
    assertEquals(r1, resolvers.get(0));
    assertEquals(r2, resolvers.get(1));
    assertEquals(r3, resolvers.get(2));

    CloudSdk sdk = builder.build();
    assertEquals(r1.getCloudSdkPath(), sdk.getPath());
  }

  @Test
  public void testResolverCascading() throws CloudSdkNotFoundException {
    CloudSdkResolver r1 = mock(CloudSdkResolver.class, "r1");
    when(r1.getRank()).thenReturn(0);
    when(r1.getCloudSdkPath()).thenReturn(null);
    CloudSdkResolver r2 = mock(CloudSdkResolver.class, "r2");
    when(r2.getRank()).thenReturn(10);
    when(r2.getCloudSdkPath()).thenReturn(Paths.get("/r2"));

    CloudSdk.Builder builder = new CloudSdk.Builder().resolvers(Arrays.asList(r1, r2));
    List<CloudSdkResolver> resolvers = builder.getResolvers();
    assertEquals(r1, resolvers.get(0));
    assertEquals(r2, resolvers.get(1));

    CloudSdk sdk = builder.build();
    assertEquals("r1 should not resolve", r2.getCloudSdkPath(), sdk.getPath());
  }

  @Test
  public void testGetJavaBinary() throws CloudSdkNotFoundException {
    CloudSdk sdk = new CloudSdk.Builder().javaHome(Paths.get("java", "path")).build();
    assertEquals(
        Paths.get(
                "java",
                "path",
                "bin",
                System.getProperty("os.name").contains("Windows") ? "java.exe" : "java")
            .toAbsolutePath(),
        sdk.getJavaExecutablePath());
  }
}
