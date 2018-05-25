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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import com.google.cloud.tools.appengine.cloudsdk.JsonParseException;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

public class GcloudStructuredLogTest {

  private static final String sampleJson =
      "{"
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

  private static final String noErrorSampleJson =
      "{ 'version': '0.0.1', 'verbosity': 'ERROR',"
          + " 'timestamp': '2017-08-04T18:49:50.917Z',"
          + " 'message': '(gcloud.app.deploy) Could not copy [/tmp/tmpAqUB6m/src.tgz]' }";

  @Test
  public void testParse_fullJson() throws JsonParseException {
    GcloudStructuredLog log = GcloudStructuredLog.parse(sampleJson);
    assertEquals("semantic version of the message format, e.g. 0.0.1", log.getVersion());
    assertEquals(
        "logging level: e.g. debug, info, warn, error, critical, exception", log.getVerbosity());
    assertEquals(
        "time event logged in UTC log file format: %Y-%m-%dT%H:%M:%S.%3f%Ez", log.getTimestamp());
    assertEquals("log/error message string", log.getMessage());
    GcloudStructuredLog.GcloudError error = log.getError();
    assertNotNull(error);
    assertEquals(
        "exception or error raised (if logged message has actual exception data)", error.getType());
    assertEquals("stacktrace or error if available", error.getStacktrace());
    assertEquals("any additional error details", error.getDetails());
  }

  @Test
  public void testParse_errorNotPresent() throws JsonParseException {
    GcloudStructuredLog log = GcloudStructuredLog.parse(noErrorSampleJson);
    assertEquals("0.0.1", log.getVersion());
    assertEquals("ERROR", log.getVerbosity());
    assertEquals("2017-08-04T18:49:50.917Z", log.getTimestamp());
    assertEquals("(gcloud.app.deploy) Could not copy [/tmp/tmpAqUB6m/src.tgz]", log.getMessage());
    assertNull(log.getError());
  }

  @Test
  public void testParse_emptyInput() {
    try {
      GcloudStructuredLog.parse("");
      fail();
    } catch (JsonParseException e) {
      assertEquals("Empty input: \"\"", e.getMessage());
    }
  }

  @Test
  public void testParse_effectivelyEmptyInput() {
    try {
      GcloudStructuredLog.parse("# comment?");
      fail();
    } catch (JsonParseException e) {
      assertEquals("Empty input: \"# comment?\"", e.getMessage());
    }
  }

  @Test
  public void testParse_inputMalformed() {
    try {
      GcloudStructuredLog.parse("non-JSON");
      fail();
    } catch (JsonParseException e) {
      assertThat(e.getMessage(), CoreMatchers.containsString("JsonSyntaxException"));
    }
  }

  @Test
  public void testParse_noErrorWhenVersionMissing() throws JsonParseException {
    GcloudStructuredLog.parse(
        "{'verbosity': 'INFO', 'timestamp': 'a-timestamp', 'message': 'info message'}");
  }

  @Test
  public void testParse_noErrorWhenVerbosityMissing() throws JsonParseException {
    GcloudStructuredLog.parse(
        "{'version': '0.0.1', 'timestamp': 'a-timestamp', 'message': 'info message'}");
  }

  @Test
  public void testParse_noErrorWhenTimestampMissing() throws JsonParseException {
    GcloudStructuredLog.parse(
        "{'version': '0.0.1', 'verbosity': 'INFO', 'message': 'info message'}");
  }

  @Test
  public void testParse_noErrorWhenMessageMissing() throws JsonParseException {
    GcloudStructuredLog log =
        GcloudStructuredLog.parse(
            "{'version': '0.0.1', 'verbosity': 'INFO', 'timestamp': 'a-timestamp'}");
    Assert.assertEquals("", log.getMessage());
  }
}
