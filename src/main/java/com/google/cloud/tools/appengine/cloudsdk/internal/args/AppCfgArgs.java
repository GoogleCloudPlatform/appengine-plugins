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

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;

/** Command Line argument helper for AppCfg based commands. */
public class AppCfgArgs {

  /** Returns {@code [--name=value]} or {@code []} if value=null. */
  public static List<String> get(String name, @Nullable String value) {
    return Args.stringWithEq(name, value);
  }

  /** Returns {@code [--name]} if value=true, {@code []} if value=false/null. */
  public static List<String> get(String name, @Nullable Boolean value) {
    if (Boolean.TRUE.equals(value)) {
      return Collections.singletonList("--" + name);
    }
    return Collections.emptyList();
  }
}
