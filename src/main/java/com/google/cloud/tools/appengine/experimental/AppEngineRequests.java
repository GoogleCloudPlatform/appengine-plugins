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

package com.google.cloud.tools.appengine.experimental;

import com.google.cloud.tools.appengine.experimental.internal.cloudsdk.CloudSdkAppEngineRequestFactory;
import java.nio.file.Path;
import javax.annotation.Nonnull;

/** Entry point to generate {@link AppEngineRequestFactory}. */
public class AppEngineRequests {
  public static AppEngineRequestFactoryBuilder newRequestFactoryBuilder() {
    return new AppEngineRequestFactoryBuilder();
  }

  public static class AppEngineRequestFactoryBuilder {
    Path cloudSdkHome;
    Path credentialFile;
    String metricsEnvironment;
    String metricsEnvironmentVersion;
    Factory factory;

    private enum Factory {
      CLOUD_SDK;
    }

    private AppEngineRequestFactoryBuilder() {}

    /** Configure the factory to use the Google Cloud SDK. */
    public AppEngineRequestFactoryBuilder cloudSdk(@Nonnull Path cloudSdkHome) {
      this.cloudSdkHome = cloudSdkHome;
      this.factory = Factory.CLOUD_SDK;
      return this;
    }

    /** Configure a credential file override. */
    public AppEngineRequestFactoryBuilder credentialFile(Path credentialFile) {
      this.credentialFile = credentialFile;
      return this;
    }

    /** Configure the metrics environment for user agent modification. */
    public AppEngineRequestFactoryBuilder metricsEnvironment(
        String metricsEnvironment, String metricsEnvironmentVersion) {
      this.metricsEnvironment = metricsEnvironment;
      this.metricsEnvironmentVersion = metricsEnvironmentVersion;
      return this;
    }

    /** Build a new {@link AppEngineRequestFactory} based on the builder configuration. */
    public AppEngineRequestFactory build() {
      if (factory == Factory.CLOUD_SDK) {
        return new CloudSdkAppEngineRequestFactory(
            cloudSdkHome, credentialFile, metricsEnvironment, metricsEnvironmentVersion);
      }

      throw new IllegalStateException("No App Engine request factory implementation defined");
    }
  }
}
