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

package com.google.cloud.tools.libraries.json;

import javax.annotation.Nullable;

/** Holds details about a single Cloud library client. */
public final class CloudLibraryClient {

  @Nullable private String name;
  @Nullable private String language;
  @Nullable private String site;
  @Nullable private String apireference;
  @Nullable private String infotip;
  @Nullable private String launchStage;
  @Nullable private String source;
  @Nullable private String languageLevel;
  @Nullable private CloudLibraryClientMavenCoordinates mavenCoordinates;

  /** Prevents direct instantiation. GSON instantiates these objects using dark magic. */
  private CloudLibraryClient() {}

  /** Returns the name of this client. */
  @Nullable
  public String getName() {
    return name;
  }

  /** Returns the language this client is for. */
  @Nullable
  public String getLanguage() {
    return language;
  }

  /** Returns a URL to this client's main site. */
  @Nullable
  public String getSite() {
    return site;
  }

  /** Returns a URL to this client's API reference documentation. */
  @Nullable
  public String getApiReference() {
    return apireference;
  }

  /** Returns tip information for this client. */
  @Nullable
  public String getInfoTip() {
    return infotip;
  }

  /** Returns the launch stage of this client (e.g. alpha, beta, GA, etc.). */
  @Nullable
  public String getLaunchStage() {
    return launchStage;
  }

  /** Returns a URL to the source of this client. */
  @Nullable
  public String getSource() {
    return source;
  }

  /** Returns the language level of the source for this client. */
  @Nullable
  public String getLanguageLevel() {
    return languageLevel;
  }

  /** Returns the Maven artifact details. */
  @Nullable
  public CloudLibraryClientMavenCoordinates getMavenCoordinates() {
    return mavenCoordinates;
  }
}
