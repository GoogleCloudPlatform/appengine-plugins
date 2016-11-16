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

package com.google.cloud.tools.util.semver;

import com.google.common.collect.ImmutableList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static junit.framework.TestCase.assertNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class SemanticVersionTest {

  @Test(expected = IllegalArgumentException.class)
  public void testConstructor_null() {
    new SemanticVersion(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructor_emptyString() {
    new SemanticVersion("");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructor_preReleaseBeforeNumber() {
    new SemanticVersion("v1.beta.3-1.0.0");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructor_missingRequiredNumbers() {
    new SemanticVersion("1.0");
  }

  @Test
  public void testConstructor_requiredNumbersOnly() {
    SemanticVersion version = new SemanticVersion("0.1.0");
    assertEquals(0, version.getMajorVersion());
    assertEquals(1, version.getMinorVerion());
    assertEquals(0, version.getPatchVersion());
    assertNull(version.getBuild());
    assertNull(version.getPreRelease());
  }

  @Test
  public void testConstructor_withPreRelease() {
    SemanticVersion version = new SemanticVersion("0.1.0-beta");
    assertEquals(0, version.getMajorVersion());
    assertEquals(1, version.getMinorVerion());
    assertEquals(0, version.getPatchVersion());
    assertEquals(new SemanticVersionPreRelease("beta"), version.getPreRelease());
    assertNull(version.getBuild());
  }

  @Test
  public void testConstructor_withBuild() {
    SemanticVersion version = new SemanticVersion("0.1.0+12345v0");
    assertEquals(0, version.getMajorVersion());
    assertEquals(1, version.getMinorVerion());
    assertEquals(0, version.getPatchVersion());
    assertEquals("12345v0", version.getBuild());
    assertNull(version.getPreRelease());
  }

  @Test
  public void testConstructor_withPreReleaseAndBuild() {
    SemanticVersion version = new SemanticVersion("0.1.0-beta.1.0+22xyz331");
    assertEquals(0, version.getMajorVersion());
    assertEquals(1, version.getMinorVerion());
    assertEquals(0, version.getPatchVersion());
    assertEquals("22xyz331", version.getBuild());
    assertEquals("beta.1.0", version.getPreRelease().toString());
  }

  @Test
  public void testConstructor_buildBeforePreRelease() {
    SemanticVersion version = new SemanticVersion("0.1.0+v01234-beta.1");
    assertEquals(0, version.getMajorVersion());
    assertEquals(1, version.getMinorVerion());
    assertEquals(0, version.getPatchVersion());
    // the build identifier should match greedily
    assertEquals("v01234-beta.1", version.getBuild());
    assertNull(version.getPreRelease());
  }

  @Test
  public void testToString() {
    List<String> versions = ImmutableList.of("0.1.0-rc22", "1.0.1+33221", "0.0.1");
    for (String version : versions) {
      assertEquals(version, new SemanticVersion(version).toString());
    }
  }

  @Test
  public void testEquals_requiredOnly() {
    assertTrue(new SemanticVersion("0.1.0").equals(new SemanticVersion("0.1.0")));
  }

  @Test
  public void testEquals_preRelease() {
    assertEquals(new SemanticVersion("0.1.0-rc.1"), new SemanticVersion("0.1.0-rc.1"));
    assertNotEquals(new SemanticVersion("0.1.0-rc.1"), new SemanticVersion("0.1.0-rc.2"));
  }

  @Test
  public void testEquals_buildNumbers() {
    assertEquals(new SemanticVersion("0.1.0-rc.1+123"), new SemanticVersion("0.1.0-rc.1+123"));
    // build numbers are not considered for comparison purposes
    assertEquals(new SemanticVersion("0.1.0-rc.1+123"), new SemanticVersion("0.1.0-rc.1+456"));
  }

  @Test
  public void testEquals_refEqual() {
    SemanticVersion v1 = new SemanticVersion("1.0.0");
    SemanticVersion v2 = v1;
    assertTrue(v1.equals(v2));
  }

  @Test
  public void testCompareTo_simple() {
    assertTrue(new SemanticVersion("0.1.0").compareTo(new SemanticVersion("1.1.0")) < 0);
  }

  @Test
  public void testCompareTo_preReleaseNumeric() {
    assertTrue(new SemanticVersion("1.0.0-1")
        .compareTo(new SemanticVersion("1.0.0-2")) < 0);
  }

  @Test
  public void testCompareTo_preReleaseAlphaNumeric() {
    assertTrue(new SemanticVersion("1.0.0-a")
        .compareTo(new SemanticVersion("1.0.0-b")) < 0);
  }

  @Test
  public void testCompareTo_preReleaseNumericVsAlpha() {
    assertTrue(new SemanticVersion("1.0.0-alpha.2")
        .compareTo(new SemanticVersion("1.0.0-alpha.1-beta")) < 0);
  }

  @Test
  public void testCompareTo_differentBuildNumbers() {
    SemanticVersion first = new SemanticVersion("0.1.0+v1");
    SemanticVersion second = new SemanticVersion("0.1.0+v2");
    assertEquals(0, first.compareTo(second));
    assertEquals(0, second.compareTo(first));
  }

  @Test
  public void testCompareTo_preReleaseWithDifferentNumberOfFields() {
    assertTrue(new SemanticVersion("0.1.0-alpha")
        .compareTo(new SemanticVersion("0.1.0-alpha.0")) < 0);
    assertTrue(new SemanticVersion("0.1.0-alpha.1.0.1")
        .compareTo(new SemanticVersion("0.1.0-omega")) < 0);
  }
}
