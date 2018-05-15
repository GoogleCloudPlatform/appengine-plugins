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

package com.google.cloud.tools.managedcloudsdk;

import org.junit.Assert;
import org.junit.Test;

/** Tests for {@link com.google.cloud.tools.managedcloudsdk.Version} */
public class VersionTest {

  @Test
  public void testNewVersion_defaultLatest() {
    Assert.assertEquals("LATEST", Version.LATEST.getVersion());
  }

  @Test
  public void testNewVersion_valid() throws BadCloudSdkVersionException {
    Version version = new Version("111.000.000");
    Assert.assertEquals("111.000.000", version.getVersion());
  }

  @Test
  public void testNewVersion_invalid() {
    try {
      new Version("1");
      Assert.fail("did not detect invalid version");
    } catch (BadCloudSdkVersionException ex) {
      Assert.assertEquals(
          "Version must match [number].[number].[number] for example 100.0.0", ex.getMessage());
    }
  }
}
