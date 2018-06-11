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

package com.google.cloud.tools.appengine.api.deploy;

import java.io.File;
import javax.annotation.Nullable;

/**
 * Arguments needed to stage an App Engine standard environment application. Null return values
 * indicate that the configuration was not set, and thus assumes the tool default value.
 */
public interface StageStandardConfiguration {

  File getSourceDirectory();

  File getStagingDirectory();

  @Nullable
  File getDockerfile();

  @Nullable
  Boolean getEnableQuickstart();

  @Nullable
  Boolean getDisableUpdateCheck();

  @Nullable
  Boolean getEnableJarSplitting();

  @Nullable
  String getJarSplittingExcludes();

  @Nullable
  String getCompileEncoding();

  @Nullable
  Boolean getDeleteJsps();

  @Nullable
  Boolean getEnableJarClasses();

  @Nullable
  Boolean getDisableJarJsps();

  @Nullable
  String getRuntime();
}
