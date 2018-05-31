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
import javax.annotation.Nullable;

/** Holds de-serialized JSON of a single instance of structured log output from {@code gcloud}. */
public class GcloudStructuredLog {

  public static class GcloudError {
    // Don't change the field names because Gson uses them for automatic de-serialization.
    @Nullable private String type;
    @Nullable private String stacktrace;
    @Nullable private String details;

    // empty private constructor; GSON instantiates
    private GcloudError() {}

    @Nullable
    public String getType() {
      return type;
    }

    @Nullable
    public String getStacktrace() {
      return stacktrace;
    }

    @Nullable
    public String getDetails() {
      return details;
    }
  }

  // Don't change the field names because Gson uses them for automatic de-serialization.
  @Nullable private String version;
  @Nullable private String verbosity;
  @Nullable private String timestamp;
  @Nullable private String message;
  @Nullable private GcloudError error;

  @Nullable
  public String getVersion() {
    return version;
  }

  @Nullable
  public String getVerbosity() {
    return verbosity;
  }

  @Nullable
  public String getTimestamp() {
    return timestamp;
  }

  /** Returns a human readable description of the error. */
  public String getMessage() {
    if (message == null) {
      return "";
    }
    return message;
  }

  @Nullable
  public GcloudError getError() {
    return error;
  }

  // empty private constructor; GSON instantiates
  private GcloudStructuredLog() {}

  /**
   * Parses a JSON string representing {@code gcloud} structured log output.
   *
   * @return parsed JSON
   * @throws JsonParseException if {@code jsonString} has syntax errors or incompatible JSON element
   *     type
   */
  public static GcloudStructuredLog parse(String jsonString) throws JsonParseException {
    Preconditions.checkNotNull(jsonString);
    try {
      GcloudStructuredLog log = new Gson().fromJson(jsonString, GcloudStructuredLog.class);
      if (log == null) {
        throw new JsonParseException("Empty input: \"" + jsonString + "\"");
      }
      return log;
    } catch (JsonSyntaxException e) {
      throw new JsonParseException(e);
    }
  }
}
