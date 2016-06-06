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

package com.google.cloud.tools.app.cloudsdk.internal.args;

import com.google.cloud.tools.app.api.Configuration;
import com.google.common.collect.Lists;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Command Line argument helper for gcloud based commands.
 */
public class GcloudArgs {

  /**
   * @return [--name, value] or [] if value=null.
   */
  public static List<String> get(String name, String value) {
    return Args.string(name, value);
  }

  /**
   * @return [--name, value] or [] if value=null.
   */
  public static List<String> get(String name, Integer value) {
    return Args.integer(name, value);
  }

  /**
   * @return [--name] if value=true, [--no-name] if value=false, [] if value=null.
   */
  public static List<String> get(String name, Boolean value) {
    return Args.boolWithNo(name, value);
  }

  /**
   * @return [--name, file.getAbsolutePath()] or [] if file=null.
   */
  public static List<String> get(String name, File file) {
    return Args.filePath(name, file);
  }

  /**
   * @return [key1=value1,key2=value2,...], [] if keyValueMapping=empty/null
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
