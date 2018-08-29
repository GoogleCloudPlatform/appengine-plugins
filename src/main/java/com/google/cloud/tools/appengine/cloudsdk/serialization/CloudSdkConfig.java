/*
 * Copyright 2018 Google LLC. All Rights Reserved.
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

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import javax.annotation.Nullable;

/** Representation of gcloud state. Used for JSON serialization/deserialization. */
public class CloudSdkConfig {

  public static class Core {
    @Nullable public String project;
  }

  @Nullable private Core core;

  private static final Gson gson = new Gson();

  private CloudSdkConfig() {}

  public static CloudSdkConfig fromJson(String json) throws JsonSyntaxException {
    return gson.fromJson(json, CloudSdkConfig.class);
  }

  /** Returns "project" from gcloud configuration and {@code null} if not configured. */
  @Nullable
  public String getProject() {
    if (core == null) {
      return null;
    }
    return core.project;
  }
}
