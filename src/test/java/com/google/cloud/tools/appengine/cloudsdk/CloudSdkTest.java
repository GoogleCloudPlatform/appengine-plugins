package com.google.cloud.tools.appengine.cloudsdk;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Unit tests for {@link CloudSdk}
 */
@RunWith(MockitoJUnitRunner.class)
public class CloudSdkTest {
  @Test
  public void testGetSdkPath() {
    Path location = Paths.get("/");
    CloudSdk sdk = new CloudSdk.Builder().sdkPath(location).build();
    assertEquals(location, sdk.getSdkPath());
  }

  @Test
  public void testGetJavaAppEngineSdkPath() {
    Path location = Paths.get("/");
    CloudSdk sdk = new CloudSdk.Builder().sdkPath(location).build();
    assertEquals(location.resolve("platform/google_appengine/google/appengine/tools/java/lib"),
        sdk.getJavaAppEngineSdkPath());
  }

  @Test
  public void testGetJarPathJavaTools() {
    Path location = Paths.get("/");
    CloudSdk sdk = new CloudSdk.Builder().sdkPath(location).build();
    assertEquals(Paths.get("/platform/google_appengine/google/appengine"
        + "/tools/java/lib/appengine-tools-api.jar"),
        sdk.getJarPath("appengine-tools-api.jar"));
  }
}
