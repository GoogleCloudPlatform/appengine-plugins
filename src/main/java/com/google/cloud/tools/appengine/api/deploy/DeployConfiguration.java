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

import com.google.auto.value.AutoValue;
import java.io.File;
import java.util.List;
import javax.annotation.Nullable;

/** Configuration for {@link AppEngineDeployment#deploy(DeployConfiguration)}. */
@AutoValue
public abstract class DeployConfiguration {
  /** GCS storage bucket used for staging files associated with deployment. */
  @Nullable
  public abstract String getBucket();

  /** List of deployable target directories or yaml files. */
  @Nullable
  public abstract List<File> getDeployables();

  /** Docker image to use during deployment (only for app.yaml deployments). */
  @Nullable
  public abstract String getImageUrl();

  /** Google Cloud Project ID to deploy to. */
  @Nullable
  public abstract String getProjectId();

  /** Promote the deployed version to receive all traffic. */
  @Nullable
  public abstract Boolean getPromote();

  /** The App Engine server to use. Users typically will never set this value. */
  @Nullable
  public abstract String getServer();

  /** Stop the previous running version when deploying and promoting a new version. */
  @Nullable
  public abstract Boolean getStopPreviousVersion();

  /** Version to deploy. */
  @Nullable
  public abstract String getVersion();

  public static Builder builder() {
    return new AutoValue_DeployConfiguration.Builder();
  }

  @AutoValue.Builder
  public abstract static class Builder {
    public abstract Builder setBucket(@Nullable String bucket);

    public abstract Builder setDeployables(@Nullable List<File> deployables);

    public abstract Builder setImageUrl(@Nullable String imageUrl);

    public abstract Builder setProjectId(@Nullable String projectId);

    public abstract Builder setPromote(@Nullable Boolean promote);

    public abstract Builder setServer(@Nullable String server);

    public abstract Builder setStopPreviousVersion(@Nullable Boolean stopPreviousVersion);

    public abstract Builder setVersion(@Nullable String version);

    public abstract DeployConfiguration build();
  }
}
