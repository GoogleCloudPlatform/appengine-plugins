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

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;

/**
 * Holds de-serialized JSON of a single instance of structured log output from {@code gcloud}.
 */
// Don't change the field names because Gson uses them for automatic de-serialization.
public class GcloudStructuredLog {

  public static class Error {
    String type;
    String stacktrace;
    String details;
  }

  public String version;
  public String verbosity;
  public String timestamp;
  public String message;
  public Error error;

  private GcloudStructuredLog() {}  // empty private constructor

  /**
   * Parses a JSON string representing gcloud structured log output.
   *
   * @return parsed JSON; never {@code null}
   * @throws JsonSyntaxException if {@code jsonString} has syntax errors
   * @throws JsonParseException if {@code jsonString} has semantic errors
   *     (e.g., missing 'timestamp' property)
   */
  public static GcloudStructuredLog parse(String jsonString) {
    Preconditions.checkNotNull(jsonString);
    GcloudStructuredLog json = new Gson().fromJson(jsonString, GcloudStructuredLog.class);
    if (json.version == null || json.verbosity == null || json.timestamp == null
        || json.message == null) {
      throw new JsonParseException("cannot parse gcloud structured log entry: " + jsonString);
    }
    return json;
  }
}
