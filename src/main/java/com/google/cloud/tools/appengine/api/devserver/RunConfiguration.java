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

package com.google.cloud.tools.appengine.api.devserver;

import java.io.File;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

/** Configuration for {@link AppEngineDevServer#run(RunConfiguration)}. */
public interface RunConfiguration {

  // TODO(joaomartins): Only contains common, jvm, Python, VM and misc flags for now. Need to add
  // PHP, AppIdentity, Blobstore, etc.
  /**
   * List of all the service web output directories (1 or more) that need to be run with the local
   * devappserver. Such directory needs to include WEB-INF/appengine-web.xml.
   */
  @Nullable
  List<File> getServices();

  @Nullable
  String getHost();

  @Nullable
  Integer getPort();

  @Nullable
  String getAdminHost();

  @Nullable
  Integer getAdminPort();

  @Nullable
  String getAuthDomain();

  @Nullable
  File getStoragePath();

  @Nullable
  String getLogLevel();

  @Nullable
  Integer getMaxModuleInstances();

  @Nullable
  Boolean getUseMtimeFileWatcher();

  @Nullable
  String getThreadsafeOverride();

  @Nullable
  String getPythonStartupScript();

  @Nullable
  String getPythonStartupArgs();

  @Nullable
  List<String> getJvmFlags();

  @Nullable
  String getCustomEntrypoint();

  @Nullable
  String getRuntime();

  @Nullable
  Boolean getAllowSkippedFiles();

  @Nullable
  Integer getApiPort();

  @Nullable
  Boolean getAutomaticRestart();

  @Nullable
  String getDevAppserverLogLevel();

  @Nullable
  Boolean getSkipSdkUpdateCheck();

  @Nullable
  String getDefaultGcsBucketName();

  @Nullable
  Boolean getClearDatastore();

  @Nullable
  File getDatastorePath();

  @Nullable
  Map<String, String> getEnvironment();

  /**
   * Any additional arguments to be passed to the appserver. These arguments are neither parsed nor
   * validated.
   */
  @Nullable
  List<String> getAdditionalArguments();

  /** Gets the GCP project ID. */
  @Nullable
  String getProjectId();
}
