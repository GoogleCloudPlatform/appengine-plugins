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
import java.util.List;

/** Holds de-serialized JSON result output of {@code gcloud app deploy}. */
public class AppEngineDeployResult {

  private static class Version {
    // Don't change the field names because Gson uses them for automatic de-serialization.
    private String id;
    private String service;
    private String project;
  }

  // Don't change the field names because Gson uses them for automatic de-serialization.
  private List<Version> versions;

  private AppEngineDeployResult() {} // empty private constructor

  /**
   * Returns the version of the deployed app.
   *
   * @param index designates an app among multiple deployed apps
   * @throws IndexOutOfBoundsException if the index is out of range
   */
  public String getVersion(int index) {
    return versions.get(index).id;
  }

  /**
   * Returns the service name of the deployed app.
   *
   * @param index designates an app among multiple deployed apps
   * @throws IndexOutOfBoundsException if the index is out of range
   */
  public String getService(int index) {
    return versions.get(index).service;
  }

  /**
   * Returns the GCP project where the app is deployed.
   *
   * @param index designates an app among multiple deployed apps
   * @throws IndexOutOfBoundsException if the index is out of range
   */
  public String getProject(int index) {
    return versions.get(index).project;
  }

  /**
   * Parses a JSON string representing successful {@code gcloud app deploy} result.
   *
   * @return parsed JSON; never {@code null}
   * @throws JsonParseException if {@code jsonString} has syntax errors or incompatible JSON element
   *     type
   */
  public static AppEngineDeployResult parse(String jsonString) throws JsonParseException {
    Preconditions.checkNotNull(jsonString);
    try {
      return new Gson().fromJson(jsonString, AppEngineDeployResult.class);
    } catch (JsonSyntaxException e) {
      throw new JsonParseException(e);
    }
  }
}
