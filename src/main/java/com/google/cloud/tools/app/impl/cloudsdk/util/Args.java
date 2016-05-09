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

package com.google.cloud.tools.app.impl.cloudsdk.util;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Command Line argument helper.
 */
public class Args {

  /**
   * @return [--name, value] or [] if value=null.
   */
  public static List<String> string(String name, String value) {
    if (!Strings.isNullOrEmpty(value)) {
      return Arrays.asList("--" + name, value);
    }
    return Collections.emptyList();
  }

  /**
   * @return [--name=value] or [] if value=null.
   */
  public static List<String> stringEq(String name, String value) {
    if (!Strings.isNullOrEmpty(value)) {
      return Collections.singletonList("--" + name + "=" + value);
    }
    return Collections.emptyList();
  }

  /**
   * @return [--name, value1, --name, value2, ...] or [] if value=null.
   */
  public static List<String> strings(String name, List<String> values) {
    List<String> result = Lists.newArrayList();
    if (values != null) {
      for (String value : values) {
        result.add("--" + name);
        result.add(value);
      }
    }
    return result;
  }

  /**
   * @return [--name, value] or [] if value=null.
   */
  public static List<String> integer(String name, Integer value) {
    if (value != null) {
      return Arrays.asList("--" + name, value.toString());
    }
    return Collections.emptyList();
  }

  /**
   * @return [--name] if value=true, [--no-name] if value=false, [] if value=null.
   */
  public static List<String> boolWithNo(String name, Boolean value) {
    if (value != null) {
      if (value) {
        return Collections.singletonList("--" + name);
      }
      return Collections.singletonList("--no-" + name);
    }
    return Collections.emptyList();
  }

  /**
   * @return [--name] if value=true, [] if value=false/null.
   */
  public static List<String> bool(String name, Boolean value) {
    if (Boolean.TRUE.equals(value)) {
      return Collections.singletonList("--" + name);
    }
    return Collections.emptyList();
  }

  /**
   * @return [key1=value1, key2=value2, ...]
   */
  public static List<String> keyValues(Map<?, ?> keyValueMapping) {
    List<String> result = Lists.newArrayList();
    for (Map.Entry<?, ?> entry : keyValueMapping.entrySet()) {
      result.add(entry.getKey() + "=" + entry.getValue());
    }

    return result;
  }
}
