/*
 * Copyright 2016-2022 Google LLC.
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

package com.google.cloud.tools.appengine.operations;

import static com.google.common.base.StandardSystemProperty.JAVA_SPECIFICATION_VERSION;
import static org.junit.Assume.assumeTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.cloud.tools.appengine.AppEngineException;
import com.google.cloud.tools.appengine.configuration.RunConfiguration;
import com.google.cloud.tools.appengine.configuration.StopConfiguration;
import com.google.cloud.tools.appengine.operations.cloudsdk.process.ProcessHandlerException;
import com.google.cloud.tools.test.utils.LogStoringHandler;
import com.google.cloud.tools.test.utils.SpyVerifier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/** Unit tests for {@link DevServer}. */
@RunWith(MockitoJUnitRunner.class)
public class DevServerJava8Test {

  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();
  private Path fakeJavaSdkHome;

  private LogStoringHandler testHandler;
  @Mock private CloudSdk sdk;
  @Mock private DevAppServerRunner devAppServerRunner;

  private DevServer devServer;

  private final Path java8Service = Paths.get("src/test/resources/projects/EmptyStandard8Project");
  private final Path java7Service = Paths.get("src/test/resources/projects/EmptyStandard7Project");

  private final Path java8Service1EnvVars =
      Paths.get("src/test/resources/projects/Standard8Project1EnvironmentVariables");
  private final Path java8Service2EnvVars =
      Paths.get("src/test/resources/projects/Standard8Project2EnvironmentVariables");

  // Environment variables included in running the dev server for Java 7/8 runtimes.
  private final Map<String, String> expectedJava7Environment =
      ImmutableMap.of("GAE_ENV", "localdev", "GAE_RUNTIME", "java7");
  private final Map<String, String> expectedJava8Environment =
      ImmutableMap.of("GAE_ENV", "localdev", "GAE_RUNTIME", "java8");

  @BeforeClass
  public static void disableIfJavaVersionAbove8() {
    assumeTrue(
        "DevServerTestJava8 requires Java 8", JAVA_SPECIFICATION_VERSION.value().equals("1.8"));
  }

  @Before
  public void setUp() throws IOException {
    devServer = Mockito.spy(new DevServer(sdk, devAppServerRunner));
    fakeJavaSdkHome = temporaryFolder.newFolder("java-sdk").toPath();

    Mockito.when(sdk.getAppEngineSdkForJavaPath()).thenReturn(fakeJavaSdkHome);

    testHandler = LogStoringHandler.getForLogger(DevServer.class.getName());
  }

  @Test
  public void testStop_allFlags() {
    StopConfiguration configuration =
        StopConfiguration.builder().host("alt-local-host").port(7777).build();
    try {
      devServer.stop(configuration);
      Assert.fail();
    } catch (AppEngineException ex) {
      Assert.assertEquals(
          "Error connecting to http://alt-local-host:7777/_ah/admin/quit", ex.getMessage());
    }
  }

  @Test
  public void testStop_defaultAdminHost() {
    StopConfiguration configuration = StopConfiguration.builder().port(7777).build();
    try {
      devServer.stop(configuration);
      Assert.fail();
    } catch (AppEngineException ex) {
      Assert.assertEquals(
          "Error connecting to http://localhost:7777/_ah/admin/quit", ex.getMessage());
    }
  }

  @Test
  public void testNullSdk() {
    try {
      new DevServer(null, devAppServerRunner);
      Assert.fail("Allowed null SDK");
    } catch (NullPointerException expected) {
    }

    try {
      new DevServer(sdk, null);
      Assert.fail("Allowed null runner");
    } catch (NullPointerException expected) {
    }
  }

  @Test
  public void testPrepareCommand_allFlags() throws Exception {

    RunConfiguration configuration =
        Mockito.spy(
            RunConfiguration.builder(ImmutableList.of(java8Service))
                .host("host")
                .port(8090)
                .jvmFlags(ImmutableList.of("-Dflag1", "-Dflag2"))
                .defaultGcsBucketName("buckets")
                .environment(null)
                .automaticRestart(true)
                .projectId("my-project")
                .environment(ImmutableMap.of("ENV_NAME", "ENV_VAL"))
                .additionalArguments(Arrays.asList("--ARG1", "--ARG2"))
                .build());

    SpyVerifier.newVerifier(configuration).verifyAllValuesNotNull();

    List<String> expectedFlags =
        ImmutableList.of(
            "--address=host",
            "--port=8090",
            "--default_gcs_bucket=buckets",
            "--application=my-project",
            "--allow_remote_shutdown",
            "--disable_update_check",
            "--ARG1",
            "--ARG2",
            "--no_java_agent",
            java8Service.toString());

    List<String> expectedJvmArgs =
        ImmutableList.of(
            "-Dappengine.fullscan.seconds=1",
            "-Dflag1",
            "-Dflag2",
            "-Duse_jetty9_runtime=true",
            "-D--enable_all_permissions=true");

    // Not us immutable map, it enforces order
    Map<String, String> expectedEnvironment =
        ImmutableMap.<String, String>builder()
            .putAll(expectedJava8Environment)
            .put("ENV_NAME", "ENV_VAL")
            .build();

    devServer.run(configuration);

    verify(devAppServerRunner, times(1))
        .run(
            expectedJvmArgs,
            expectedFlags,
            expectedEnvironment,
            java8Service /* workingDirectory */);

    SpyVerifier.newVerifier(configuration)
        .verifyDeclaredGetters(
            ImmutableMap.of("getServices", 7, "getJavaHomeDir", 2, "getJvmFlags", 2));
  }

  @Test
  public void testPrepareCommand_booleanFlags()
      throws AppEngineException, ProcessHandlerException, IOException {
    RunConfiguration configuration =
        RunConfiguration.builder(ImmutableList.of(java8Service)).build();

    List<String> expectedFlags =
        ImmutableList.of(
            "--allow_remote_shutdown",
            "--disable_update_check",
            "--no_java_agent",
            java8Service.toString());
    List<String> expectedJvmArgs =
        ImmutableList.of("-Duse_jetty9_runtime=true", "-D--enable_all_permissions=true");
    devServer.run(configuration);
    verify(devAppServerRunner, times(1))
        .run(
            expectedJvmArgs,
            expectedFlags,
            expectedJava8Environment,
            java8Service /* workingDirectory */);
  }

  @Test
  public void testPrepareCommand_noFlags()
      throws AppEngineException, ProcessHandlerException, IOException {

    RunConfiguration configuration =
        RunConfiguration.builder(ImmutableList.of(java8Service)).build();

    List<String> expectedFlags =
        ImmutableList.of(
            "--allow_remote_shutdown",
            "--disable_update_check",
            "--no_java_agent",
            java8Service.toString());

    List<String> expectedJvmArgs =
        ImmutableList.of("-Duse_jetty9_runtime=true", "-D--enable_all_permissions=true");

    devServer.run(configuration);

    verify(devAppServerRunner, times(1))
        .run(
            expectedJvmArgs,
            expectedFlags,
            expectedJava8Environment,
            java8Service /* workingDirectory */);
  }

  @Test
  public void testPrepareCommand_noFlagsJava7()
      throws AppEngineException, ProcessHandlerException, IOException {

    RunConfiguration configuration =
        RunConfiguration.builder(ImmutableList.of(java7Service)).build();

    List<String> expectedFlags =
        ImmutableList.of(
            "--allow_remote_shutdown", "--disable_update_check", java7Service.toString());
    List<String> expectedJvmArgs =
        ImmutableList.of(
            "-javaagent:"
                + fakeJavaSdkHome.resolve("agent/appengine-agent.jar").toAbsolutePath().toString());

    devServer.run(configuration);

    verify(devAppServerRunner, times(1))
        .run(
            expectedJvmArgs,
            expectedFlags,
            expectedJava7Environment,
            java7Service /* workingDirectory */);
  }

  @Test
  public void testPrepareCommand_noFlagsMultiModule()
      throws AppEngineException, ProcessHandlerException, IOException {

    RunConfiguration configuration =
        RunConfiguration.builder(ImmutableList.of(java7Service, java8Service)).build();

    List<String> expectedFlags =
        ImmutableList.of(
            "--allow_remote_shutdown",
            "--disable_update_check",
            "--no_java_agent",
            java7Service.toString(),
            java8Service.toString());

    List<String> expectedJvmArgs =
        ImmutableList.of("-Duse_jetty9_runtime=true", "-D--enable_all_permissions=true");

    devServer.run(configuration);

    verify(devAppServerRunner, times(1))
        .run(expectedJvmArgs, expectedFlags, expectedJava8Environment, null /* workingDirectory */);
  }

  @Test
  public void testPrepareCommand_appEngineWebXmlEnvironmentVariables()
      throws AppEngineException, ProcessHandlerException, IOException {
    RunConfiguration configuration =
        RunConfiguration.builder(ImmutableList.of(java8Service1EnvVars)).build();

    List<String> expectedFlags =
        ImmutableList.of(
            "--allow_remote_shutdown",
            "--disable_update_check",
            "--no_java_agent",
            java8Service1EnvVars.toString());

    List<String> expectedJvmArgs =
        ImmutableList.of("-Duse_jetty9_runtime=true", "-D--enable_all_permissions=true");

    Map<String, String> expectedConfigurationEnvironment =
        ImmutableMap.of("key1", "val1", "key2", "val2");
    Map<String, String> expectedEnvironment =
        ImmutableMap.<String, String>builder()
            .putAll(expectedConfigurationEnvironment)
            .putAll(expectedJava8Environment)
            .build();

    devServer.run(configuration);

    verify(devAppServerRunner, times(1))
        .run(
            expectedJvmArgs,
            expectedFlags,
            expectedEnvironment,
            java8Service1EnvVars /* workingDirectory */);
  }

  @Test
  public void testPrepareCommand_multipleServicesDuplicateAppEngineWebXmlEnvironmentVariables()
      throws AppEngineException, ProcessHandlerException, IOException {
    RunConfiguration configuration =
        RunConfiguration.builder(ImmutableList.of(java8Service1EnvVars, java8Service2EnvVars))
            .build();

    List<String> expectedFlags =
        ImmutableList.of(
            "--allow_remote_shutdown",
            "--disable_update_check",
            "--no_java_agent",
            java8Service1EnvVars.toString(),
            java8Service2EnvVars.toString());

    List<String> expectedJvmArgs =
        ImmutableList.of("-Duse_jetty9_runtime=true", "-D--enable_all_permissions=true");

    Map<String, String> expectedConfigurationEnvironment =
        ImmutableMap.of("key1", "val1", "keya", "vala", "key2", "duplicated-key", "keyc", "valc");
    Map<String, String> expectedEnvironment =
        ImmutableMap.<String, String>builder()
            .putAll(expectedConfigurationEnvironment)
            .putAll(expectedJava8Environment)
            .build();

    devServer.run(configuration);

    verify(devAppServerRunner, times(1))
        .run(expectedJvmArgs, expectedFlags, expectedEnvironment, null /* workingDirectory */);
  }

  @Test
  public void testPrepareCommand_clientSuppliedEnvironmentVariables()
      throws AppEngineException, ProcessHandlerException, IOException {
    Map<String, String> clientEnvironmentVariables =
        ImmutableMap.of("mykey1", "myval1", "mykey2", "myval2");

    RunConfiguration configuration =
        RunConfiguration.builder(ImmutableList.of(java7Service))
            .environment(clientEnvironmentVariables)
            .build();

    Map<String, String> expectedEnvironment =
        ImmutableMap.<String, String>builder()
            .putAll(expectedJava7Environment)
            .putAll(clientEnvironmentVariables)
            .build();
    List<String> expectedFlags =
        ImmutableList.of(
            "--allow_remote_shutdown", "--disable_update_check", java7Service.toString());
    List<String> expectedJvmArgs =
        ImmutableList.of(
            "-javaagent:"
                + fakeJavaSdkHome.resolve("agent/appengine-agent.jar").toAbsolutePath().toString());

    devServer.run(configuration);

    verify(devAppServerRunner, times(1))
        .run(
            expectedJvmArgs,
            expectedFlags,
            expectedEnvironment,
            java7Service /* workingDirectory */);
  }

  @Test
  public void testPrepareCommand_clientSuppliedAndAppEngineWebXmlEnvironmentVariables()
      throws AppEngineException, ProcessHandlerException, IOException {
    Map<String, String> clientEnvironmentVariables =
        ImmutableMap.of("mykey1", "myval1", "mykey2", "myval2");

    RunConfiguration configuration =
        RunConfiguration.builder(ImmutableList.of(java8Service1EnvVars))
            .environment(clientEnvironmentVariables)
            .build();

    List<String> expectedFlags =
        ImmutableList.of(
            "--allow_remote_shutdown",
            "--disable_update_check",
            "--no_java_agent",
            java8Service1EnvVars.toString());

    List<String> expectedJvmArgs =
        ImmutableList.of("-Duse_jetty9_runtime=true", "-D--enable_all_permissions=true");

    Map<String, String> appEngineEnvironment = ImmutableMap.of("key1", "val1", "key2", "val2");
    Map<String, String> expectedEnvironment =
        ImmutableMap.<String, String>builder()
            .putAll(appEngineEnvironment)
            .putAll(expectedJava8Environment)
            .putAll(clientEnvironmentVariables)
            .build();

    devServer.run(configuration);

    verify(devAppServerRunner, times(1))
        .run(
            expectedJvmArgs,
            expectedFlags,
            expectedEnvironment,
            java8Service1EnvVars /* workingDirectory */);
  }

  @Test
  public void testCheckAndWarnIgnored_withSetValue() {
    devServer.checkAndWarnIgnored(new Object(), "testName");

    Assert.assertEquals(1, testHandler.getLogs().size());

    LogRecord logRecord = testHandler.getLogs().get(0);
    Assert.assertEquals(
        "testName only applies to Dev Appserver v2 and will be ignored by Dev Appserver v1",
        logRecord.getMessage());
    Assert.assertEquals(Level.WARNING, logRecord.getLevel());
  }

  @Test
  public void testCheckAndWarnIgnored_withUnsetValue() {
    devServer.checkAndWarnIgnored(null, "testName");

    Assert.assertEquals(0, testHandler.getLogs().size());
  }

  @Test
  public void testDetermineJavaRuntime_noWarningsJava7() throws AppEngineException {
    Assert.assertTrue(devServer.isSandboxEnforced(ImmutableList.of(java7Service)));
    Assert.assertEquals(0, testHandler.getLogs().size());
  }

  @Test
  public void testDetermineJavaRuntime_noWarningsJava7Multiple() throws AppEngineException {
    Assert.assertTrue(devServer.isSandboxEnforced(ImmutableList.of(java7Service, java7Service)));
    Assert.assertEquals(0, testHandler.getLogs().size());
  }

  @Test
  public void testDetermineJavaRuntime_noWarningsJava8() throws AppEngineException {
    Assert.assertFalse(devServer.isSandboxEnforced(ImmutableList.of(java8Service)));
    Assert.assertEquals(0, testHandler.getLogs().size());
  }

  @Test
  public void testDetermineJavaRuntime_noWarningsJava8Multiple() throws AppEngineException {
    Assert.assertFalse(devServer.isSandboxEnforced(ImmutableList.of(java8Service, java8Service)));
    Assert.assertEquals(0, testHandler.getLogs().size());
  }

  @Test
  public void testDetermineJavaRuntime_mixedModeWarning() throws AppEngineException {

    Assert.assertFalse(devServer.isSandboxEnforced(ImmutableList.of(java8Service, java7Service)));
    Assert.assertEquals(1, testHandler.getLogs().size());

    LogRecord logRecord = testHandler.getLogs().get(0);
    Assert.assertEquals(
        "Mixed runtimes detected, will not enforce sandbox restrictions.", logRecord.getMessage());
    Assert.assertEquals(Level.WARNING, logRecord.getLevel());
  }

  @Test
  public void testWorkingDirectory_fallbackIfOneProject()
      throws ProcessHandlerException, AppEngineException, IOException {
    RunConfiguration configuration =
        RunConfiguration.builder(ImmutableList.of(java8Service)).build();

    devServer.run(configuration);

    verify(devAppServerRunner).run(any(), any(), any(), eq(java8Service) /* workingDirectory */);
  }

  @Test
  public void testWorkingDirectory_noFallbackIfManyProjects()
      throws ProcessHandlerException, AppEngineException, IOException {
    RunConfiguration configuration =
        RunConfiguration.builder(ImmutableList.of(java8Service, java8Service)).build();

    devServer.run(configuration);

    verify(devAppServerRunner).run(any(), any(), any(), eq(null) /* workingDirectory */);
  }

  @Test
  public void testGetLocalAppEngineEnvironmentVariables_java7() {
    Map<String, String> environment = DevServer.getLocalAppEngineEnvironmentVariables("java7");
    Assert.assertEquals(expectedJava7Environment, environment);
  }

  @Test
  public void testGetLocalAppEngineEnvironmentVariables_java8() {
    Map<String, String> environment = DevServer.getLocalAppEngineEnvironmentVariables("java8");
    Assert.assertEquals(expectedJava8Environment, environment);
  }

  @Test
  public void testGetLocalAppEngineEnvironmentVariables_other() {
    Map<String, String> environment =
        DevServer.getLocalAppEngineEnvironmentVariables("some_other_runtime");
    Map<String, String> expectedEnvironment =
        ImmutableMap.of("GAE_ENV", "localdev", "GAE_RUNTIME", "some_other_runtime");
    Assert.assertEquals(expectedEnvironment, environment);
  }

  @Test
  public void testGetGaeRuntimeJava_isJava8() {
    Assert.assertEquals("java8", DevServer.getGaeRuntimeJava(true));
  }

  @Test
  public void testGetGaeRuntimeJava_isNotJava8() {
    Assert.assertEquals("java7", DevServer.getGaeRuntimeJava(false));
  }
}
