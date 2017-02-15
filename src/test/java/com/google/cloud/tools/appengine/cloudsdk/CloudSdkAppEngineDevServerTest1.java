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
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;
import java.util.List;
import java.util.Map;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link CloudSdkAppEngineDevServer}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CloudSdkAppEngineDevServerTest1 {

  @Mock
  private CloudSdk sdk;

  private CloudSdkAppEngineDevServer1 devServer;

  private final File serviceDirectory = new File("src/test/java/resources");

  @Before
  public void setUp() {
    devServer = new CloudSdkAppEngineDevServer1(sdk);
  }

  @Test
  public void tesNullSdk() {
    try {
      new CloudSdkAppEngineDevServer1(null);
      Assert.fail("Allowed null SDK");
    } catch (NullPointerException expected) {
    }
  }

  @Test
  public void testPrepareCommand_allFlags() throws AppEngineException, ProcessRunnerException {

    DefaultRunConfiguration configuration = new DefaultRunConfiguration();
    configuration.setServices(ImmutableList.of(serviceDirectory));
    configuration.setHost("host");
    configuration.setPort(8090);
    configuration.setJvmFlags(ImmutableList.of("-Dflag1", "-Dflag2"));
    configuration.setRuntime("java");
    configuration.setJavaHomeDir("/usr/lib/jvm/default-java");

    List<String> expectedFlags = ImmutableList.of(
            "--server=host", "--address=8090", "--jvm_flag=-Dflag1",
            "--jvm_flag=-Dflag2", "--allow_remote_shutdown",
            "--no_java_agent", "src/test/java/resources");

    Map<String, String> expectedEnv = ImmutableMap.of("JAVA_HOME", "/usr/lib/jvm/default-java");
    List<String> expectedJvmArgs = ImmutableList.of("-Duse_jetty9_runtime=true",
            "-D--enable_all_permissions=true");

    devServer.run(configuration);

    verify(sdk, times(1)).runDevAppServer1Command(eq(expectedJvmArgs), eq(expectedFlags), eq(expectedEnv));
  }

  @Test
  public void testPrepareCommand_booleanFlags() throws AppEngineException, ProcessRunnerException {
    DefaultRunConfiguration configuration = new DefaultRunConfiguration();

    configuration.setServices(ImmutableList.of(serviceDirectory));

    List<String> expectedFlags = ImmutableList.of("--allow_remote_shutdown",
            "--no_java_agent", "src/test/java/resources");
    Map<String, String> expectedEnv = ImmutableMap.of();
    List<String> expectedJvmArgs = ImmutableList.of("-Duse_jetty9_runtime=true",
            "-D--enable_all_permissions=true");
    devServer.run(configuration);
    verify(sdk, times(1)).runDevAppServer1Command(eq(expectedJvmArgs), eq(expectedFlags), eq(expectedEnv));
  }

  @Test
  public void testPrepareCommand_noFlags() throws AppEngineException, ProcessRunnerException {

    DefaultRunConfiguration configuration = new DefaultRunConfiguration();
    configuration.setServices(ImmutableList.of(serviceDirectory));

    List<String> expectedFlags = ImmutableList.of("--allow_remote_shutdown",
            "--no_java_agent", "src/test/java/resources");
    Map<String, String> expectedEnv = ImmutableMap.of();

    List<String> expectedJvmArgs = ImmutableList.of("-Duse_jetty9_runtime=true",
            "-D--enable_all_permissions=true");

    devServer.run(configuration);

    verify(sdk, times(1)).runDevAppServer1Command(eq(expectedJvmArgs), eq(expectedFlags), eq(expectedEnv));
  }

}
