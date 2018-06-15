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

import com.google.cloud.tools.appengine.api.AppEngineException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;
import javax.annotation.Nullable;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.error.YAMLException;

/** Tools for reading {@code app.yaml}. */
public class AppYaml {

  private static final String ENVIRONMENT_TYPE_KEY = "env";
  private static final String RUNTIME_KEY = "runtime";
  private static final String API_VERSION_KEY = "api_version";
  private static final String APPLICATION_KEY = "application";
  private static final String VERSION_KEY = "version";
  private static final String SERVICE_KEY = "service";
  private static final String MODULE_KEY = "module";
  private static final String ENVIRONMENT_VARIABLES_KEY = "env_variables";

  private final Map<String, ?> yamlMap;

  /**
   * Parse an app.yaml file to an AppYaml object.
   *
   * @param input the input, typically the contents of an {@code app.yaml} file
   * @throws AppEngineException if reading app.yaml fails while scanning such as due to malformed
   *     YAML
   */
  @SuppressWarnings("unchecked")
  public static AppYaml parse(InputStream input) throws AppEngineException {
    try {
      // our needs are simple so just load using primitive objects
      Yaml yaml = new Yaml(new SafeConstructor());
      Map<String, ?> contents = (Map<String, ?>) yaml.load(input);
      return new AppYaml(contents);
    } catch (YAMLException ex) {
      throw new AppEngineException("Malformed 'app.yaml'.", ex);
    }
  }

  private AppYaml(@Nullable Map<String, ?> yamlMap) {
    this.yamlMap = yamlMap == null ? Collections.emptyMap() : yamlMap;
  }

  /**
   * Return the content of the {@code environment} field, which defines this app's required App
   * Engine environment.
   */
  @Nullable
  public String getEnvironmentType() {
    return getString(ENVIRONMENT_TYPE_KEY);
  }

  /** Return the content of the {@code runtime} field, which defines this app's expected runtime. */
  @Nullable
  public String getRuntime() {
    return getString(RUNTIME_KEY);
  }

  /**
   * Return the content of the {@code application} field, which identifies the App Engine API
   * version used by this app.
   */
  @Nullable
  public String getApiVersion() {
    return getString(API_VERSION_KEY);
  }

  /** Return the content of the {@code application} field, which identifies the Project ID. */
  @Nullable
  public String getProjectId() {
    return getString(APPLICATION_KEY);
  }

  /** Return the content of the {@code version} field, which identifies the Project version. */
  @Nullable
  public String getProjectVersion() {
    return getString(VERSION_KEY);
  }

  /**
   * Return the content of the {@code service} field or the now-deprecated {@code module} field,
   * which identifies the Service ID.
   */
  @Nullable
  public String getServiceId() {
    String serviceId = getString(SERVICE_KEY);
    if (serviceId == null) {
      serviceId = getString(MODULE_KEY);
    }
    return serviceId;
  }

  /** Return the content of the {@code env_variables} field, which defines environment variables. */
  @Nullable
  public Map<String, ?> getEnvironmentVariables() {
    return getStringMap(ENVIRONMENT_VARIABLES_KEY);
  }

  @Nullable
  private String getString(String key) {
    Object value = yamlMap.get(key);
    return value instanceof String ? (String) value : null;
  }

  @Nullable
  @SuppressWarnings("unchecked")
  private Map<String, ?> getStringMap(String key) {
    Object value = yamlMap.get(key);
    return value instanceof Map<?, ?> ? (Map<String, ?>) value : null;
  }
}
