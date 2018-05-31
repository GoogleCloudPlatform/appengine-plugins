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

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

/** Command Line argument helper. */
class Args {

  /**
   * Produces the flag form of a string value.
   *
   * @return {@code [--name, value]} or {@code []} if value is null.
   */
  static List<String> string(String name, @Nullable String value) {
    if (!Strings.isNullOrEmpty(value)) {
      return Arrays.asList("--" + name, value);
    }
    return Collections.emptyList();
  }

  /**
   * Produces the flag form of a string value, separated with an equals character.
   *
   * @return {@code [--name=value]} or {@code []} if value is null.
   */
  static List<String> stringWithEq(String name, @Nullable String value) {
    if (!Strings.isNullOrEmpty(value)) {
      return Collections.singletonList("--" + name + "=" + value);
    }
    return Collections.emptyList();
  }

  /**
   * Produces the flag form of a repeated string, separated with an equals character.
   *
   * @return {@code [--name=value1, --name=value2, ...]} or {@code []} if value is null.
   */
  static List<String> stringsWithEq(String name, @Nullable List<String> values) {
    List<String> result = new ArrayList<>();
    if (values != null) {
      for (String value : values) {
        result.addAll(stringWithEq(name, value));
      }
    }
    return result;
  }

  /**
   * Produces the flag form of an integer value using {@link Integer#toString()}.
   *
   * @return {@code [--name, value]} or {@code []} if value is null.
   */
  static List<String> integer(String name, @Nullable Integer value) {
    if (value != null) {
      return Arrays.asList("--" + name, value.toString());
    }
    return Collections.emptyList();
  }

  /**
   * Produces the flag form of an integer using {@link Integer#toString()}, separated by an equals
   * character.
   *
   * @return {@code [--name=value]} or {@code []} if value is null.
   */
  static List<String> integerWithEq(String name, @Nullable Integer value) {
    if (value != null) {
      return Arrays.asList("--" + name + "=" + value.toString());
    }
    return Collections.emptyList();
  }

  /**
   * Produces the flag form of a file object, using {@link File#toPath()}.
   *
   * @return {@code [--name, file.toPath().toString()]} or {@code []} if file is null.
   */
  static List<String> filePath(String name, @Nullable File file) {
    if (file != null) {
      return path(name, file.toPath());
    }
    return Collections.emptyList();
  }

  /**
   * Produces the flag form of a path object, using {@link Path#toString()}.
   *
   * @return {@code [--name, path.toString()]} or {@code []} if path is null or not set.
   */
  static List<String> path(String name, @Nullable Path path) {
    if (path != null && !path.toString().isEmpty()) {
      return Arrays.asList("--" + name, path.toString());
    }
    return Collections.emptyList();
  }

  /**
   * Produces a single element list with a key/value pair comma separated string from a {@link Map}.
   *
   * @return {@code ["key1=value1,key2=value2,..."]} or {@code []} if keyValueMapping=empty/null
   */
  static List<String> keyValueString(@Nullable Map<?, ?> keyValueMapping) {
    List<String> result = Lists.newArrayList();
    if (keyValueMapping != null && keyValueMapping.size() > 0) {
      for (Map.Entry<?, ?> entry : keyValueMapping.entrySet()) {
        result.add(entry.getKey() + "=" + entry.getValue());
      }
      Joiner joiner = Joiner.on(",");
      return Collections.singletonList(joiner.join(result));
    }

    return Collections.emptyList();
  }

  /**
   * Produces a flagged key/value pair list given a flag name and a {@link Map}.
   *
   * @return {@code [--flagName, key1=value1, --flagName, key2=value2, ...]} or {@code []} if
   *     keyValueMapping=empty/null
   */
  static List<String> flaggedKeyValues(final String flagName, @Nullable Map<?, ?> keyValueMapping) {
    List<String> result = Lists.newArrayList();
    if (keyValueMapping != null && keyValueMapping.size() > 0) {
      for (Map.Entry<?, ?> entry : keyValueMapping.entrySet()) {
        result.addAll(string(flagName, entry.getKey() + "=" + entry.getValue()));
      }

      return result;
    }

    return Collections.emptyList();
  }
}
