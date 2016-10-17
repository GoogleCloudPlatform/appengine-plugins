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
   * @return {@code [--name=value]} or {@code []} if value=null.
   */
  public static List<String> get(String name, String value) {
    return Args.stringWithEq(name, value);
  }

  /**
   * @return {@code [--name=value1, --name=value2, ...]} or {@code []} if value=null.
   */
  public static List<String> get(String name, List<String> values) {
    return Args.stringsWithEq(name, values);
  }

  /**
   * @return {@code [--name=value]} or {@code []} if value=null.
   */
  public static List<String> get(String name, Integer value) {
    return Args.integerWithEq(name, value);
  }

  /**
   * @return {@code [--name=true]} if value=true, {@code [--name=false]} if value=false,
   *     {@code []} if value=null.
   */
  public static List<String> get(String name, Boolean value) {
    return Args.boolWithTrueFalse(name, value);
  }
}
