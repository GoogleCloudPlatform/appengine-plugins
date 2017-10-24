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

/** Tests for {@link OsType}. */
public class OsTypeTest {

  @Test
  public void testGetSystemOs_windows() throws UnsupportedOsException {
    Assert.assertEquals(OsType.WINDOWS, OsType.getSystemOs("Windows"));
    Assert.assertEquals(OsType.WINDOWS, OsType.getSystemOs("windows"));
    Assert.assertEquals(OsType.WINDOWS, OsType.getSystemOs("windows 300"));
  }

  @Test
  public void testGetSystemOs_linux() throws UnsupportedOsException {
    Assert.assertEquals(OsType.LINUX, OsType.getSystemOs("Linux"));
    Assert.assertEquals(OsType.LINUX, OsType.getSystemOs("linux"));
    Assert.assertEquals(OsType.LINUX, OsType.getSystemOs("linux 300"));
  }

  @Test
  public void testGetSystemOs_mac() throws UnsupportedOsException {
    Assert.assertEquals(OsType.MAC, OsType.getSystemOs("Mac OS X"));
    Assert.assertEquals(OsType.MAC, OsType.getSystemOs("mac os x"));
    Assert.assertEquals(OsType.MAC, OsType.getSystemOs("mac os x 300"));
    Assert.assertEquals(OsType.MAC, OsType.getSystemOs("macOS"));
    Assert.assertEquals(OsType.MAC, OsType.getSystemOs("macos"));
    Assert.assertEquals(OsType.MAC, OsType.getSystemOs("macos 300"));
    Assert.assertEquals(OsType.MAC, OsType.getSystemOs("Darwin"));
    Assert.assertEquals(OsType.MAC, OsType.getSystemOs("darwin"));
    Assert.assertEquals(OsType.MAC, OsType.getSystemOs("darwin 300"));
  }

  @Test
  public void testGetSystemOs_unsupported() {
    try {
      OsType.getSystemOs("BadOs 10.3");
      Assert.fail("UnsupportedOsException expected but not thrown.");
    } catch (UnsupportedOsException ex) {
      Assert.assertEquals("Unknown OS: BadOs 10.3", ex.getMessage());
    }
  }
}
