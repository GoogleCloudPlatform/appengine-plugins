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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import org.junit.Test;

public class GcloudStructuredLogTest {

  private static final String sampleJson = "{"
      + "  'version': 'semantic version of the message format, e.g. 0.0.1',"
      + "  'verbosity': 'logging level: e.g. debug, info, warn, error, critical, exception',"
      + "  'timestamp': 'time event logged in UTC log file format: %Y-%m-%dT%H:%M:%S.%3f%Ez',"
      + "  'message': 'log/error message string',"
      + "  'error': {"
      + "    'type': 'exception or error raised (if logged message has actual exception data)',"
      + "    'stacktrace': 'stacktrace or error if available',"
      + "    'details': 'any additional error details'"
      + "  }"
      + "}";

  private static final String noErrorSampleJson = "{ 'version': '0.0.1', 'verbosity': 'ERROR',"
      + " 'timestamp': '2017-08-04T18:49:50.917Z',"
      + " 'message': '(gcloud.app.deploy) Could not copy [/tmp/tmpAqUB6m/src.tgz]' }";

  private static final String noVersionSampleJson = "{ 'verbosity': 'ERROR',"
      + " 'timestamp': '2017-08-04T18:49:50.917Z',"
      + " 'message': '(gcloud.app.deploy) Could not copy [/tmp/tmpAqUB6m/src.tgz]' }";

  private static final String noVerbositySampleJson = "{ 'version': '0.0.1',"
      + " 'timestamp': '2017-08-04T18:49:50.917Z',"
      + " 'message': '(gcloud.app.deploy) Could not copy [/tmp/tmpAqUB6m/src.tgz]' }";

  private static final String noTimestampSampleJson = "{ 'version': '0.0.1', 'verbosity': 'ERROR',"
      + " 'message': '(gcloud.app.deploy) Could not copy [/tmp/tmpAqUB6m/src.tgz]' }";

  private static final String noMessageSampleJson = "{ 'version': '0.0.1', 'verbosity': 'ERROR',"
      + " 'timestamp': '2017-08-04T18:49:50.917Z' }";

  @Test
  public void testParse_fullJson() {
    GcloudStructuredLog error = GcloudStructuredLog.parse(sampleJson);
    assertEquals("semantic version of the message format, e.g. 0.0.1", error.version);
    assertEquals("logging level: e.g. debug, info, warn, error, critical, exception",
        error.verbosity);
    assertEquals("time event logged in UTC log file format: %Y-%m-%dT%H:%M:%S.%3f%Ez",
        error.timestamp);
    assertEquals("log/error message string", error.message);
    assertEquals("exception or error raised (if logged message has actual exception data)",
        error.error.type);
    assertEquals("stacktrace or error if available", error.error.stacktrace);
    assertEquals("any additional error details", error.error.details);
  }

  @Test
  public void testParse_errorNotPresent() {
    GcloudStructuredLog error = GcloudStructuredLog.parse(noErrorSampleJson);
    assertEquals("0.0.1", error.version);
    assertEquals("ERROR", error.verbosity);
    assertEquals("2017-08-04T18:49:50.917Z", error.timestamp);
    assertEquals("(gcloud.app.deploy) Could not copy [/tmp/tmpAqUB6m/src.tgz]", error.message);
    assertNull(error.error);
  }

  @Test
  public void testParse_inputMalformed() {
    try {
      GcloudStructuredLog.parse("non-JSON");
      fail();
    } catch (JsonSyntaxException e) {
      assertNotNull(e.getMessage());
    }
  }

  @Test
  public void testParse_versionMissing() {
    try {
      GcloudStructuredLog.parse(
          "{'verbosity': 'INFO', 'timestamp': 'a-timestamp', 'message': 'info message'}");
      fail();
    } catch (JsonParseException e) {
      assertThat(e.getMessage(), startsWith("cannot parse gcloud structured log entry: "));
    }
  }

  @Test
  public void testParse_verbosityMissing() {
    try {
      GcloudStructuredLog.parse(
          "{'version': '0.0.1', 'timestamp': 'a-timestamp', 'message': 'info message'}");
      fail();
    } catch (JsonParseException e) {
      assertThat(e.getMessage(), startsWith("cannot parse gcloud structured log entry: "));
    }
  }

  @Test
  public void testParse_timestampMissing() {
    try {
      GcloudStructuredLog.parse(
          "{'version': '0.0.1', 'verbosity': 'INFO', 'message': 'info message'}");
      fail();
    } catch (JsonParseException e) {
      assertThat(e.getMessage(), startsWith("cannot parse gcloud structured log entry: "));
    }
  }

  @Test
  public void testParse_messageMissing() {
    try {
      GcloudStructuredLog.parse(
          "{'version': '0.0.1', 'verbosity': 'INFO', 'timestamp': 'a-timestamp'}");
      fail();
    } catch (JsonParseException e) {
      assertThat(e.getMessage(), startsWith("cannot parse gcloud structured log entry: "));
    }
  }
}
