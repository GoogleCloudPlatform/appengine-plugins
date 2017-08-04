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

package com.google.cloud.tools.appengine.cloudsdk.serialization;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import org.junit.Assert;
import org.junit.Test;

public class AppEngineDeployResultTest {

  private static final String ONE_VERSION =
      "{" +
      "  'configs': []," +
      "  'versions': [" +
      "    {" +
      "      'id': '20160429t112518'," +
      "      'last_deployed_time': null," +
      "      'project': 'bizarre-project'," +
      "      'service': 'display-service'," +
      "      'traffic_split': null," +
      "      'version': null" +
      "    }" +
      "  ]" +
      "}";

  private static final String TWO_VERSIONS =
      "{" +
      "  'configs': []," +
      "  'versions': [" +
      "    {" +
      "      'id': '20160429t112518'," +
      "      'last_deployed_time': null," +
      "      'project': 'bizarre-project'," +
      "      'service': 'display-service'," +
      "      'traffic_split': null," +
      "      'version': null" +
      "    }," +
      "    {" +
      "      'id': '20170805t091353'," +
      "      'last_deployed_time': null," +
      "      'project': 'another-project'," +
      "      'service': 'awesome-service'," +
      "      'traffic_split': null," +
      "      'version': null" +
      "    }" +
      "  ]" +
      "}";

  @Test
  public void testParse_oneVersion() {
    AppEngineDeployResult json = AppEngineDeployResult.parse(ONE_VERSION);
    Assert.assertEquals("20160429t112518", json.getVersion(0));
    Assert.assertEquals("display-service", json.getService(0));
    Assert.assertEquals("bizarre-project", json.getProject(0));
  }

  @Test
  public void testParse_twoVersions() {
    AppEngineDeployResult json = AppEngineDeployResult.parse(TWO_VERSIONS);
    Assert.assertEquals("20160429t112518", json.getVersion(0));
    Assert.assertEquals("display-service", json.getService(0));
    Assert.assertEquals("bizarre-project", json.getProject(0));

    Assert.assertEquals("20170805t091353", json.getVersion(1));
    Assert.assertEquals("awesome-service", json.getService(1));
    Assert.assertEquals("another-project", json.getProject(1));
  }

  @Test
  public void testParse_deprecatedFormat() {
    String deprecatedFormat = "{ 'default': 'https://springboot-maven-project.appspot.com' }";
    try {
      AppEngineDeployResult.parse(deprecatedFormat);
      fail();
    } catch (JsonParseException e) {
      assertThat(e.getMessage(), startsWith("cannot parse gcloud app deploy result output: "));
    }
  }

  @Test
  public void testParse_malformedInput() {
    try {
      AppEngineDeployResult.parse("non-JSON");
      fail();
    } catch (JsonSyntaxException e) {
      assertNotNull(e.getMessage());
    }
  }

  @Test
  public void testParse_nonArrayVersions() {
    try {
      AppEngineDeployResult.parse("{ 'versions' : 'non-array' }");
      fail();
    } catch (JsonSyntaxException e) {
      assertNotNull(e.getMessage());
    }
  }

  @Test
  public void testParse_versionsMissing() {
    try {
      AppEngineDeployResult.parse("{}");
      fail();
    } catch (JsonParseException e) {
      assertThat(e.getMessage(), startsWith("cannot parse gcloud app deploy result output: "));
    }
  }

  @Test
  public void testParse_emptyVersions() {
    try {
      AppEngineDeployResult.parse("{ 'versions' : [] }");
      fail();
    } catch (JsonParseException e) {
      assertThat(e.getMessage(), startsWith("cannot parse gcloud app deploy result output: "));
    }
  }

  @Test
  public void testParse_versionIdMissing() {
    try {
      AppEngineDeployResult.parse(
          "{'versions': [ {'service': 'a-service', 'project': 'a-project'} ]}");
      fail();
    } catch (JsonParseException e) {
      assertThat(e.getMessage(), startsWith("cannot parse gcloud app deploy result output: "));
    }
  }

  @Test
  public void testParse_versionServiceMissing() {
    try {
      AppEngineDeployResult.parse("{'versions': [ {'id': 'a-id', 'project': 'a-project'} ]}");
      fail();
    } catch (JsonParseException e) {
      assertThat(e.getMessage(), startsWith("cannot parse gcloud app deploy result output: "));
    }
  }

  @Test
  public void testParse_versionProjectMissing() {
    try {
      AppEngineDeployResult.parse("{'versions': [ {'id': 'a-id', 'service': 'a-service'} ]}");
      fail();
    } catch (JsonParseException e) {
      assertThat(e.getMessage(), startsWith("cannot parse gcloud app deploy result output: "));
    }
  }
}
