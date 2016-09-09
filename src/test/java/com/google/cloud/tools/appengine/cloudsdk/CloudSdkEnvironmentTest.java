package com.google.cloud.tools.appengine.cloudsdk;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.nio.file.Files;

import org.junit.Test;

import com.google.cloud.tools.appengine.cloudsdk.internal.process.ProcessRunnerException;

/**
 * Integration tests for {@link CloudSdk} that require an installed CloudSdk instance. 
 */
public class CloudSdkEnvironmentTest {
  
  private CloudSdk sdk = new CloudSdk.Builder().build();

  @Test
  public void testGetSdkPath() {
    assertTrue(Files.exists(sdk.getSdkPath()));
  }
  
  @Test
  public void testIsComponentInstalled_true() throws ProcessRunnerException {
    assertTrue(sdk.isComponentInstalled("app-engine-java"));
  }

  @Test
  public void testIsComponentInstalled_False() throws ProcessRunnerException {
    assertFalse(sdk.isComponentInstalled("no-such-component"));
  }
  
  @Test
  public void testIsComponentInstalled_sequential() throws ProcessRunnerException {
    assertTrue(sdk.isComponentInstalled("app-engine-java"));
    assertFalse(sdk.isComponentInstalled("no-such-component"));
  }
  
}
