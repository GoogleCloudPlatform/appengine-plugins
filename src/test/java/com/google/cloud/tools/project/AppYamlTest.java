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

package com.google.cloud.tools.project;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

/** Tests for AppYaml parsing */
public class AppYamlTest {

  // https://github.com/GoogleCloudPlatform/appengine-plugins-core/issues/405
  @Test
  public void testEmptyAppYaml() {
    InputStream appYaml = asStream("");
    Assert.assertNull(AppYaml.parse(appYaml).getRuntime());
  }

  @Test
  public void testIgnoresUnsupportedElements() {
    InputStream appYaml = asStream("runtime: java\nhandlers:\n- url: /\n  script: foo.app\n");
    Assert.assertNotNull(AppYaml.parse(appYaml));
  }

  @Test
  public void testGetEnvironmentType_success() {
    InputStream appYaml = asStream("env: flex\nruntime: java\np2: v2");
    Assert.assertEquals("flex", AppYaml.parse(appYaml).getEnvironmentType());
  }

  @Test
  public void testGetEnvironmentType_failureBecauseWrongType() {
    InputStream appYaml = asStream("env: [goose, moose]\np2: v2");
    Assert.assertNull(AppYaml.parse(appYaml).getEnvironmentType());
  }

  @Test
  public void testGetEnvironmentType_failureBecauseNotPresent() {
    InputStream appYaml = asStream("p1: v1\np2: v2");
    Assert.assertNull(AppYaml.parse(appYaml).getEnvironmentType());
  }

  @Test
  public void testGetRuntime_success() {
    InputStream appYaml = asStream("runtime: java\np2: v2");
    Assert.assertEquals("java", AppYaml.parse(appYaml).getRuntime());
  }

  @Test
  public void testGetRuntime_failureBecauseWrongType() {
    InputStream appYaml = asStream("runtime: [goose, moose]\np2: v2");
    Assert.assertNull(AppYaml.parse(appYaml).getRuntime());
  }

  @Test
  public void testGetRuntime_failureBecauseNotPresent() {
    InputStream appYaml = asStream("p1: v1\np2: v2");
    Assert.assertNull(AppYaml.parse(appYaml).getRuntime());
  }

  @Test
  public void testGetApplication_success() {
    InputStream appYaml = asStream("application: app\np2: v2");
    Assert.assertEquals("app", AppYaml.parse(appYaml).getProjectId());
  }

  @Test
  public void testGetApplication_failureBecauseWrongType() {
    InputStream appYaml = asStream("application: [goose, moose]\np2: v2");
    Assert.assertNull(AppYaml.parse(appYaml).getProjectId());
  }

  @Test
  public void testGetApplication_failureBecauseNotPresent() {
    InputStream appYaml = asStream("p1: v1\np2: v2");
    Assert.assertNull(AppYaml.parse(appYaml).getProjectId());
  }

  @Test
  public void testGetServiceId_success() {
    InputStream appYaml = asStream("service: service\np2: v2");
    Assert.assertEquals("service", AppYaml.parse(appYaml).getServiceId());
  }

  @Test
  public void testGetServiceId_failureBecauseWrongType() {
    InputStream appYaml = asStream("service: [goose, moose]\np2: v2");
    Assert.assertNull(AppYaml.parse(appYaml).getServiceId());
  }

  @Test
  public void testGetServiceId_successWithModule() {
    InputStream appYaml = asStream("module: module\np2: v2");
    Assert.assertEquals("module", AppYaml.parse(appYaml).getServiceId());
  }

  @Test
  public void testGetServiceId_failureWithModuleBecauseWrongType() {
    InputStream appYaml = asStream("module: [goose, moose]\np2: v2");
    Assert.assertNull(AppYaml.parse(appYaml).getServiceId());
  }

  @Test
  public void testGetServiceId_failureBecauseNotPresent() {
    InputStream appYaml = asStream("p1: v1\np2: v2");
    Assert.assertNull(AppYaml.parse(appYaml).getServiceId());
  }

  @Test
  public void testGetVersion_success() {
    InputStream appYaml = asStream("version: ver\np2: v2");
    Assert.assertEquals("ver", AppYaml.parse(appYaml).getProjectVersion());
  }

  @Test
  public void testGetVersion_failureBecauseWrongType() {
    InputStream appYaml = asStream("version: [goose, moose]\np2: v2");
    Assert.assertNull(AppYaml.parse(appYaml).getProjectVersion());
  }

  @Test
  public void testGetVersion_failureBecauseNotPresent() {
    InputStream appYaml = asStream("p1: v1\np2: v2");
    Assert.assertNull(AppYaml.parse(appYaml).getProjectVersion());
  }

  @Test
  public void testGetApiVersion_success() {
    InputStream appYaml = asStream("api_version: ver\np2: v2");
    Assert.assertEquals("ver", AppYaml.parse(appYaml).getApiVersion());
  }

  @Test
  public void testGetApiVersion_failureBecauseWrongType() {
    InputStream appYaml = asStream("api_version: [goose, moose]\np2: v2");
    Assert.assertNull(AppYaml.parse(appYaml).getApiVersion());
  }

  @Test
  public void testGetApiVersion_failureBecauseNotPresent() {
    InputStream appYaml = asStream("p1: v1\np2: v2");
    Assert.assertNull(AppYaml.parse(appYaml).getApiVersion());
  }

  @Test
  public void testGetEnvironmentVariables_success() {
    InputStream appYaml = asStream("env_variables:\n  key1: value1\n  key2: 0\np2: v2");
    Map<String, ?> environment = AppYaml.parse(appYaml).getEnvironmentVariables();
    Assert.assertNotNull(environment);
    Assert.assertEquals(2, environment.size());
    Assert.assertEquals("value1", environment.get("key1"));
    Assert.assertEquals(0, environment.get("key2"));
  }

  @Test
  public void testGetEnvironmentVariables_failureBecauseWrongType() {
    InputStream appYaml = asStream("env_variables: [goose, moose]\np2: v2");
    Assert.assertNull(AppYaml.parse(appYaml).getEnvironmentVariables());
  }

  @Test
  public void testGetEnvironmentVariables_failureBecauseNotPresent() {
    InputStream appYaml = asStream("p1: v1\np2: v2");
    Assert.assertNull(AppYaml.parse(appYaml).getEnvironmentVariables());
  }

  private InputStream asStream(String contents) {
    return new ByteArrayInputStream(contents.getBytes(StandardCharsets.UTF_8));
  }
}
