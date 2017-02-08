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

import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Tools for reading app.yaml
 */
public class AppYaml {

  private final Map<String, ?> yamlMap;

  private static final String RUNTIME_KEY = "runtime";

  /**
   * @param appyaml A valid existing app.yaml
   * @throws FileNotFoundException if app.yaml doesn't exist
   */
  public AppYaml(Path appyaml) throws FileNotFoundException {
    Yaml yaml = new Yaml();

    // Standard snake yaml parsing.
    @SuppressWarnings("unchecked")
    Map<String, Object> parserResult =
        (Map<String, Object>) yaml.load(new FileReader(appyaml.toFile()));

    yamlMap = parserResult;
  }

  /**
   * @return "runtime" value if it is a String type.
   *         {@code null} if it is not a String or the "runtime" key is not present.
   */
  public String getRuntime() {
    Object result = yamlMap.get(RUNTIME_KEY);
    if (result != null && result instanceof String) {
      return (String) result;
    }

    return null;
  }
}
