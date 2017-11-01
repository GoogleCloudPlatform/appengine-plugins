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

package com.google.cloud.tools.managedcloudsdk;

import java.util.Locale;

/** Enum for Operating System detection. */
public class OsInfo {

  public enum Name {
    MAC,
    WINDOWS,
    LINUX;
  }

  public enum Arch {
    X86,
    X86_64
  }

  private final Name name;
  private final Arch arch;

  public OsInfo(Name name, Arch arch) {
    this.name = name;
    this.arch = arch;
  }

  public Name name() {
    return name;
  }

  public Arch arch() {
    return arch;
  }

  /**
   * Detects and returns the operating system.
   *
   * @return an {@link OsInfo} representation of the detected OS
   * @throws UnsupportedOsException if not Windows, Linux or MacOs
   */
  public static OsInfo getSystemOsInfo() throws UnsupportedOsException {
    return new OsInfo(getSystemOs(), getSystemArchitecture());
  }

  static Name getSystemOs() throws UnsupportedOsException {
    String osName = System.getProperty("os.name");
    return getSystemOs(osName);
  }

  static Name getSystemOs(String rawOsName) throws UnsupportedOsException {
    String osName = rawOsName.toLowerCase(Locale.ENGLISH);
    if (osName.contains("windows")) {
      return Name.WINDOWS;
    }

    if (osName.contains("linux")) {
      return Name.LINUX;
    }

    if (osName.contains("mac") || osName.contains("darwin")) {
      return Name.MAC;
    }

    throw new UnsupportedOsException("Unknown OS: " + rawOsName);
  }

  public static Arch getSystemArchitecture() {
    String arch = System.getProperty("os.arch");
    return getSystemArchitecture(arch);
  }

  static Arch getSystemArchitecture(String rawArch) {
    String arch = rawArch.toLowerCase(Locale.ENGLISH);
    if (arch.contains("64") || arch.contains("universal")) {
      return Arch.X86_64;
    }
    return Arch.X86;
  }
}
