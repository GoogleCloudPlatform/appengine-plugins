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

package com.google.cloud.tools.libraries.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.util.List;
import org.junit.Test;

/** Unit tests for {@link CloudLibrary}. */
public final class CloudLibraryTest {

  private static final String NAME = "My API";
  private static final String ID = "myapi";
  private static final String SERVICE_NAME = "myservice.googleapis.com";
  private static final String SERVICE_ROLE = "roles/myrole";
  private static final String DOCUMENTATION = "http://documentation";
  private static final String DESCRIPTION = "My example API";
  private static final String TRANSPORT = "grpc";
  private static final String ICON = "http://icon";
  private static final String CLIENT_NAME = "My API Client";
  private static final String CLIENT_LANGUAGE = "java";
  private static final String CLIENT_SITE = "http://client-site";
  private static final String CLIENT_API_REFERENCE = "http://api-reference";
  private static final String CLIENT_INFO_TIP = "info tip";
  private static final String CLIENT_LAUNCH_STAGE = "alpha";
  private static final String CLIENT_SOURCE = "http://client-source";
  private static final String CLIENT_LANGUAGE_LEVEL = "1.8.0";
  private static final String CLIENT_MAVEN_GROUP_ID = "my-group";
  private static final String CLIENT_MAVEN_ARTIFACT_ID = "my-artifact";
  private static final String CLIENT_MAVEN_VERSION = "1.0.0-alpha";

  @Test
  public void parse_withFullyPopulatedJson_returnsPopulatedObject() {
    CloudLibrary library = parse(createFullyPopulatedJson());
    CloudLibraryClient client = Iterables.getOnlyElement(library.getClients());
    CloudLibraryClientMavenCoordinates mavenCoordinates = client.getMavenCoordinates();
    assertNotNull(mavenCoordinates);
    String serviceRole = Iterables.getOnlyElement(library.getServiceRoles());

    assertEquals(NAME, library.getName());
    assertEquals(ID, library.getId());
    assertEquals(SERVICE_NAME, library.getServiceName());
    assertEquals(SERVICE_ROLE, serviceRole);
    assertEquals(DOCUMENTATION, library.getDocumentation());
    assertEquals(DESCRIPTION, library.getDescription());
    assertEquals(TRANSPORT, Iterables.getOnlyElement(library.getTransports()));
    assertEquals(ICON, library.getIcon());

    assertEquals(CLIENT_NAME, client.getName());
    assertEquals(CLIENT_LANGUAGE, client.getLanguage());
    assertEquals(CLIENT_SITE, client.getSite());
    assertEquals(CLIENT_API_REFERENCE, client.getApiReference());
    assertEquals(CLIENT_INFO_TIP, client.getInfoTip());
    assertEquals(CLIENT_LAUNCH_STAGE, client.getLaunchStage());
    assertEquals(CLIENT_SOURCE, client.getSource());
    assertEquals(CLIENT_LANGUAGE_LEVEL, client.getLanguageLevel());

    assertEquals(CLIENT_MAVEN_GROUP_ID, mavenCoordinates.getGroupId());
    assertEquals(CLIENT_MAVEN_ARTIFACT_ID, mavenCoordinates.getArtifactId());
    assertEquals(CLIENT_MAVEN_VERSION, mavenCoordinates.getVersion());
  }

  @Test
  public void parse_withMissingFields_returnsObjectWithNulls() {
    String json = String.format("{name:%s}", wrap(NAME));
    CloudLibrary library = parse(json);
    assertEquals(NAME, library.getName());
    assertNull(library.getId());

    // Lists should also be null.
    assertNull(library.getTransports());
    assertNull(library.getClients());
  }

  @Test
  public void parse_withMultipleTransports_returnsObjectWithMultipleTransports() {
    String transport1 = "transport1";
    String transport2 = "transport2";
    String json = String.format("{transports:[%s, %s]}", transport1, transport2);
    CloudLibrary library = parse(json);

    List<String> transports = library.getTransports();
    assertNotNull("expected 2 transports", transports);
    assertEquals("expected 2 transports", 2, transports.size());
    assertEquals(transport1, transports.get(0));
    assertEquals(transport2, transports.get(1));
  }

  @Test
  public void parse_withMultipleClients_returnsObjectWithMultipleClients() {
    String client1 = "client1";
    String client2 = "client2";
    String client1Json = String.format("{name:%s}", client1);
    String client2Json = String.format("{name:%s}", client2);
    String json = String.format("{clients:[%s, %s]}", client1Json, client2Json);
    CloudLibrary library = parse(json);

    List<CloudLibraryClient> clients = library.getClients();
    assertNotNull("expected 2 clients", clients);
    assertEquals("expected 2 clients", 2, clients.size());
    assertEquals(client1, clients.get(0).getName());
    assertEquals(client2, clients.get(1).getName());
  }

  @Test
  public void parse_withEmptyJson_doesNotThrowException() {
    // The test will fail if this throws an exception.
    parse("");
  }

  @Test
  public void parse_withMalformedJson_throwsException() {
    try {
      parse("this is invalid json");
      fail("Expected JsonSyntaxException to be thrown.");
    } catch (JsonSyntaxException e) {
      // The existence of the exception is enough.
    }
  }

  @Test
  public void parse_withMismatchingTypes_throwsException() {
    try {
      parse("{clients:123}");
      fail("Expected JsonSyntaxException to be thrown.");
    } catch (JsonSyntaxException e) {
      // The existence of the exception is enough.
    }
  }

  /**
   * Parses the given JSON representation of a {@link CloudLibrary} and returns a new instance
   * deserialized from the given JSON.
   */
  private static CloudLibrary parse(String json) {
    return new Gson().fromJson(json, CloudLibrary.class);
  }

  /**
   * Returns JSON that has a value set for every field in the serialized version of {@link
   * CloudLibrary}.
   */
  private static String createFullyPopulatedJson() {
    return String.format(
        "{name:%s,id:%s,serviceName:%s,serviceRoles:[%s],documentation:%s,description:%s,"
            + "transports:[%s],icon:%s,clients:[{name:%s,language:%s,site:%s,apireference:%s,"
            + "infotip:%s,launchStage:%s,source:%s,languageLevel:%s,mavenCoordinates:{groupId:%s,"
            + "artifactId:%s,version:%s}}]}",
        wrap(NAME),
        wrap(ID),
        wrap(SERVICE_NAME),
        wrap(SERVICE_ROLE),
        wrap(DOCUMENTATION),
        wrap(DESCRIPTION),
        wrap(TRANSPORT),
        wrap(ICON),
        wrap(CLIENT_NAME),
        wrap(CLIENT_LANGUAGE),
        wrap(CLIENT_SITE),
        wrap(CLIENT_API_REFERENCE),
        wrap(CLIENT_INFO_TIP),
        wrap(CLIENT_LAUNCH_STAGE),
        wrap(CLIENT_SOURCE),
        wrap(CLIENT_LANGUAGE_LEVEL),
        wrap(CLIENT_MAVEN_GROUP_ID),
        wrap(CLIENT_MAVEN_ARTIFACT_ID),
        wrap(CLIENT_MAVEN_VERSION));
  }

  /** Wraps the given string in quotes and returns the result. */
  private static String wrap(String text) {
    return String.format("\"%s\"", text);
  }
}
