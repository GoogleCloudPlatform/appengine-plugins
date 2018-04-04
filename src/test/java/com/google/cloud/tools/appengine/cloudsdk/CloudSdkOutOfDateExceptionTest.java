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

package com.google.cloud.tools.appengine.cloudsdk;

import com.google.cloud.tools.appengine.cloudsdk.serialization.CloudSdkVersion;
import org.junit.Assert;
import org.junit.Test;

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
    CloudSdkOutOfDateException ex =
        new CloudSdkOutOfDateException(installedVersion, requiredVersion);
    Assert.assertEquals(installedVersion, ex.getInstalledVersion());
    Assert.assertEquals(requiredVersion, ex.getRequiredVersion());
    Assert.assertEquals(
        "Requires version 133.0.0 or later but found version 131.0.0", ex.getMessage());
  }
}
