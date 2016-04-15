/**
 * Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.cloud.tools.app;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.cloud.tools.app.devserver.RunConfiguration;
import com.google.cloud.tools.app.executor.DevAppServerExecutor;
import com.google.cloud.tools.app.executor.ExecutorException;
import com.google.common.collect.ImmutableList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.util.List;

/**
 * Unit tests for {@link RunAction}.
 */
@RunWith(MockitoJUnitRunner.class)
public class RunActionTest {

  @Mock
  DevAppServerExecutor devAppServerExecutor;

  @Test
  public void testPrepareCommand_allFlags() throws ExecutorException {

    RunConfiguration configuration = DefaultRunConfiguration.newBuilder(new File("app.yaml"))
        .host("host")
        .port(8090)
        .adminHost("adminHost")
        .adminPort(8000)
        .authDomain("example.com")
        .storagePath("storage/path")
        .logLevel("debug")
        .maxModuleInstances(3)
        .useMtimeFileWatcher(true)
        .threadsafeOverride("default:False,backend:True")
        .pythonStartupScript("script.py")
        .pythonStartupArgs("arguments")
        .jvmFlags(ImmutableList.of("-Dtomato", "-Dpotato"))
        .customEntrypoint("entrypoint")
        .runtime("java")
        .allowSkippedFiles(true)
        .apiPort(8091)
        .automaticRestart(true)
        .devAppserverLogLevel("info")
        .skipSdkUpdateCheck(true)
        .defaultGcsBucketName("buckets")
        .build();

    RunAction action = new RunAction(configuration, devAppServerExecutor);

    List<String> expected = ImmutableList
        .of("app.yaml", "--host", "host", "--port", "8090", "--admin_host", "adminHost",
            "--admin_port", "8000", "--auth_domain", "example.com", "--storage_path",
            "storage/path", "--log_level", "debug", "--max_module_instances", "3",
            "--use_mtime_file_watcher", "--threadsafe_override", "default:False,backend:True",
            "--python_startup_script", "script.py", "--python_startup_args", "arguments",
            "--jvm_flag", "-Dtomato", "--jvm_flag", "-Dpotato", "--custom_entrypoint", "entrypoint",
            "--runtime", "java", "--allow_skipped_files", "--api_port", "8091",
            "--automatic_restart", "--dev_appserver_log_level", "info", "--skip_sdk_update_check",
            "--default_gcs_bucket_name", "buckets");

    action.execute();
    verify(devAppServerExecutor, times(1)).runDevAppServer(eq(expected));
  }

  @Test
  public void testPrepareCommand_noFlags() throws ExecutorException {

    RunConfiguration configuration = DefaultRunConfiguration.newBuilder(new File("app.yaml"))
        .build();

    RunAction action = new RunAction(configuration, devAppServerExecutor);

    List<String> expected = ImmutableList.of("app.yaml");

    action.execute();
    verify(devAppServerExecutor, times(1)).runDevAppServer(eq(expected));
  }

  public void testPrepareCommand_noFlagsAsync() throws ExecutorException {

    RunConfiguration configuration = DefaultRunConfiguration.newBuilder(new File("app.yaml"))
        .async(true)
        .build();

    RunAction action = new RunAction(configuration, devAppServerExecutor);

    List<String> expected = ImmutableList.of("app.yaml");

    action.execute();
    verify(devAppServerExecutor, times(1)).runDevAppServerAsync(eq(expected));
  }

}
