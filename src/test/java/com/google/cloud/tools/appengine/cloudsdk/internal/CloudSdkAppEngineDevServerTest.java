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

package com.google.cloud.tools.appengine.cloudsdk.internal;

import com.google.cloud.tools.appengine.api.AppEngineException;
import com.google.cloud.tools.appengine.api.devserver.DefaultRunConfiguration;
import com.google.cloud.tools.appengine.cloudsdk.internal.process.ProcessRunnerException;
import com.google.cloud.tools.appengine.cloudsdk.internal.sdk.CloudSdk;
import com.google.common.collect.ImmutableList;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.util.List;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link CloudSdkAppEngineDevServer}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CloudSdkAppEngineDevServerTest {

  @Mock
  private CloudSdk sdk;

  private CloudSdkAppEngineDevServer devServer;

  @Before
  public void setUp() {
    devServer = new CloudSdkAppEngineDevServer(sdk);
  }

  @Test
  public void testPrepareCommand_allFlags() throws AppEngineException, ProcessRunnerException {

    DefaultRunConfiguration configuration = new DefaultRunConfiguration();
    configuration.setAppYamls(ImmutableList.of(new File("app.yaml")));
    configuration.setHost("host");
    configuration.setPort(8090);
    configuration.setAdminHost("adminHost");
    configuration.setAdminPort(8000);
    configuration.setAuthDomain("example.com");
    configuration.setStoragePath("storage/path");
    configuration.setLogLevel("debug");
    configuration.setMaxModuleInstances(3);
    configuration.setUseMtimeFileWatcher(true);
    configuration.setThreadsafeOverride("default:False,backend:True");
    configuration.setPythonStartupScript("script.py");
    configuration.setPythonStartupArgs("arguments");
    configuration.setJvmFlags(ImmutableList.of("-Dtomato", "-Dpotato"));
    configuration.setCustomEntrypoint("entrypoint");
    configuration.setRuntime("java");
    configuration.setAllowSkippedFiles(true);
    configuration.setApiPort(8091);
    configuration.setAutomaticRestart(true);
    configuration.setDevAppserverLogLevel("info");
    configuration.setSkipSdkUpdateCheck(true);
    configuration.setDefaultGcsBucketName("buckets");

    List<String> expected = ImmutableList
        .of("app.yaml", "--host", "host", "--port", "8090", "--admin_host", "adminHost",
            "--admin_port", "8000", "--auth_domain", "example.com", "--storage_path",
            "storage/path", "--log_level", "debug", "--max_module_instances", "3",
            "--use_mtime_file_watcher", "--threadsafe_override", "default:False,backend:True",
            "--python_startup_script", "script.py", "--python_startup_args", "arguments",
            "--jvm_flag", "-Dtomato", "--jvm_flag", "-Dpotato", "--custom_entrypoint", "entrypoint",
            "--runtime", "java", "--allow_skipped_files", "--api_port", "8091",
            "--automatic_restart", "--dev_appserver_log_level", "info",
            "--skip_sdk_update_check", "--default_gcs_bucket_name", "buckets");

    devServer.run(configuration);

    verify(sdk, times(1)).runDevAppServerCommand(eq(expected));
  }

  public void testPrepareCommand_booleanFlags() throws AppEngineException, ProcessRunnerException {
    DefaultRunConfiguration configuration = new DefaultRunConfiguration();

    devServer.run(configuration);
    configuration.setAppYamls(ImmutableList.of(new File("app.yaml")));
    configuration.setUseMtimeFileWatcher(false);
    configuration.setAllowSkippedFiles(false);
    configuration.setAutomaticRestart(false);
    configuration.setSkipSdkUpdateCheck(false);

    List<String> expected = ImmutableList.of("app.yaml");

    devServer.run(configuration);
    verify(sdk, times(1)).runDevAppServerCommand(eq(expected));
  }

  @Test
  public void testPrepareCommand_noFlags() throws AppEngineException, ProcessRunnerException {

    DefaultRunConfiguration configuration = new DefaultRunConfiguration();
    configuration.setAppYamls(ImmutableList.of(new File("app.yaml")));

    List<String> expected = ImmutableList.of("app.yaml");

    devServer.run(configuration);

    verify(sdk, times(1)).runDevAppServerCommand(eq(expected));
  }

}
