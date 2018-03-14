/*
 * Copyright 2018 Google Inc.
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

package com.google.cloud.tools.appengine.cloudsdk;

import com.google.cloud.tools.appengine.api.AppEngineException;
import com.google.cloud.tools.appengine.api.auth.Auth;
import com.google.cloud.tools.appengine.cloudsdk.internal.args.GcloudArgs;
import com.google.cloud.tools.appengine.cloudsdk.internal.process.ProcessRunnerException;
import com.google.common.base.Preconditions;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/** Cloud SDK based implementation of {@link Auth}. */
public class CloudSdkAuth implements Auth {
  private final CloudSdk cloudSdk;
  private final Pattern emailPattern =
      Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]+$", Pattern.CASE_INSENSITIVE);

  public CloudSdkAuth(CloudSdk cloudSdk) {
    this.cloudSdk = Preconditions.checkNotNull(cloudSdk);
  }

  /**
   * Logs into the cloud sdk with a specific user (does not retrigger auth flow if user is already
   * configured for the system).
   *
   * @param user a user email
   */
  @Override
  public void login(String user) {
    Preconditions.checkNotNull(user);
    Preconditions.checkArgument(emailPattern.matcher(user).find(), "Invalid email: " + user);
    try {
      cloudSdk.runAuthCommand(Arrays.asList("login", user));
    } catch (ProcessRunnerException e) {
      throw new AppEngineException(e);
    }
  }

  /** Launches the gcloud auth login flow. */
  @Override
  public void login() {
    try {
      cloudSdk.runAuthCommand(Collections.singletonList("login"));
    } catch (ProcessRunnerException e) {
      throw new AppEngineException(e);
    }
  }

  /**
   * Activates a service account based on a configured json key file.
   *
   * @param jsonFile a service account json key file
   */
  @Override
  public void activateServiceAccount(Path jsonFile) {
    Preconditions.checkArgument(Files.exists(jsonFile), "File does not exist: " + jsonFile);
    try {
      List<String> args = new ArrayList<>(3);
      args.add("activate-service-account");
      args.addAll(GcloudArgs.get("key-file", jsonFile));
      cloudSdk.runAuthCommand(args);
    } catch (ProcessRunnerException e) {
      throw new AppEngineException(e);
    }
  }
}
