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
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class SemanticVersionTest {

  @Test
  public void testConstructor_invalid() {
    List<String> invalids = Arrays.asList(null, "", "v1beta3-1.0.0", "132.alpha-1.0",
        "132alpha-1.0", "1.0", "0.1.0-beta.2+build");
    int thrown = 0;
    for (String invalid : invalids) {
      try {
        new SemanticVersion(invalid);
      } catch (IllegalArgumentException exception) {
        thrown++;
      }
    }
    assertEquals(invalids.size(), thrown);
  }

  @Test
  public void testConstructor_simple() {
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

    SemanticVersion version2 = new SemanticVersion("0.1.0-beta2.alpha.1");
    assertEquals(0, version2.getMajorVersion());
    assertEquals(1, version2.getMinorVerion());
    assertEquals(0, version2.getPatchVersion());
    assertEquals(new SemanticVersionPreRelease("beta2.alpha.1"), version2.getPreRelease());
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
    assertEquals("beta.1.0", version.getPreRelease());
  }

  @Test
  public void testToString() {
    List<String> versions = ImmutableList.of("0.1.0-rc22", "1.0.1+33221", "0.0.1");
    for (String version : versions) {
      assertEquals(version, new SemanticVersion(version).toString());
    }
  }

  @Test
  public void testEquals_differentSizes() {
    assertTrue(new SemanticVersion("0.1.0").equals(new SemanticVersion("0.1")));
    assertTrue(new SemanticVersion("0").equals(new SemanticVersion("0.0.0.0")));
    assertFalse(new SemanticVersion("1.1").equals(new SemanticVersion("1.10")));
  }

  @Test
  public void testEquals_preRelease() {
    // TODO(alexsloan): implement and assert true semver comparisons, such that prerelease suffixes
    // are compared according to the semver spec (semver.org)
    assertEquals(new SemanticVersion("0.1.0-rc.1"), new SemanticVersion("0.1.0-rc.1"));
    assertEquals(new SemanticVersion("0.1.0-rc.1"),
        new SemanticVersion("0.1.0-release-anystring.x.y.z"));
    assertEquals(new SemanticVersion("0.1.0+12345678-beta.1"),
        new SemanticVersion("0.1.0-something"));
  }

  @Test
  public void testEquals_same() {
    assertTrue(new SemanticVersion("0.1.0").equals(new SemanticVersion("0.1.0")));
  }

  @Test
  public void testEquals_refEqual() {
    SemanticVersion v1 = new SemanticVersion("1");
    SemanticVersion v2 = v1;
    assertTrue(v1.equals(v2));
  }

  @Test
  public void testCompareTo_sort() {
    List<SemanticVersion> ordered = Arrays.asList(new SemanticVersion("1.0.0-alpha"),
        new SemanticVersion("1.0.0-alpha.1"), new SemanticVersion("1.0.0-alpha.beta"),
        new SemanticVersion("1.0.0-beta"), new SemanticVersion("1.0.0-beta.2"),
        new SemanticVersion("1.0.0-beta.11"), new SemanticVersion("1.0.0-rc.1"),
        new SemanticVersion("1.0.0"));
    List<SemanticVersion> copy = new ArrayList<>(ordered);
    Collections.shuffle(copy);
    Collections.sort(copy);

    assertEquals(ordered, copy);
  }

  @Test
  public void testCompareTo_equal() {
    String firstVersion = "0.1";
    String secondVersion = "0.1.0";
    SemanticVersion first = new SemanticVersion(firstVersion);
    SemanticVersion second = new SemanticVersion(secondVersion);

    // make sure that objects with different lengths can be compared, and that toString returns the
    // original version String passed to the object's constructor
    assertEquals(0, first.compareTo(second));
    assertEquals(firstVersion, first.toString());
    assertEquals(secondVersion, second.toString());
  }

  @Test
  public void testCompareTo_preRelease() {
    assertEquals(-1, new SemanticVersion("1.1.0-alpha-01")
        .compareTo(new SemanticVersion("2.1.0-beta2+123456")));
    assertEquals(-1, new SemanticVersion("1.1.0-01-asdf-beta")
        .compareTo(new SemanticVersion("2.1.0-beta2+123456")));
  }
}
