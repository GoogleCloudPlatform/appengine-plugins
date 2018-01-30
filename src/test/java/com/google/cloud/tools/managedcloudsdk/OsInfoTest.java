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

/** Tests for {@link OsInfo}. */
public class OsInfoTest {

  @Test
  public void testGetSystemOs_windows() throws UnsupportedOsException {
    Assert.assertEquals(OsInfo.Name.WINDOWS, OsInfo.getSystemOs("Windows"));
    Assert.assertEquals(OsInfo.Name.WINDOWS, OsInfo.getSystemOs("windows"));
    Assert.assertEquals(OsInfo.Name.WINDOWS, OsInfo.getSystemOs("windows 300"));
  }

  @Test
  public void testGetSystemOs_linux() throws UnsupportedOsException {
    Assert.assertEquals(OsInfo.Name.LINUX, OsInfo.getSystemOs("Linux"));
    Assert.assertEquals(OsInfo.Name.LINUX, OsInfo.getSystemOs("linux"));
    Assert.assertEquals(OsInfo.Name.LINUX, OsInfo.getSystemOs("linux 300"));
  }

  @Test
  public void testGetSystemOs_mac() throws UnsupportedOsException {
    Assert.assertEquals(OsInfo.Name.MAC, OsInfo.getSystemOs("Mac OS X"));
    Assert.assertEquals(OsInfo.Name.MAC, OsInfo.getSystemOs("mac os x"));
    Assert.assertEquals(OsInfo.Name.MAC, OsInfo.getSystemOs("mac os x 300"));
    Assert.assertEquals(OsInfo.Name.MAC, OsInfo.getSystemOs("macOS"));
    Assert.assertEquals(OsInfo.Name.MAC, OsInfo.getSystemOs("macos"));
    Assert.assertEquals(OsInfo.Name.MAC, OsInfo.getSystemOs("macos 300"));
    Assert.assertEquals(OsInfo.Name.MAC, OsInfo.getSystemOs("Darwin"));
    Assert.assertEquals(OsInfo.Name.MAC, OsInfo.getSystemOs("darwin"));
    Assert.assertEquals(OsInfo.Name.MAC, OsInfo.getSystemOs("darwin 300"));
  }

  @Test
  public void testGetSystemOs_unsupported() {
    try {
      OsInfo.getSystemOs("BadOs 10.3");
      Assert.fail("UnsupportedOsException expected but not thrown.");
    } catch (UnsupportedOsException ex) {
      Assert.assertEquals("Unknown OS: BadOs 10.3", ex.getMessage());
    }
  }

  @Test
  public void testGetSystemArchitecture_is64() {
    Assert.assertEquals(OsInfo.Architecture.X86_64, OsInfo.getSystemArchitecture("64"));
    Assert.assertEquals(OsInfo.Architecture.X86_64, OsInfo.getSystemArchitecture("universal"));
    Assert.assertEquals(OsInfo.Architecture.X86_64, OsInfo.getSystemArchitecture("junk64Junk"));
    Assert.assertEquals(
        OsInfo.Architecture.X86_64, OsInfo.getSystemArchitecture("junkUniversaljunk"));
  }

  @Test
  public void testGetSystemArchitecture_defaultIs32() {
    Assert.assertEquals(OsInfo.Architecture.X86, OsInfo.getSystemArchitecture("32"));
    Assert.assertEquals(OsInfo.Architecture.X86, OsInfo.getSystemArchitecture("junk32junk"));
    Assert.assertEquals(OsInfo.Architecture.X86, OsInfo.getSystemArchitecture("junk"));
  }
}
