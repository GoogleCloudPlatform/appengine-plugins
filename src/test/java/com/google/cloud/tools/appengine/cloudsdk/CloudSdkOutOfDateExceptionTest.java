package com.google.cloud.tools.appengine.cloudsdk;

import org.junit.Assert;
import org.junit.Test;

import com.google.cloud.tools.appengine.cloudsdk.serialization.CloudSdkVersion;

public class CloudSdkOutOfDateExceptionTest {

  @Test
  public void testNoInstalledVersion() {
    CloudSdkVersion requiredVersion = new CloudSdkVersion("132.4.5");
    CloudSdkOutOfDateException ex = new CloudSdkOutOfDateException(requiredVersion);
    Assert.assertNull(ex.getInstalledVersion());
    Assert.assertEquals(requiredVersion, ex.getRequiredVersion());
    Assert.assertEquals("Cloud SDK versions before 132.4.5 are not supported", ex.getMessage());
  }

  @Test
  public void testInstalledVersion() {
    CloudSdkVersion installedVersion = new CloudSdkVersion("131.0.0");
    CloudSdkVersion requiredVersion = new CloudSdkVersion("133.0.0");
    CloudSdkOutOfDateException ex = new CloudSdkOutOfDateException(installedVersion, requiredVersion);
    Assert.assertEquals(installedVersion, ex.getInstalledVersion());
    Assert.assertEquals(requiredVersion, ex.getRequiredVersion());
    Assert.assertEquals("Requires version 133.0.0 or later but found version 131.0.0", ex.getMessage());
  }


}
