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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class CloudSdkVersionTest {

  @Test(expected = IllegalArgumentException.class)
  public void testConstructor_null() {
    new CloudSdkVersion(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testConstructor_emptyString() {
    new CloudSdkVersion("");
  }

  @Test(expected = NumberFormatException.class)
  public void testConstructor_nonNumeric() {
    new CloudSdkVersion("v1beta3-1.0.0");
  }

  @Test
  public void testToString() {
    String version = "0.1.0.22";
    assertEquals(version, new CloudSdkVersion(version).toString());
  }

  @Test
  public void testEquals_differentSizes() {
    assertTrue(new CloudSdkVersion("0.1.0").equals(new CloudSdkVersion("0.1")));
    assertTrue(new CloudSdkVersion("0").equals(new CloudSdkVersion("0.0.0.0")));
    assertFalse(new CloudSdkVersion("1.1").equals(new CloudSdkVersion("1.10")));
  }

  @Test
  public void testEquals_preRelease() {
    // TODO(alexsloan): implement and assert true semver comparisons, such that prerelease suffixes
    // are compared according to the semver spec (semver.org)
    assertEquals(new CloudSdkVersion("0.1.0-rc.1"), new CloudSdkVersion("0.1.0-rc.1"));
    assertEquals(new CloudSdkVersion("0.1.0-rc.1"),
        new CloudSdkVersion("0.1.0-release-anystring.x.y.z"));
    assertEquals(new CloudSdkVersion("0.1.0+12345678-beta.1"),
        new CloudSdkVersion("0.1.0-something"));
  }

  @Test
  public void testEquals_same() {
    assertTrue(new CloudSdkVersion("0.1.0").equals(new CloudSdkVersion("0.1.0")));
  }

  @Test
  public void testEquals_refEqual() {
    CloudSdkVersion v1 = new CloudSdkVersion("1");
    CloudSdkVersion v2 = v1;
    assertTrue(v1.equals(v2));
  }

  @Test
  public void testCompareTo_sort() {
    List<CloudSdkVersion> ordered = Arrays.asList(new CloudSdkVersion("0"),
        new CloudSdkVersion("0.0.1"), new CloudSdkVersion("0.1"), new CloudSdkVersion("0.1.1"),
        new CloudSdkVersion("0.1.2"), new CloudSdkVersion("0.1.2.1"), new CloudSdkVersion("1"));
    List<CloudSdkVersion> copy = new ArrayList<>(ordered);
    Collections.shuffle(copy);
    Collections.sort(copy);

    assertEquals(ordered, copy);
  }

  @Test
  public void testCompareTo_equal() {
    String firstVersion = "0.1";
    String secondVersion = "0.1.0";
    CloudSdkVersion first = new CloudSdkVersion(firstVersion);
    CloudSdkVersion second = new CloudSdkVersion(secondVersion);

    // make sure that objects with different lengths can be compared, and that toString returns the
    // original version String passed to the object's constructor
    assertEquals(0, first.compareTo(second));
    assertEquals(firstVersion, first.toString());
    assertEquals(secondVersion, second.toString());
  }

  @Test
  public void testCompareTo_preRelease() {
    assertEquals(-1, new CloudSdkVersion("1.1.0-alpha-01")
        .compareTo(new CloudSdkVersion("2.1.0-beta2+123456")));
    assertEquals(-1, new CloudSdkVersion("1.1.0-01-asdf-beta")
        .compareTo(new CloudSdkVersion("2.1.0-beta2+123456")));
  }
}
