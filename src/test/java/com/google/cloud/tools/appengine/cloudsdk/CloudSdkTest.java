package com.google.cloud.tools.appengine.cloudsdk;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertEquals;

import com.google.cloud.tools.appengine.api.AppEngineException;
import com.google.cloud.tools.appengine.cloudsdk.process.ProcessOutputLineListener;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Unit tests for {@link CloudSdk}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CloudSdkTest {
  @Mock
  ProcessOutputLineListener outputListener;

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

  @Test
  public void testNewCloudSdk_nullWaitingOutputListener() {
    CloudSdk sdk = new CloudSdk.Builder().sdkPath(Paths.get("/"))
        .addStdOutLineListener(outputListener).runDevAppServerWait(10).async(false).build();

    assertNull(sdk.getRunDevAppServerWaitListener());

    sdk = new CloudSdk.Builder().addStdOutLineListener(outputListener)
        .runDevAppServerWait(0).async(true).build();

    assertNull(sdk.getRunDevAppServerWaitListener());
  }

  @Test
  public void testNewCloudSdk_outListener() {
    CloudSdk.Builder sdkBuilder = new CloudSdk.Builder().sdkPath(Paths.get("/"))
        .addStdOutLineListener(outputListener).runDevAppServerWait(10).async(true);

    CloudSdk sdk = sdkBuilder.build();

    assertNotNull(sdk.getRunDevAppServerWaitListener());
    assertEquals(2, sdkBuilder.getStdOutLineListeners().size());
    assertEquals(1, sdkBuilder.getStdErrLineListeners().size());
    assertEquals(1, sdkBuilder.getExitListeners().size());
  }

  @Test
  public void testNewCloudSdk_errListener() {
    CloudSdk.Builder sdkBuilder = new CloudSdk.Builder().sdkPath(Paths.get("/"))
        .addStdErrLineListener(outputListener).runDevAppServerWait(10).async(true);

    CloudSdk sdk = sdkBuilder.build();

    assertNotNull(sdk.getRunDevAppServerWaitListener());
    assertEquals(1, sdkBuilder.getStdOutLineListeners().size());
    assertEquals(2, sdkBuilder.getStdErrLineListeners().size());
    assertEquals(1, sdkBuilder.getExitListeners().size());
  }

  @Test(expected = AppEngineException.class)
  public void testNewCloudSdk_inheritOutputAndOutListener() {
    new CloudSdk.Builder().sdkPath(Paths.get("/"))
        .inheritProcessOutput(true).addStdOutLineListener(outputListener).build();
  }

  @Test(expected = AppEngineException.class)
  public void testNewCloudSdk_inheritOutputAndErrListener() {
    new CloudSdk.Builder().sdkPath(Paths.get("/"))
        .inheritProcessOutput(true).addStdErrLineListener(outputListener).build();
  }
}
