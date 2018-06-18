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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.ImmutableList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class CloudSdkVersionTest {

  @Test
  public void testConstructor_null() {
    try {
      new CloudSdkVersion(null);
      Assert.fail();
    } catch (NullPointerException ex) {
      Assert.assertNotNull(ex.getMessage());
    }
  }

  @Test
  public void testConstructor_emptyString() {
    try {
      new CloudSdkVersion("");
      Assert.fail();
    } catch (IllegalArgumentException ex) {
      Assert.assertNotNull(ex.getMessage());
    }
  }

  @Test
  public void testConstructor_head() {
    CloudSdkVersion version = new CloudSdkVersion("HEAD");
    assertEquals("HEAD", version.toString());
    assertNull(version.getPreRelease());
    assertNull(version.getBuildIdentifier());
  }

  @Test
  public void testConstructor_preReleaseBeforeNumber() {
    try {
      new CloudSdkVersion("v1.beta.3-1.0.0");
      Assert.fail();
    } catch (IllegalArgumentException ex) {
      Assert.assertNotNull(ex.getMessage());
    }
  }

  @Test
  public void testConstructor_missingRequiredNumbers() {
    try {
      new CloudSdkVersion("1.0");
      Assert.fail();
    } catch (IllegalArgumentException ex) {
      Assert.assertNotNull(ex.getMessage());
    }
  }

  @Test
  public void testConstructor_requiredNumbersOnly() {
    CloudSdkVersion version = new CloudSdkVersion("0.1.0");
    assertEquals(0, version.majorVersion);
    assertEquals(1, version.minorVersion);
    assertEquals(0, version.patchVersion);
    assertNull(version.getBuildIdentifier());
    assertNull(version.getPreRelease());
  }

  @Test
  public void testConstructor_withPreRelease() {
    CloudSdkVersion version = new CloudSdkVersion("0.1.0-beta");
    assertEquals(0, version.majorVersion);
    assertEquals(1, version.minorVersion);
    assertEquals(0, version.patchVersion);
    assertEquals(new CloudSdkVersionPreRelease("beta"), version.getPreRelease());
    assertNull(version.getBuildIdentifier());
  }

  @Test
  public void testConstructor_withBuild() {
    CloudSdkVersion version = new CloudSdkVersion("0.1.0+12345v0");
    assertEquals(0, version.majorVersion);
    assertEquals(1, version.minorVersion);
    assertEquals(0, version.patchVersion);
    assertEquals("12345v0", version.getBuildIdentifier());
    assertNull(version.getPreRelease());
  }

  @Test
  public void testConstructor_withPreReleaseAndBuild() {
    CloudSdkVersion version = new CloudSdkVersion("0.1.0-beta.1.0+22xyz331");
    assertEquals(0, version.majorVersion);
    assertEquals(1, version.minorVersion);
    assertEquals(0, version.patchVersion);
    assertEquals("22xyz331", version.getBuildIdentifier());
    assertEquals("beta.1.0", version.getPreRelease().toString());
  }

  @Test
  public void testConstructor_buildBeforePreRelease() {
    CloudSdkVersion version = new CloudSdkVersion("0.1.0+v01234-beta.1");
    assertEquals(0, version.majorVersion);
    assertEquals(1, version.minorVersion);
    assertEquals(0, version.patchVersion);
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
  public void testEquals_heads() {
    assertEquals(new CloudSdkVersion("HEAD"), new CloudSdkVersion("HEAD"));
  }

  @Test
  public void testEquals_headAndNonHead() {
    assertNotEquals(new CloudSdkVersion("0.1.0-rc.1+123"), new CloudSdkVersion("HEAD"));
    assertNotEquals(new CloudSdkVersion("HEAD"), new CloudSdkVersion("1.0.0"));
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

  @Test
  public void testCompareTo_heads() {
    assertEquals(0, new CloudSdkVersion("HEAD").compareTo(new CloudSdkVersion("HEAD")));
  }

  @Test
  public void testCompareTo_headIsGreater() {
    assertTrue(new CloudSdkVersion("HEAD").compareTo(new CloudSdkVersion("0.1.0-alpha.1.0.1")) > 0);
    assertTrue(new CloudSdkVersion("0.1.0").compareTo(new CloudSdkVersion("HEAD")) < 0);
  }

  @Test
  public void testMajorVersionParsing() {
    CloudSdkVersion major0 = new CloudSdkVersion("0.99.88");
    CloudSdkVersion major1 = new CloudSdkVersion("1.77.66");
    CloudSdkVersion major2 = new CloudSdkVersion("2.55.44");
    CloudSdkVersion major3 = new CloudSdkVersion("3.33.22");
    CloudSdkVersion major4 = new CloudSdkVersion("4.11.0");
    CloudSdkVersion major5 = new CloudSdkVersion("5.0.11");
    CloudSdkVersion major6 = new CloudSdkVersion("6.22.33");
    CloudSdkVersion major7 = new CloudSdkVersion("7.44.55");
    assertTrue(major0.compareTo(major1) < 0);
    assertTrue(major1.compareTo(major2) < 0);
    assertTrue(major2.compareTo(major3) < 0);
    assertTrue(major3.compareTo(major4) < 0);
    assertTrue(major4.compareTo(major5) < 0);
    assertTrue(major5.compareTo(major6) < 0);
    assertTrue(major6.compareTo(major7) < 0);

    assertTrue(major7.compareTo(major6) > 0);
    assertTrue(major6.compareTo(major5) > 0);
    assertTrue(major5.compareTo(major4) > 0);
    assertTrue(major4.compareTo(major3) > 0);
    assertTrue(major3.compareTo(major2) > 0);
    assertTrue(major2.compareTo(major1) > 0);
    assertTrue(major1.compareTo(major0) > 0);
  }

  @Test
  public void testMinorVersionParsing() {
    CloudSdkVersion minor0 = new CloudSdkVersion("0.0.88");
    CloudSdkVersion minor1 = new CloudSdkVersion("0.1.66");
    CloudSdkVersion minor2 = new CloudSdkVersion("0.2.44");
    CloudSdkVersion minor3 = new CloudSdkVersion("0.3.22");
    CloudSdkVersion minor4 = new CloudSdkVersion("0.4.0");
    CloudSdkVersion minor5 = new CloudSdkVersion("0.5.11");
    CloudSdkVersion minor6 = new CloudSdkVersion("0.6.33");
    CloudSdkVersion minor7 = new CloudSdkVersion("0.7.55");
    assertTrue(minor0.compareTo(minor1) < 0);
    assertTrue(minor1.compareTo(minor2) < 0);
    assertTrue(minor2.compareTo(minor3) < 0);
    assertTrue(minor3.compareTo(minor4) < 0);
    assertTrue(minor4.compareTo(minor5) < 0);
    assertTrue(minor5.compareTo(minor6) < 0);
    assertTrue(minor6.compareTo(minor7) < 0);

    assertTrue(minor7.compareTo(minor6) > 0);
    assertTrue(minor6.compareTo(minor5) > 0);
    assertTrue(minor5.compareTo(minor4) > 0);
    assertTrue(minor4.compareTo(minor3) > 0);
    assertTrue(minor3.compareTo(minor2) > 0);
    assertTrue(minor2.compareTo(minor1) > 0);
    assertTrue(minor1.compareTo(minor0) > 0);
  }

  @Test
  public void testPatchVersionParsing() {
    CloudSdkVersion patch0 = new CloudSdkVersion("0.99.0");
    CloudSdkVersion patch1 = new CloudSdkVersion("0.99.1");
    CloudSdkVersion patch2 = new CloudSdkVersion("0.99.2");
    CloudSdkVersion patch3 = new CloudSdkVersion("0.99.3");
    CloudSdkVersion patch4 = new CloudSdkVersion("0.99.4");
    CloudSdkVersion patch5 = new CloudSdkVersion("0.99.5");
    CloudSdkVersion patch6 = new CloudSdkVersion("0.99.6");
    CloudSdkVersion patch7 = new CloudSdkVersion("0.99.7");
    assertTrue(patch0.compareTo(patch1) < 0);
    assertTrue(patch1.compareTo(patch2) < 0);
    assertTrue(patch2.compareTo(patch3) < 0);
    assertTrue(patch3.compareTo(patch4) < 0);
    assertTrue(patch4.compareTo(patch5) < 0);
    assertTrue(patch5.compareTo(patch6) < 0);
    assertTrue(patch6.compareTo(patch7) < 0);

    assertTrue(patch7.compareTo(patch6) > 0);
    assertTrue(patch6.compareTo(patch5) > 0);
    assertTrue(patch5.compareTo(patch4) > 0);
    assertTrue(patch4.compareTo(patch3) > 0);
    assertTrue(patch3.compareTo(patch2) > 0);
    assertTrue(patch2.compareTo(patch1) > 0);
    assertTrue(patch1.compareTo(patch0) > 0);
  }

  @Test
  public void testHashCode_heads() {
    assertEquals(new CloudSdkVersion("HEAD"), new CloudSdkVersion("HEAD"));
  }

  @Test
  public void testHashCode_headAndNonHead() {
    assertNotEquals(new CloudSdkVersion("HEAD"), new CloudSdkVersion("1.23.98"));
  }
}
