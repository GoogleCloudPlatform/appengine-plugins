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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

/** Command Line argument helper for gcloud based commands. */
public class GcloudArgs {

  /** Returns {@code [--name, value]} or {@code []} if value is null. */
  public static List<String> get(String name, @Nullable String value) {
    return Args.string(name, value);
  }

  /** Returns {@code [--name, value]} or {@code []} if value is null. */
  public static List<String> get(String name, @Nullable Integer value) {
    return Args.integer(name, value);
  }

  /**
   * Returns {@code [--name]} if value is true, {@code [--no-name]} if value is false, {@code []} if
   * value is null.
   */
  public static List<String> get(String name, @Nullable Boolean value) {
    if (value != null) {
      if (value) {
        return Collections.singletonList("--" + name);
      }
      return Collections.singletonList("--no-" + name);
    }
    return Collections.emptyList();
  }

  /** Returns {@code [--name, file.toPath().toString()]} or {@code []} if file is null. */
  public static List<String> get(String name, @Nullable File file) {
    return Args.filePath(name, file);
  }

  /**
   * Returns {@code [--name, path.toString()]} or {@code []} if path is null, or its representation
   * is empty.
   */
  public static List<String> get(String name, @Nullable Path path) {
    return Args.path(name, path);
  }

  /** Returns {@code [key1=value1,key2=value2,...]}, {@code []} if keyValueMapping=empty/null. */
  public static List<String> get(@Nullable Map<?, ?> keyValueMapping) {
    return Args.keyValueString(keyValueMapping);
  }

  /** Returns a list of args for the common arguments in {@link Configuration}. */
  public static List<String> get(Configuration configuration) {
    List<String> result = Lists.newArrayList();
    if (configuration != null) {
      result.addAll(get("project", configuration.getProjectId()));
    }
    return result;
  }
}
