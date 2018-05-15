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

package com.google.cloud.tools.libraries;

import com.google.cloud.tools.libraries.json.CloudLibrary;
import com.google.common.annotations.VisibleForTesting;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.MissingResourceException;

/** Returns helpful metadata for supported Google Cloud libraries. */
public final class CloudLibraries {

  private static final String LIBRARIES_JSON = "libraries.json";

  private final String librariesJsonPath;

  @VisibleForTesting
  CloudLibraries(String librariesJsonPath) {
    this.librariesJsonPath = librariesJsonPath;
  }

  /**
   * Returns the list of {@link CloudLibrary} objects deserialized from the {@code libraries.json}
   * file.
   *
   * @throws IOException if there was a problem reading the {@code libraries.json} file
   */
  public static List<CloudLibrary> getCloudLibraries() throws IOException {
    return new CloudLibraries(LIBRARIES_JSON).getLibraries();
  }

  @VisibleForTesting
  List<CloudLibrary> getLibraries() throws IOException {
    try (InputStream inputStream = CloudLibraries.class.getResourceAsStream(librariesJsonPath)) {
      if (inputStream == null) {
        throw new MissingResourceException(
            "Resource not found when loading libraries", LIBRARIES_JSON, librariesJsonPath);
      }

      InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
      JsonReader jsonReader = new JsonReader(reader);
      Type listType = new TypeToken<List<CloudLibrary>>() {}.getType();
      return new Gson().fromJson(jsonReader, listType);
    }
  }
}
