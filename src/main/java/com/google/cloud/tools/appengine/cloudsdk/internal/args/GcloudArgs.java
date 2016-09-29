/*
 * Copyright 2016 Google Inc.
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

package com.google.cloud.tools.appengine.cloudsdk.internal.args;

import com.google.cloud.tools.appengine.api.Configuration;
import com.google.common.collect.Lists;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

/**
 * Command Line argument helper for gcloud based commands.
 */
public class GcloudArgs {

  /**
   * @return {@code [--name, value]} or {@code []} if value is null.
   */
  public static List<String> get(String name, String value) {
    return Args.string(name, value);
  }

  /**
   * @return {@code [--name, value]} or {@code []} if value is null.
   */
  public static List<String> get(String name, Integer value) {
    return Args.integer(name, value);
  }

  /**
   * @return {@code [--name]} if value is true, {@code [--no-name]} if value is false,
   * {@code []} if value is null.
   */
  public static List<String> get(String name, Boolean value) {
    return Args.boolWithNo(name, value);
  }

  /**
   * @return {@code [--name, file.toPath().toString()]} or {@code []} if file is null.
   */
  public static List<String> get(String name, File file) {
    return Args.filePath(name, file);
  }

  /**
   * @return {@code [--name, path.toString()]} or {@code []} if path is null, or its representation
   *     is empty.
   */
  public static List<String> get(String name, Path path) {
    return Args.path(name, path);
  }

  /**
   * @return {@code [key1=value1,key2=value2,...]}, {@code []} if keyValueMapping=empty/null
   */
  public static List<String> get(Map<?, ?> keyValueMapping) {
    return Args.keyValues(keyValueMapping);
  }

  /**
   * @return list of args for the common arguments in {@link Configuration}.
   */
  public static List<String> get(Configuration configuration) {
    List<String> result = Lists.newArrayList();
    result.addAll(get("project", configuration.getProject()));
    return result;
  }
}
