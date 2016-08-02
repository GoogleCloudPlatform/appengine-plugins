package com.google.cloud.tools.appengine.cloudsdk;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import com.google.cloud.tools.appengine.cloudsdk.CloudSdk.Builder;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

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

  @Test
  public void testResolversOrdering() {
    CloudSdkResolver r1 = Mockito.mock(CloudSdkResolver.class, "r1");
    when(r1.getRank()).thenReturn(0);
    when(r1.getCloudSdkPath()).thenReturn(Paths.get("/r1"));
    CloudSdkResolver r2 = Mockito.mock(CloudSdkResolver.class, "r2");
    when(r2.getRank()).thenReturn(10);
    when(r2.getCloudSdkPath()).thenReturn(Paths.get("/r2"));
    CloudSdkResolver r3 = Mockito.mock(CloudSdkResolver.class, "r3");
    when(r3.getRank()).thenReturn(100);
    when(r3.getCloudSdkPath()).thenReturn(Paths.get("/r3"));

    Builder builder = new CloudSdk.Builder().resolvers(Arrays.asList(r3, r2, r1));
    List<CloudSdkResolver> resolvers = builder.getResolvers();
    assertEquals(r1, resolvers.get(0));
    assertEquals(r2, resolvers.get(1));
    assertEquals(r3, resolvers.get(2));

    CloudSdk sdk = builder.build();
    assertEquals(r1.getCloudSdkPath(), sdk.getSdkPath());
  }

  @Test
  public void testResolverCascading() {
    CloudSdkResolver r1 = Mockito.mock(CloudSdkResolver.class, "r1");
    when(r1.getRank()).thenReturn(0);
    when(r1.getCloudSdkPath()).thenReturn(null);
    CloudSdkResolver r2 = Mockito.mock(CloudSdkResolver.class, "r2");
    when(r2.getRank()).thenReturn(10);
    when(r2.getCloudSdkPath()).thenReturn(Paths.get("/r2"));

    Builder builder = new CloudSdk.Builder().resolvers(Arrays.asList(r1, r2));
    List<CloudSdkResolver> resolvers = builder.getResolvers();
    assertEquals(r1, resolvers.get(0));
    assertEquals(r2, resolvers.get(1));

    CloudSdk sdk = builder.build();
    assertEquals("r1 should not resolve", r2.getCloudSdkPath(), sdk.getSdkPath());
  }
}
