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

package com.google.cloud.tools.appengine.cloudsdk.serialization;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.junit.Test;

public class CloudSdkVersionTest {

  @Test(expected = NullPointerException.class)
  public void testConstructor_null() {
    new CloudSdkVersion(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructor_emptyString() {
    new CloudSdkVersion("");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructor_preReleaseBeforeNumber() {
    new CloudSdkVersion("v1.beta.3-1.0.0");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructor_missingRequiredNumbers() {
    new CloudSdkVersion("1.0");
  }

  @Test
  public void testConstructor_requiredNumbersOnly() {
    CloudSdkVersion version = new CloudSdkVersion("0.1.0");
    assertEquals(0, version.getMajorVersion());
    assertEquals(1, version.getMinorVerion());
    assertEquals(0, version.getPatchVersion());
    assertNull(version.getBuildIdentifier());
    assertNull(version.getPreRelease());
  }

  @Test
  public void testConstructor_withPreRelease() {
    CloudSdkVersion version = new CloudSdkVersion("0.1.0-beta");
    assertEquals(0, version.getMajorVersion());
    assertEquals(1, version.getMinorVerion());
    assertEquals(0, version.getPatchVersion());
    assertEquals(new CloudSdkVersionPreRelease("beta"), version.getPreRelease());
    assertNull(version.getBuildIdentifier());
  }

  @Test
  public void testConstructor_withBuild() {
    CloudSdkVersion version = new CloudSdkVersion("0.1.0+12345v0");
    assertEquals(0, version.getMajorVersion());
    assertEquals(1, version.getMinorVerion());
    assertEquals(0, version.getPatchVersion());
    assertEquals("12345v0", version.getBuildIdentifier());
    assertNull(version.getPreRelease());
  }

  @Test
  public void testConstructor_withPreReleaseAndBuild() {
    CloudSdkVersion version = new CloudSdkVersion("0.1.0-beta.1.0+22xyz331");
    assertEquals(0, version.getMajorVersion());
    assertEquals(1, version.getMinorVerion());
    assertEquals(0, version.getPatchVersion());
    assertEquals("22xyz331", version.getBuildIdentifier());
    assertEquals("beta.1.0", version.getPreRelease().toString());
  }

  @Test
  public void testConstructor_buildBeforePreRelease() {
    CloudSdkVersion version = new CloudSdkVersion("0.1.0+v01234-beta.1");
    assertEquals(0, version.getMajorVersion());
    assertEquals(1, version.getMinorVerion());
    assertEquals(0, version.getPatchVersion());
    // the build identifier should match greedily
    assertEquals("v01234-beta.1", version.getBuildIdentifier());
    assertNull(version.getPreRelease());
  }

  @Test
  public void testToString() {
    List<String> versions = ImmutableList.of("0.1.0-rc22", "1.0.1+33221", "0.0.1");
    for (String version : versions) {
      assertEquals(version, new CloudSdkVersion(version).toString());
    }
  }

  @Test
  public void testEquals_requiredOnly() {
    assertTrue(new CloudSdkVersion("0.1.0").equals(new CloudSdkVersion("0.1.0")));
  }

  @Test
  public void testEquals_preRelease() {
    assertEquals(new CloudSdkVersion("0.1.0-rc.1"), new CloudSdkVersion("0.1.0-rc.1"));
    assertNotEquals(new CloudSdkVersion("0.1.0-rc.1"), new CloudSdkVersion("0.1.0-rc.2"));
  }

  @Test
  public void testEquals_buildNumbers() {
    assertEquals(new CloudSdkVersion("0.1.0-rc.1+123"), new CloudSdkVersion("0.1.0-rc.1+123"));
    assertNotEquals(new CloudSdkVersion("0.1.0-rc.1+123"), new CloudSdkVersion("0.1.0-rc.1+456"));
  }

  @Test
  public void testEquals_refEqual() {
    CloudSdkVersion v1 = new CloudSdkVersion("1.0.0");
    CloudSdkVersion v2 = v1;
    assertTrue(v1.equals(v2));
  }

  @Test
  public void testCompareTo_simple() {
    assertTrue(new CloudSdkVersion("0.1.0").compareTo(new CloudSdkVersion("1.1.0")) < 0);
  }

  @Test
  public void testCompareTo_preReleaseNumeric() {
    assertTrue(new CloudSdkVersion("1.0.0-1").compareTo(new CloudSdkVersion("1.0.0-2")) < 0);
  }

  @Test
  public void testCompareTo_preReleaseAlphaNumeric() {
    assertTrue(new CloudSdkVersion("1.0.0-a").compareTo(new CloudSdkVersion("1.0.0-b")) < 0);
  }

  @Test
  public void testCompareTo_preReleaseNumericVsAlpha() {
    assertTrue(
        new CloudSdkVersion("1.0.0-alpha.2").compareTo(new CloudSdkVersion("1.0.0-alpha.1-beta"))
            < 0);
  }

  @Test
  public void testCompareTo_differentBuildNumbers() {
    CloudSdkVersion first = new CloudSdkVersion("0.1.0+v1");
    CloudSdkVersion second = new CloudSdkVersion("0.1.0+v2");
    assertEquals(0, first.compareTo(second));
    assertEquals(0, second.compareTo(first));
  }

  @Test
  public void testCompareTo_preReleaseWithDifferentNumberOfFields() {
    assertTrue(
        new CloudSdkVersion("0.1.0-alpha").compareTo(new CloudSdkVersion("0.1.0-alpha.0")) < 0);
    assertTrue(
        new CloudSdkVersion("0.1.0-alpha.1.0.1").compareTo(new CloudSdkVersion("0.1.0-omega")) < 0);
  }
}
