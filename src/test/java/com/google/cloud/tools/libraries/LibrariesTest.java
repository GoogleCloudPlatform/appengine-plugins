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

package com.google.cloud.tools.libraries;

import static org.hamcrest.collection.IsArrayContaining.hasItemInArray;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonReaderFactory;
import javax.json.JsonString;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class LibrariesTest {

  private JsonObject[] apis;

  @Before
  public void parseJson() {
    JsonReaderFactory factory = Json.createReaderFactory(null);
    InputStream in = LibrariesTest.class.getResourceAsStream("libraries.json");
    JsonReader reader = factory.createReader(in);
    apis = reader.readArray().toArray(new JsonObject[0]);
  }

  @Test
  public void testJson() throws IOException {
    Assert.assertTrue(apis.length > 0);
    for (int i = 0; i < apis.length; i++) {
      assertApi(apis[i]);
    }
  }

  private static final String[] statuses = {"early access", "alpha", "beta", "GA", "deprecated"};

  private static void assertApi(JsonObject api) throws IOException {
    String id = api.getString("id");
    Assert.assertTrue(id.matches("[a-z]+"));
    Assert.assertFalse(api.getString("serviceName").isEmpty());
    Assert.assertFalse(api.getString("name").isEmpty());
    Assert.assertFalse(api.getString("name").contains("Google"));
    Assert.assertFalse(api.getString("description").isEmpty());
    String transports = api.getJsonArray("transports").getString(0);
    Assert.assertTrue(
        transports + " is not a recognized transport",
        "http".equals(transports) || "grpc".equals(transports));
    assertReachable(api.getString("documentation"));
    try {
      assertReachable(api.getString("icon"));
    } catch (NullPointerException ex) {
      // no icon element to test
    }
    JsonArray clients = api.getJsonArray("clients");
    Assert.assertFalse(clients.isEmpty());
    for (int i = 0; i < clients.size(); i++) {
      JsonObject client = (JsonObject) clients.get(i);
      String launchStage = client.getString("launchStage");
      Assert.assertThat(statuses, hasItemInArray(launchStage));
      try {
        assertReachable(client.getString("site"));
      } catch (NullPointerException ex) {
        // no site element to test
      }
      assertReachable(client.getString("apireference"));
      Assert.assertTrue(client.getString("languageLevel").matches("1\\.\\d+\\.\\d+"));
      Assert.assertFalse(client.getString("name").isEmpty());
      JsonString language = client.getJsonString("language");
      Assert.assertNotNull("Missing language in " + client.getString("name"), language);
      Assert.assertEquals("java", language.getString());
      JsonObject mavenCoordinates = client.getJsonObject("mavenCoordinates");
      String version = mavenCoordinates.getString("version");
      Assert.assertFalse(version.isEmpty());
      if ("beta".equals(launchStage) || "alpha".equals(launchStage)) {
        Assert.assertTrue(version.endsWith(launchStage));
      } else {
        Assert.assertTrue(version.matches("\\d+\\.\\d+\\.\\d+"));
      }
      Assert.assertFalse(mavenCoordinates.getString("artifactId").isEmpty());
      Assert.assertFalse(mavenCoordinates.getString("groupId").isEmpty());
      if (client.getString("source") != null) {
        assertReachable(client.getString("source"));
      }
    }
  }

  private static void assertReachable(String url) throws IOException {
    HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
    connection.setRequestMethod("HEAD");
    Assert.assertEquals("Could not reach " + url, 200, connection.getResponseCode());
  }

  @Test
  public void testDuplicates() {
    Map<String, String> apiCoordinates = Maps.newHashMap();
    Set<String> serviceNames = Sets.newHashSet();
    for (JsonObject api : apis) {
      String name = api.getString("name");
      String serviceName = api.getString("serviceName");
      if (apiCoordinates.containsKey(name)) {
        Assert.fail("name: " + name + " is defined twice");
      }
      if (serviceNames.contains(serviceName)) {
        Assert.fail("service name: " + serviceName + " is defined twice");
      }
      JsonObject coordinates =
          ((JsonObject) api.getJsonArray("clients").get(0)).getJsonObject("mavenCoordinates");
      String mavenCoordinates =
          coordinates.getString("groupId") + ":" + coordinates.getString("artifactId");
      if (apiCoordinates.containsValue(mavenCoordinates)) {
        Assert.fail(mavenCoordinates + " is defined twice");
      }
      apiCoordinates.put(name, mavenCoordinates);
      serviceNames.add(serviceName);
    }
  }

  @Test
  public void testServiceRoleMapping_hasNoDuplicateRoles() {
    for (JsonObject api : apis) {
      JsonArray serviceRoles = api.getJsonArray("serviceRoles");
      if (serviceRoles != null) {
        Set<String> roles = Sets.newHashSet();
        for (int i = 0; i < serviceRoles.size(); i++) {
          String role = serviceRoles.getString(i);
          if (roles.contains(role)) {
            Assert.fail("Role: " + role + " is defined multiple times");
          }
          roles.add(role);
        }
      }
    }
  }

  @Test
  public void testVersionExists() throws IOException {
    for (JsonObject api : apis) {
      JsonObject coordinates =
          ((JsonObject) api.getJsonArray("clients").get(0)).getJsonObject("mavenCoordinates");
      String repo =
          "https://repo1.maven.org/maven2/"
              + coordinates.getString("groupId").replace('.', '/')
              + "/"
              + coordinates.getString("artifactId")
              + "/"
              + coordinates.getString("version")
              + "/";
      assertReachable(repo);
    }
  }

  @Test
  public void testOrder() {
    List<String> names = new ArrayList<>();
    for (JsonObject api : apis) {
      names.add(api.getString("name"));
    }
    for (int i = 1; i < names.size(); i++) {
      String previous = names.get(i - 1).toLowerCase(Locale.US);
      String current = names.get(i).toLowerCase(Locale.US);
      Assert.assertTrue(current + " < " + previous, current.compareTo(previous) > 0);
    }
  }
}
