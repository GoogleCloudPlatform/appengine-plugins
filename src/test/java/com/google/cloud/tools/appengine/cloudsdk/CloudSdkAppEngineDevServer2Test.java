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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.cloud.tools.appengine.api.AppEngineException;
import com.google.cloud.tools.appengine.api.devserver.RunConfiguration;
import com.google.cloud.tools.appengine.cloudsdk.process.ProcessHandlerException;
import com.google.cloud.tools.test.utils.SpyVerifier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/** Unit tests for {@link CloudSdkAppEngineDevServer2}. */
@RunWith(MockitoJUnitRunner.class)
public class CloudSdkAppEngineDevServer2Test {

  @Mock private DevAppServerRunner devAppServerRunner;
  private Path fakeStoragePath = Paths.get("storage/path");
  private Path fakeDatastorePath = Paths.get("datastore/path");
  private List<Path> fakeExplodedWarService = ImmutableList.of(Paths.get("exploded-war/"));

  private CloudSdkAppEngineDevServer2 devServer;

  @Before
  public void setUp() {
    devServer = new CloudSdkAppEngineDevServer2(devAppServerRunner);
  }

  @Test
  public void tesNullSdk() {
    try {
      new CloudSdkAppEngineDevServer2(null);
      Assert.fail("Allowed null runner");
    } catch (NullPointerException expected) {
      // pass
    }
  }

  @Test
  public void testPrepareCommand_allFlags() throws Exception {

    RunConfiguration configuration =
        Mockito.spy(
            RunConfiguration.builder(fakeExplodedWarService)
                .host("host")
                .port(8090)
                .adminHost("adminHost")
                .adminPort(8000)
                .authDomain("example.com")
                .storagePath(fakeStoragePath)
                .logLevel("debug")
                .maxModuleInstances(3)
                .useMtimeFileWatcher(true)
                .threadsafeOverride("default:False,backend:True")
                .pythonStartupScript("script.py")
                .pythonStartupArgs("arguments")
                .jvmFlags(ImmutableList.of("-Dflag1", "-Dflag2"))
                .customEntrypoint("entrypoint")
                .runtime("java")
                .allowSkippedFiles(true)
                .apiPort(8091)
                .automaticRestart(false)
                .devAppserverLogLevel("info")
                .skipSdkUpdateCheck(true)
                .defaultGcsBucketName("buckets")
                .clearDatastore(true)
                .datastorePath(fakeDatastorePath)
                .environment(ImmutableMap.of("ENV_NAME", "ENV_VAL"))
                .projectId("my-project")
                .additionalArguments(Arrays.asList("--ARG1", "--ARG2"))
                .build());

    SpyVerifier.newVerifier(configuration).verifyAllValuesNotNull();

    List<String> expected =
        ImmutableList.of(
            "exploded-war",
            "--host=host",
            "--port=8090",
            "--admin_host=adminHost",
            "--admin_port=8000",
            "--auth_domain=example.com",
            "--storage_path=" + fakeStoragePath,
            "--log_level=debug",
            "--max_module_instances=3",
            "--use_mtime_file_watcher=true",
            "--threadsafe_override=default:False,backend:True",
            "--python_startup_script=script.py",
            "--python_startup_args=arguments",
            "--jvm_flag=-Dflag1",
            "--jvm_flag=-Dflag2",
            "--custom_entrypoint=entrypoint",
            "--runtime=java",
            "--allow_skipped_files=true",
            "--api_port=8091",
            "--automatic_restart=false",
            "--dev_appserver_log_level=info",
            "--skip_sdk_update_check=true",
            "--default_gcs_bucket_name=buckets",
            "--clear_datastore=true",
            "--datastore_path=" + fakeDatastorePath,
            "--env_var",
            "ENV_NAME=ENV_VAL",
            "--application=my-project",
            "--ARG1",
            "--ARG2");

    devServer.run(configuration);

    verify(devAppServerRunner, times(1)).runV2(eq(expected));

    SpyVerifier.newVerifier(configuration).verifyDeclaredGetters(ImmutableMap.of("getServices", 3));
  }

  @Test
  public void testPrepareCommand_booleanFlags()
      throws AppEngineException, ProcessHandlerException, IOException {
    RunConfiguration configuration =
        RunConfiguration.builder(fakeExplodedWarService)
            .useMtimeFileWatcher(false)
            .allowSkippedFiles(false)
            .automaticRestart(false)
            .skipSdkUpdateCheck(false)
            .clearDatastore(false)
            .build();

    List<String> expected =
        ImmutableList.of(
            "exploded-war",
            "--use_mtime_file_watcher=false",
            "--allow_skipped_files=false",
            "--automatic_restart=false",
            "--skip_sdk_update_check=false",
            "--clear_datastore=false");

    devServer.run(configuration);
    verify(devAppServerRunner, times(1)).runV2(eq(expected));
  }

  @Test
  public void testPrepareCommand_noFlags()
      throws AppEngineException, ProcessHandlerException, IOException {

    RunConfiguration configuration = RunConfiguration.builder(fakeExplodedWarService).build();

    List<String> expected = ImmutableList.of("exploded-war");

    devServer.run(configuration);

    verify(devAppServerRunner, times(1)).runV2(eq(expected));
  }

  @Test
  public void testPrepareCommand_clientEnvVars()
      throws AppEngineException, ProcessHandlerException, IOException {

    Map<String, String> clientEnvVars = ImmutableMap.of("key1", "val1", "key2", "val2");

    RunConfiguration configuration =
        RunConfiguration.builder(fakeExplodedWarService).environment(clientEnvVars).build();

    List<String> expectedArgs =
        ImmutableList.of("exploded-war", "--env_var", "key1=val1", "--env_var", "key2=val2");

    devServer.run(configuration);

    verify(devAppServerRunner, times(1)).runV2(eq(expectedArgs));
  }
}
