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

import com.google.cloud.tools.appengine.api.AppEngineException;
import com.google.cloud.tools.appengine.api.devserver.DefaultRunConfiguration;
import com.google.cloud.tools.appengine.cloudsdk.internal.process.ProcessRunnerException;
import com.google.cloud.tools.test.utils.SpyVerifier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

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
  public void tesNullSdk() {
    try {
      new CloudSdkAppEngineDevServer(null);
      Assert.fail("Allowed null SDK");
    } catch (NullPointerException expected) {
    }
  }

  @Test
  public void testPrepareCommand_allFlags() throws Exception {

    DefaultRunConfiguration configuration = Mockito.spy(new DefaultRunConfiguration());
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
    configuration.setJvmFlags(ImmutableList.of("-Dflag1", "-Dflag2"));
    configuration.setCustomEntrypoint("entrypoint");
    configuration.setRuntime("java");
    configuration.setAllowSkippedFiles(true);
    configuration.setApiPort(8091);
    configuration.setAutomaticRestart(false);
    configuration.setDevAppserverLogLevel("info");
    configuration.setSkipSdkUpdateCheck(true);
    configuration.setDefaultGcsBucketName("buckets");
    configuration.setJavaHomeDir("/usr/lib/jvm/default-java");
    configuration.setClearDatastore(true);

    List<String> expected = ImmutableList
        .of("app.yaml", "--host=host", "--port=8090", "--admin_host=adminHost",
            "--admin_port=8000", "--auth_domain=example.com", "--storage_path=storage/path",
            "--log_level=debug", "--max_module_instances=3", "--use_mtime_file_watcher=true",
            "--threadsafe_override=default:False,backend:True", "--python_startup_script=script.py",
            "--python_startup_args=arguments", "--jvm_flag=-Dflag1", "--jvm_flag=-Dflag2",
            "--custom_entrypoint=entrypoint", "--runtime=java", "--allow_skipped_files=true",
            "--api_port=8091", "--automatic_restart=false", "--dev_appserver_log_level=info",
            "--skip_sdk_update_check=true", "--default_gcs_bucket_name=buckets",
            "--clear_datastore=true");

    Map<String,String> expectedEnv = ImmutableMap.of("JAVA_HOME", "/usr/lib/jvm/default-java");

    devServer.run(configuration);

    verify(sdk, times(1)).runDevAppServerCommand(eq(expected), eq(expectedEnv));

    SpyVerifier.newVerifier(configuration).verifyDeclaredGetters(
        ImmutableMap.<String, Integer>of("getJavaHomeDir", 2, "getServices", 0, "getAppYamls", 3));

  }

  @Test
  public void testPrepareCommand_booleanFlags() throws AppEngineException, ProcessRunnerException {
    DefaultRunConfiguration configuration = new DefaultRunConfiguration();

    configuration.setAppYamls(ImmutableList.of(new File("app.yaml")));
    configuration.setUseMtimeFileWatcher(false);
    configuration.setAllowSkippedFiles(false);
    configuration.setAutomaticRestart(false);
    configuration.setSkipSdkUpdateCheck(false);
    configuration.setClearDatastore(false);

    List<String> expected = ImmutableList
        .of("app.yaml", "--use_mtime_file_watcher=false", "--allow_skipped_files=false",
            "--automatic_restart=false", "--skip_sdk_update_check=false",
            "--clear_datastore=false");
    Map<String,String> expectedEnv = ImmutableMap.of();

    devServer.run(configuration);
    verify(sdk, times(1)).runDevAppServerCommand(eq(expected), eq(expectedEnv));
  }

  @Test
  public void testPrepareCommand_noFlags() throws AppEngineException, ProcessRunnerException {

    DefaultRunConfiguration configuration = new DefaultRunConfiguration();
    configuration.setAppYamls(ImmutableList.of(new File("app.yaml")));

    List<String> expected = ImmutableList.of("app.yaml");
    Map<String,String> expectedEnv = ImmutableMap.of();

    devServer.run(configuration);

    verify(sdk, times(1)).runDevAppServerCommand(eq(expected), eq(expectedEnv));
  }

}
