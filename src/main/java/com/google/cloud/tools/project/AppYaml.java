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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;
import org.yaml.snakeyaml.Yaml;

/** Tools for reading app.yaml */
public class AppYaml {

  private final Map<String, ?> yamlMap;

  private static final String RUNTIME_KEY = "runtime";

  /**
   * Parse an app.yaml to AppYaml object.
   *
   * @param appYaml the app.yaml file
   * @throws IOException if reading app.yaml fails due to I/O errors
   * @throws org.yaml.snakeyaml.scanner.ScannerException if reading app.yaml fails while scanning
   *     due to malformed YAML (undocumented {@link RuntimeException} from {@link Yaml#load})
   * @throws org.yaml.snakeyaml.parser.ParserException if reading app.yaml fails while parsing due
   *     to malformed YAML (undocumented {@link RuntimeException} from {@link Yaml#load})
   */
  public AppYaml(Path appYaml) throws IOException {
    try (InputStream in = Files.newInputStream(appYaml)) {
      Object loaded = new Yaml().load(in);
      if (loaded == null) {
        yamlMap = Collections.emptyMap();
      } else {
        yamlMap = (Map<String, ?>) loaded;
      }
    }
  }

  /**
   * Returns "runtime" value if it is a String type. {@code null} if it is not a String or the
   * "runtime" key is not present.
   */
  public String getRuntime() {
    Object result = yamlMap.get(RUNTIME_KEY);
    if (result instanceof String) {
      return (String) result;
    }

    return null;
  }
}
