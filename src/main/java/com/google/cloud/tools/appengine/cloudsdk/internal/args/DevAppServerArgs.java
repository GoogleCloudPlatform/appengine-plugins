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

import java.util.List;

/**
 * Command Line argument helper for dev_appserver based command.
 */
public class DevAppServerArgs {

  /**
   * @return [--name, value] or [] if value=null.
   */
  public static List<String> get(String name, String value) {
    return Args.string(name, value);
  }

  /**
   * @return [--name, value1, --name, value2, ...] or [] if value=null.
   */
  public static List<String> get(String name, List<String> values) {
    return Args.strings(name, values);
  }

  /**
   * @return [--name, value] or [] if value=null.
   */
  public static List<String> get(String name, Integer value) {
    return Args.integer(name, value);
  }

  /**
   * @return [--name] if value=true, [] if value=false/null.
   */
  public static List<String> get(String name, Boolean value) {
    return Args.bool(name, value);
  }
}
