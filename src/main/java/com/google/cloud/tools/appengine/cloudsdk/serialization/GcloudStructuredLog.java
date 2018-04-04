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

import com.google.cloud.tools.appengine.cloudsdk.JsonParseException;
import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

/** Holds de-serialized JSON of a single instance of structured log output from {@code gcloud}. */
public class GcloudStructuredLog {

  public static class Error {
    // Don't change the field names because Gson uses them for automatic de-serialization.
    private String type;
    private String stacktrace;
    private String details;

    public String getType() {
      return type;
    }

    public String getStacktrace() {
      return stacktrace;
    }

    public String getDetails() {
      return details;
    }
  }

  // Don't change the field names because Gson uses them for automatic de-serialization.
  private String version;
  private String verbosity;
  private String timestamp;
  private String message;
  private Error error;

  public String getVersion() {
    return version;
  }

  public String getVerbosity() {
    return verbosity;
  }

  public String getTimestamp() {
    return timestamp;
  }

  public String getMessage() {
    return message;
  }

  public Error getError() {
    return error;
  }

  private GcloudStructuredLog() {} // empty private constructor

  /**
   * Parses a JSON string representing {@code gcloud} structured log output.
   *
   * @return parsed JSON; never {@code null}
   * @throws JsonParseException if {@code jsonString} has syntax errors or incompatible JSON element
   *     type
   */
  public static GcloudStructuredLog parse(String jsonString) throws JsonParseException {
    Preconditions.checkNotNull(jsonString);
    try {
      return new Gson().fromJson(jsonString, GcloudStructuredLog.class);
    } catch (JsonSyntaxException e) {
      throw new JsonParseException(e);
    }
  }
}
