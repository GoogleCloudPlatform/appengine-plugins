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

package com.google.cloud.tools.appengine.cloudsdk;

import com.google.cloud.tools.appengine.api.AppEngineFactory;
import com.google.cloud.tools.appengine.api.deploy.AppEngineDeployment;
import com.google.cloud.tools.appengine.api.deploy.AppEngineFlexibleStaging;
import com.google.cloud.tools.appengine.api.deploy.AppEngineStandardStaging;
import com.google.cloud.tools.appengine.api.devserver.AppEngineDevServer;
import com.google.cloud.tools.appengine.api.genconfig.GenConfigUtility;
import com.google.cloud.tools.appengine.api.instances.AppEngineInstances;
import com.google.cloud.tools.appengine.api.logs.AppEngineLogs;
import com.google.cloud.tools.appengine.api.services.AppEngineServices;
import com.google.cloud.tools.appengine.api.versions.AppEngineVersions;
import com.google.cloud.tools.appengine.cloudsdk.internal.CloudSdkAppEngineDeployment;
import com.google.cloud.tools.appengine.cloudsdk.internal.CloudSdkAppEngineDevServer;
import com.google.cloud.tools.appengine.cloudsdk.internal.CloudSdkAppEngineFlexibleStaging;
import com.google.cloud.tools.appengine.cloudsdk.internal.CloudSdkAppEngineGenConfig;
import com.google.cloud.tools.appengine.cloudsdk.internal.CloudSdkAppEngineInstances;
import com.google.cloud.tools.appengine.cloudsdk.internal.CloudSdkAppEngineLogs;
import com.google.cloud.tools.appengine.cloudsdk.internal.CloudSdkAppEngineServices;
import com.google.cloud.tools.appengine.cloudsdk.internal.CloudSdkAppEngineStandardStaging;
import com.google.cloud.tools.appengine.cloudsdk.internal.CloudSdkAppEngineVersions;
import com.google.cloud.tools.appengine.cloudsdk.internal.sdk.CloudSdk;
import com.google.cloud.tools.appengine.cloudsdk.process.ProcessExitListener;
import com.google.cloud.tools.appengine.cloudsdk.process.ProcessOutputLineListener;
import com.google.cloud.tools.appengine.cloudsdk.process.ProcessStartListener;

import java.io.File;

/**
 * A factory for creating Cloud SDK-based implementations of the App Engine services.
 */
public class CloudSdkAppEngineFactory implements AppEngineFactory {

  private CloudSdk cloudSdk;

  private CloudSdkAppEngineFactory(CloudSdk cloudSdk) {
    this.cloudSdk = cloudSdk;
  }

  @Override
  public AppEngineDeployment deployment() {
    return new CloudSdkAppEngineDeployment(cloudSdk);
  }

  @Override
  public AppEngineDevServer devServer() {
    return new CloudSdkAppEngineDevServer(cloudSdk);
  }

  @Override
  public AppEngineFlexibleStaging flexibleStaging() {
    return new CloudSdkAppEngineFlexibleStaging();
  }

  @Override
  public GenConfigUtility genConfigUtility() {
    return new CloudSdkAppEngineGenConfig(cloudSdk);
  }

  @Override
  public AppEngineInstances instances() {
    return new CloudSdkAppEngineInstances(cloudSdk);
  }

  @Override
  public AppEngineLogs logs() {
    return new CloudSdkAppEngineLogs(cloudSdk);
  }

  @Override
  public AppEngineServices services() {
    return new CloudSdkAppEngineServices(cloudSdk);
  }

  @Override
  public AppEngineStandardStaging standardStaging() {
    return new CloudSdkAppEngineStandardStaging(cloudSdk);
  }

  @Override
  public AppEngineVersions versions() {
    return new CloudSdkAppEngineVersions(cloudSdk);
  }

  public static class Builder {
    private CloudSdk.Builder cloudSdkBuilder;

    /**
     * The home directory of Google Cloud SDK. If not set, will attempt to look for the SDK in known
     * install locations.
     */
    public Builder sdkPath(File sdkPathFile) {
      cloudSdkBuilder.sdkPath(sdkPathFile);
      return this;
    }

    /**
     * The metrics environment.
     */
    public Builder appCommandMetricsEnvironment(String appCommandMetricsEnvironment) {
      cloudSdkBuilder.appCommandMetricsEnvironment(appCommandMetricsEnvironment);
      return this;
    }

    /**
     * The metrics environment version.
     */
    public Builder appCommandMetricsEnvironmentVersion(String appCommandMetricsEnvironmentVersion) {
      cloudSdkBuilder.appCommandMetricsEnvironmentVersion(appCommandMetricsEnvironmentVersion);
      return this;
    }

    /**
     * Configures usage of gsutil.
     */
    public Builder appCommandGsUtil(Integer appCommandGsUtil) {
      cloudSdkBuilder.appCommandGsUtil(appCommandGsUtil);
      return this;
    }

    /**
     * Sets the path the credential override file.
     */
    public Builder appCommandCredentialFile(File appCommandCredentialFile) {
      cloudSdkBuilder.appCommandCredentialFile(appCommandCredentialFile);
      return this;
    }

    /**
     * Sets the format for printing command output resources. The default is a command-specific
     * human-friendly output format. The supported formats are: csv, default, flattened, json, list,
     * multi, none, table, text, value, yaml. For more details run $ gcloud topic formats.
     */
    public Builder appCommandOutputFormat(String appCommandOutputFormat) {
      cloudSdkBuilder.appCommandOutputFormat(appCommandOutputFormat);
      return this;
    }

    /**
     * Whether to run commands asynchronously.
     */
    public Builder async(boolean async) {
      cloudSdkBuilder.async(async);
      return this;
    }

    /**
     * Adds a client consumer of process standard output. If none, output will be inherited by
     * parent process.
     */
    public Builder addStdOutLineListener(ProcessOutputLineListener stdOutLineListener) {
      cloudSdkBuilder.addStdOutLineListener(stdOutLineListener);
      return this;
    }

    /**
     * Adds a client consumer of process error output. If none, output will be inherited by parent
     * process.
     */
    public Builder addStdErrLineListener(ProcessOutputLineListener stdErrLineListener) {
      cloudSdkBuilder.addStdErrLineListener(stdErrLineListener);
      return this;
    }

    /**
     * The client listener of the process exit with code.
     */
    public Builder exitListener(ProcessExitListener exitListener) {
      cloudSdkBuilder.exitListener(exitListener);
      return this;
    }

    /**
     * The client listener of the process start. Allows access to the underlying process.
     */
    public Builder startListener(ProcessStartListener startListener) {
      cloudSdkBuilder.startListener(startListener);
      return this;
    }

    /**
     * When run asynchronously, configure the Dev App Server command to wait for successful start of
     * the server. Setting this will force process output not to be inherited by the caller.
     *
     * @param runDevAppServerWaitSeconds Number of seconds to wait > 0.
     */
    public Builder runDevAppServerWait(int runDevAppServerWaitSeconds) {
      cloudSdkBuilder.runDevAppServerWait(runDevAppServerWaitSeconds);
      return this;
    }

    /**
     * Create a new instance of {@link CloudSdk}.
     */
    public CloudSdkAppEngineFactory build() {
      return new CloudSdkAppEngineFactory(cloudSdkBuilder.build());
    }

  }
}
