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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.cloud.tools.appengine.cloudsdk.serialization.CloudSdkComponent.State;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import javax.annotation.Nullable;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

/** Unit tests for {@link CloudSdkComponent}. */
@RunWith(MockitoJUnitRunner.class)
public class CloudSdkComponentTest {

  @Test
  public void testFromJson() {
    CloudSdkComponent result = CloudSdkComponent.fromJson(getCloudSdkComponentTestFixtureAsJson());
    CloudSdkComponent expected = getCloudSdkComponentTestFixture();
    assertCloudSdkComponentsEqual(expected, result);
  }

  @Test
  public void testFromJsonList_nonempty() {
    String jsonList = "[" + getCloudSdkComponentTestFixtureAsJson() + "]";
    List<CloudSdkComponent> result = CloudSdkComponent.fromJsonList(jsonList);

    assertEquals(1, result.size());
    assertCloudSdkComponentsEqual(getCloudSdkComponentTestFixture(), result.get(0));
  }

  @Test
  public void testFromJsonList_empty() {
    String emptyList = "[]";
    List<CloudSdkComponent> result = CloudSdkComponent.fromJsonList(emptyList);

    assertEquals(0, result.size());
  }

  @Test
  public void testToJson() {
    CloudSdkComponent cloudSdkComponent = getCloudSdkComponentTestFixture();
    String result = cloudSdkComponent.toJson();

    // Since the ordering of fields in JSON objects is not guaranteed, we cannot compare the full
    // strings for equality. Instead, use regexes to validate that key/value pairs are present.
    assertJsonKeyValueExists(
        "current_version_string", cloudSdkComponent.getCurrentVersion(), result);
    assertJsonKeyValueExists("id", cloudSdkComponent.getId(), result);
    Boolean isConfiguration = cloudSdkComponent.getIsConfiguration();
    assertNotNull(isConfiguration);
    assertJsonKeyValueExists("is_configuration", isConfiguration, result);
    Boolean isHidden = cloudSdkComponent.getIsHidden();
    assertNotNull(isHidden);
    assertJsonKeyValueExists("is_hidden", isHidden, result);
    String latestVersion = cloudSdkComponent.getLatestVersion();
    assertJsonKeyValueExists("latest_version_string", latestVersion, result);
    assertJsonKeyValueExists("name", cloudSdkComponent.getName(), result);
    Integer sizeInBytes = cloudSdkComponent.getSizeInBytes();
    assertNotNull(sizeInBytes);
    assertJsonKeyValueExists("size", sizeInBytes, result);
  }

  @Test
  public void testToAndFromJson() {
    CloudSdkComponent initial = getCloudSdkComponentTestFixture();
    String serialized = initial.toJson();
    CloudSdkComponent result = CloudSdkComponent.fromJson(serialized);
    assertCloudSdkComponentsEqual(initial, result);
  }

  @Test
  public void testStateIsInstalled_true() {
    CloudSdkComponent.State state = new CloudSdkComponent.State();
    state.setName("Installed");
    assertTrue(state.isInstalled());
  }

  @Test
  public void testStateIsInstalled_trueWithUpdateAvailable() {
    CloudSdkComponent.State state = new CloudSdkComponent.State();
    state.setName("Update Available");
    assertTrue(state.isInstalled());
  }

  @Test
  public void testStateIsInstalled_false() {
    List<String> notInstalledStates = Arrays.asList("Not Installed", "", null);
    for (String stateName : notInstalledStates) {
      CloudSdkComponent.State state = new CloudSdkComponent.State();
      state.setName(stateName);
      assertFalse(state.isInstalled());
    }
  }

  private void assertJsonKeyValueExists(
      String expectedKey, @Nullable String expectedValue, String result) {
    // unusual case where a null argument is allowed in order to check that it's not null
    assertNotNull(expectedValue);
    String regex = String.format(Locale.US, ".*%s\":\\s*\"%s\".*", expectedKey, expectedValue);
    assertTrue(result.matches(regex));
  }

  private void assertJsonKeyValueExists(String expectedKey, int expectedValue, String result) {
    String regex = String.format(Locale.US, ".*%s\":\\s*%s.*", expectedKey, expectedValue);
    assertTrue(result.matches(regex));
  }

  private void assertJsonKeyValueExists(String expectedKey, boolean expectedValue, String result) {
    String regex = String.format(Locale.US, ".*%s\":\\s*%s.*", expectedKey, expectedValue);
    assertTrue(result.matches(regex));
  }

  private static String getCloudSdkComponentTestFixtureAsJson() {
    return "{"
        + "\"current_version_string\": \"1.9.43\","
        + "\"id\": \"app-engine-java\","
        + "\"is_configuration\": false,"
        + "\"is_hidden\": false,"
        + "\"latest_version_string\": \"1.9.44\","
        + "\"name\": \"gcloud app Java Extensions\","
        + "\"size\": 138442691,"
        + "\"state\": { "
        + "  \"name\": \"Installed\" "
        + "}"
        + "}";
  }

  private static CloudSdkComponent getCloudSdkComponentTestFixture() {
    CloudSdkComponent.State state = new CloudSdkComponent.State();
    state.setName("Installed");

    CloudSdkComponent expected = new CloudSdkComponent();
    expected.setState(state);
    expected.setCurrentVersion("1.9.43");
    expected.setId("app-engine-java");
    expected.setIsConfiguration(false);
    expected.setIsHidden(false);
    expected.setLatestVersionString("1.9.44");
    expected.setName("gcloud app Java Extensions");
    expected.setSizeInBytes(138442691);

    return expected;
  }

  private static void assertCloudSdkComponentsEqual(
      CloudSdkComponent expected, CloudSdkComponent actual) {
    assertEquals(expected.getCurrentVersion(), actual.getCurrentVersion());
    assertEquals(expected.getId(), actual.getId());
    assertEquals(expected.getIsConfiguration(), actual.getIsConfiguration());
    assertEquals(expected.getIsHidden(), actual.getIsHidden());
    assertEquals(expected.getLatestVersion(), actual.getLatestVersion());
    assertEquals(expected.getName(), actual.getName());
    assertEquals(expected.getSizeInBytes(), actual.getSizeInBytes());
    State expectedState = expected.getState();
    if (expectedState != null) {
      State actualState = actual.getState();
      assertNotNull(actualState);
      assertEquals(expectedState.getName(), actualState.getName());
    }
  }
}
