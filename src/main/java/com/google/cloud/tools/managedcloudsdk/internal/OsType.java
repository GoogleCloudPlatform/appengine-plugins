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

package com.google.cloud.tools.managedcloudsdk.internal;

import com.google.cloud.tools.managedcloudsdk.UnsupportedOSException;
import java.util.Locale;

/** Created by appu on 8/30/17. */
public enum OsType {
  MAC,
  WINDOWS,
  LINUX;

  public static OsType getSystemOS() throws UnsupportedOSException {
    String osName = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
    if (osName.contains("windows")) {
      return OsType.WINDOWS;
    }

    if (osName.contains("linux")) {
      return OsType.LINUX;
    }

    if (osName.contains("mac") || osName.contains("darwin")) {
      return OsType.MAC;
    }

    throw new UnsupportedOSException("Unknown OS : " + osName);
  }
}
