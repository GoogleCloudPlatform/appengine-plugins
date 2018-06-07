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

import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import javax.annotation.Nullable;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

/** Tools for reading {@code app.yaml}. */
public class AppYaml {

  private static final String RUNTIME_KEY = "runtime";
  private static final String API_VERSION_KEY = "api_version";
  private static final String APPLICATION_KEY = "application";
  private static final String VERSION_KEY = "version";
  private static final String SERVICE_KEY = "service";
  private static final String MODULE_KEY = "module";
  private static final String ENVIRONMENT_KEY = "env_variables";

  private final Map<String, ?> yamlMap;

  /**
   * Parse an app.yaml file to an AppYaml object.
   *
   * @param input the input, typically the contents of an {@code app.yaml} file
   * @throws org.yaml.snakeyaml.scanner.ScannerException if reading app.yaml fails while scanning
   *     due to malformed YAML (undocumented {@link RuntimeException} from {@link Yaml#load})
   * @throws org.yaml.snakeyaml.parser.ParserException if reading app.yaml fails while parsing due
   *     to malformed YAML (undocumented {@link RuntimeException} from {@link Yaml#load})
   */
  @SuppressWarnings("unchecked")
  public static AppYaml parse(InputStream input) {
    // our needs are simple so just load using primitive objects
    Yaml yaml = new Yaml(new SafeConstructor());
    Map<String, ?> contents = (Map<String, ?>) yaml.load(input);
    return new AppYaml(contents);
  }

  private AppYaml(Map<String, ?> yamlMap) {
    this.yamlMap = yamlMap == null ? Collections.emptyMap() : yamlMap;
  }

  @Nullable
  public String getRuntime() {
    return getString(RUNTIME_KEY);
  }

  @Nullable
  public String getApiVersion() {
    return getString(API_VERSION_KEY);
  }

  @Nullable
  public String getApplication() {
    return getString(APPLICATION_KEY);
  }

  @Nullable
  public String getProjectVersion() {
    return getString(VERSION_KEY);
  }

  @Nullable
  public String getModuleId() {
    return getString(MODULE_KEY);
  }

  @Nullable
  public String getServiceId() {
    return getString(SERVICE_KEY);
  }

  @Nullable
  public Map<String, ?> getEnvironment() {
    return getStringMap(ENVIRONMENT_KEY);
  }

  private String getString(String key) {
    Object value = yamlMap.get(key);
    return value instanceof String ? (String) value : null;
  }

  @SuppressWarnings("unchecked")
  private Map<String, ?> getStringMap(String key) {
    Object value = yamlMap.get(key);
    return value instanceof Map<?, ?> ? (Map<String, ?>) value : null;
  }
}
