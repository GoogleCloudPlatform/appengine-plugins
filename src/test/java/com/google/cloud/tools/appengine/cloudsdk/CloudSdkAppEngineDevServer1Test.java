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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.cloud.tools.appengine.api.AppEngineException;
import com.google.cloud.tools.appengine.api.devserver.DefaultRunConfiguration;
import com.google.cloud.tools.appengine.api.devserver.DefaultStopConfiguration;
import com.google.cloud.tools.appengine.cloudsdk.process.ProcessHandlerException;
import com.google.cloud.tools.test.utils.LogStoringHandler;
import com.google.cloud.tools.test.utils.SpyVerifier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/** Unit tests for {@link CloudSdkAppEngineDevServer1}. */
@RunWith(MockitoJUnitRunner.class)
public class CloudSdkAppEngineDevServer1Test {

  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();
  private Path fakeJavaSdkHome;
  private File fakeStoragePath;
  private File fakeDatastorePath;

  private LogStoringHandler testHandler;
  @Mock private CloudSdk sdk;
  @Mock private DevAppServerRunner devAppServerRunner;

  private CloudSdkAppEngineDevServer1 devServer;

  private final Path pathToJava8Service =
      Paths.get("src/test/resources/projects/EmptyStandard8Project");
  private final File java8Service = pathToJava8Service.toFile();
  private final Path pathToJava7Service =
      Paths.get("src/test/resources/projects/EmptyStandard7Project");
  private final File java7Service = pathToJava7Service.toFile();

  private final Path pathToJava8Service1WithEnvVars =
      Paths.get("src/test/resources/projects/Standard8Project1EnvironmentVariables");
  private final File java8Service1EnvVars = pathToJava8Service1WithEnvVars.toFile();
  private final Path pathToJava8Service2WithEnvVars =
      Paths.get("src/test/resources/projects/Standard8Project2EnvironmentVariables");
  private final File java8Service2EnvVars = pathToJava8Service2WithEnvVars.toFile();

  // Environment variables included in running the dev server for Java 7/8 runtimes.
  private final Map<String, String> expectedJava7Environment =
      ImmutableMap.of("GAE_ENV", "localdev", "GAE_RUNTIME", "java7");
  private final Map<String, String> expectedJava8Environment =
      ImmutableMap.of("GAE_ENV", "localdev", "GAE_RUNTIME", "java8");

  @Before
  public void setUp() throws IOException {
    devServer = Mockito.spy(new CloudSdkAppEngineDevServer1(sdk, devAppServerRunner));
    fakeJavaSdkHome = temporaryFolder.newFolder("java-sdk").toPath();
    fakeStoragePath = new File("storage/path");
    fakeDatastorePath = temporaryFolder.newFile("datastore.db");

    Mockito.when(sdk.getAppEngineSdkForJavaPath()).thenReturn(fakeJavaSdkHome);

    testHandler = LogStoringHandler.getForLogger(CloudSdkAppEngineDevServer1.class.getName());
  }

  @Test
  public void testStop() {
    DefaultStopConfiguration configuration = new DefaultStopConfiguration();
    configuration.setAdminPort(7777);
    try {
      devServer.stop(configuration);
      Assert.fail();
    } catch (AppEngineException ex) {
      Assert.assertEquals(
          ex.getMessage(), "Error connecting to http://localhost:7777/_ah/admin/quit");
    }
  }

  @Test
  public void testNullSdk() {
    try {
      new CloudSdkAppEngineDevServer1(null, devAppServerRunner);
      Assert.fail("Allowed null SDK");
    } catch (NullPointerException expected) {
    }

    try {
      new CloudSdkAppEngineDevServer1(sdk, null);
      Assert.fail("Allowed null runner");
    } catch (NullPointerException expected) {
    }
  }

  @Test
  public void testPrepareCommand_allFlags() throws Exception {

    DefaultRunConfiguration configuration = Mockito.spy(new DefaultRunConfiguration());

    configuration.setServices(ImmutableList.of(java8Service));
    configuration.setHost("host");
    configuration.setPort(8090);
    configuration.setJvmFlags(ImmutableList.of("-Dflag1", "-Dflag2"));
    configuration.setDefaultGcsBucketName("buckets");
    configuration.setEnvironment(null);
    configuration.setAutomaticRestart(true);
    configuration.setProjectId("my-project");

    // these params are not used by devappserver1 and will log warnings
    configuration.setAdminHost("adminHost");
    configuration.setAdminPort(8000);
    configuration.setAuthDomain("example.com");
    configuration.setAllowSkippedFiles(true);
    configuration.setApiPort(8091);
    configuration.setClearDatastore(true);
    configuration.setCustomEntrypoint("entrypoint");
    configuration.setDatastorePath(fakeDatastorePath);
    configuration.setDevAppserverLogLevel("info");
    configuration.setLogLevel("debug");
    configuration.setMaxModuleInstances(3);
    configuration.setPythonStartupScript("script.py");
    configuration.setPythonStartupArgs("arguments");
    configuration.setRuntime("someRuntime");
    configuration.setStoragePath(fakeStoragePath);
    configuration.setSkipSdkUpdateCheck(true);
    configuration.setThreadsafeOverride("default:False,backend:True");
    configuration.setUseMtimeFileWatcher(true);
    configuration.setAdditionalArguments(Arrays.asList("--ARG1", "--ARG2"));

    SpyVerifier.newVerifier(configuration).verifyDeclaredSetters();

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
            pathToJava8Service.toString());

    List<String> expectedJvmArgs =
        ImmutableList.of(
            "-Dappengine.fullscan.seconds=1",
            "-Dflag1",
            "-Dflag2",
            "-Duse_jetty9_runtime=true",
            "-D--enable_all_permissions=true");

    devServer.run(configuration);

    verify(devAppServerRunner, times(1))
        .runV1(
            expectedJvmArgs,
            expectedFlags,
            expectedJava8Environment,
            java8Service /* workingDirectory */);

    SpyVerifier.newVerifier(configuration)
        .verifyDeclaredGetters(
            ImmutableMap.of("getServices", 7, "getJavaHomeDir", 2, "getJvmFlags", 2));

    // verify we are checking and ignoring these parameters
    Map<String, Object> paramWarnings = new HashMap<>();
    paramWarnings.put("adminHost", configuration.getAdminHost());
    paramWarnings.put("adminPort", configuration.getAdminPort());
    paramWarnings.put("allowSkippedFiles", configuration.getAllowSkippedFiles());
    paramWarnings.put("apiPort", configuration.getApiPort());
    paramWarnings.put("authDomain", configuration.getAuthDomain());
    paramWarnings.put("clearDatastore", configuration.getClearDatastore());
    paramWarnings.put("customEntrypoint", configuration.getCustomEntrypoint());
    paramWarnings.put("datastorePath", configuration.getDatastorePath());
    paramWarnings.put("devAppserverLogLevel", configuration.getDevAppserverLogLevel());
    paramWarnings.put("logLevel", configuration.getLogLevel());
    paramWarnings.put("maxModuleInstances", configuration.getMaxModuleInstances());
    paramWarnings.put("pythonStartupArgs", configuration.getPythonStartupArgs());
    paramWarnings.put("pythonStartupScript", configuration.getPythonStartupScript());
    paramWarnings.put("runtime", configuration.getRuntime());
    paramWarnings.put("skipSdkUpdateCheck", configuration.getSkipSdkUpdateCheck());
    paramWarnings.put("storagePath", configuration.getStoragePath());
    paramWarnings.put("threadsafeOverride", configuration.getThreadsafeOverride());
    paramWarnings.put("useMtimeFileWatcher", configuration.getUseMtimeFileWatcher());

    for (String key : paramWarnings.keySet()) {
      verify(devServer).checkAndWarnIgnored(paramWarnings.get(key), key);
    }

    // verify that we're verifying all the ignored parameters (by counting)
    verify(devServer, times(paramWarnings.size())).checkAndWarnIgnored(any(), Mockito.anyString());
  }

  @Test
  public void testPrepareCommand_booleanFlags()
      throws AppEngineException, ProcessHandlerException, IOException {
    DefaultRunConfiguration configuration = new DefaultRunConfiguration();

    configuration.setServices(ImmutableList.of(java8Service));

    List<String> expectedFlags =
        ImmutableList.of(
            "--allow_remote_shutdown",
            "--disable_update_check",
            "--no_java_agent",
            pathToJava8Service.toString());
    List<String> expectedJvmArgs =
        ImmutableList.of("-Duse_jetty9_runtime=true", "-D--enable_all_permissions=true");
    devServer.run(configuration);
    verify(devAppServerRunner, times(1))
        .runV1(
            expectedJvmArgs,
            expectedFlags,
            expectedJava8Environment,
            java8Service /* workingDirectory */);
  }

  @Test
  public void testPrepareCommand_noFlags()
      throws AppEngineException, ProcessHandlerException, IOException {

    DefaultRunConfiguration configuration = new DefaultRunConfiguration();
    configuration.setServices(ImmutableList.of(java8Service));

    List<String> expectedFlags =
        ImmutableList.of(
            "--allow_remote_shutdown",
            "--disable_update_check",
            "--no_java_agent",
            pathToJava8Service.toString());

    List<String> expectedJvmArgs =
        ImmutableList.of("-Duse_jetty9_runtime=true", "-D--enable_all_permissions=true");

    devServer.run(configuration);

    verify(devAppServerRunner, times(1))
        .runV1(
            expectedJvmArgs,
            expectedFlags,
            expectedJava8Environment,
            java8Service /* workingDirectory */);
  }

  @Test
  public void testPrepareCommand_noFlagsJava7()
      throws AppEngineException, ProcessHandlerException, IOException {

    DefaultRunConfiguration configuration = new DefaultRunConfiguration();
    configuration.setServices(ImmutableList.of(java7Service));

    List<String> expectedFlags =
        ImmutableList.of(
            "--allow_remote_shutdown", "--disable_update_check", pathToJava7Service.toString());
    List<String> expectedJvmArgs =
        ImmutableList.of(
            "-javaagent:"
                + fakeJavaSdkHome.resolve("agent/appengine-agent.jar").toAbsolutePath().toString());

    devServer.run(configuration);

    verify(devAppServerRunner, times(1))
        .runV1(
            expectedJvmArgs,
            expectedFlags,
            expectedJava7Environment,
            java7Service /* workingDirectory */);
  }

  @Test
  public void testPrepareCommand_noFlagsMultiModule()
      throws AppEngineException, ProcessHandlerException, IOException {

    DefaultRunConfiguration configuration = new DefaultRunConfiguration();
    configuration.setServices(ImmutableList.of(java7Service, java8Service));

    List<String> expectedFlags =
        ImmutableList.of(
            "--allow_remote_shutdown",
            "--disable_update_check",
            "--no_java_agent",
            pathToJava7Service.toString(),
            pathToJava8Service.toString());

    List<String> expectedJvmArgs =
        ImmutableList.of("-Duse_jetty9_runtime=true", "-D--enable_all_permissions=true");

    devServer.run(configuration);

    verify(devAppServerRunner, times(1))
        .runV1(
            expectedJvmArgs, expectedFlags, expectedJava8Environment, null /* workingDirectory */);
  }

  @Test
  public void testPrepareCommand_appEngineWebXmlEnvironmentVariables()
      throws AppEngineException, ProcessHandlerException, IOException {
    DefaultRunConfiguration configuration = new DefaultRunConfiguration();
    configuration.setServices(ImmutableList.of(java8Service1EnvVars));

    List<String> expectedFlags =
        ImmutableList.of(
            "--allow_remote_shutdown",
            "--disable_update_check",
            "--no_java_agent",
            pathToJava8Service1WithEnvVars.toString());

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
        .runV1(
            expectedJvmArgs,
            expectedFlags,
            expectedEnvironment,
            java8Service1EnvVars /* workingDirectory */);
  }

  @Test
  public void testPrepareCommand_multipleServicesDuplicateAppEngineWebXmlEnvironmentVariables()
      throws AppEngineException, ProcessHandlerException, IOException {
    DefaultRunConfiguration configuration = new DefaultRunConfiguration();
    configuration.setServices(ImmutableList.of(java8Service1EnvVars, java8Service2EnvVars));

    List<String> expectedFlags =
        ImmutableList.of(
            "--allow_remote_shutdown",
            "--disable_update_check",
            "--no_java_agent",
            pathToJava8Service1WithEnvVars.toString(),
            pathToJava8Service2WithEnvVars.toString());

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
        .runV1(expectedJvmArgs, expectedFlags, expectedEnvironment, null /* workingDirectory */);
  }

  @Test
  public void testPrepareCommand_clientSuppliedEnvironmentVariables()
      throws AppEngineException, ProcessHandlerException, IOException {
    DefaultRunConfiguration configuration = new DefaultRunConfiguration();
    configuration.setServices(ImmutableList.of(java7Service));

    Map<String, String> clientEnvironmentVariables =
        ImmutableMap.of("mykey1", "myval1", "mykey2", "myval2");
    configuration.setEnvironment(clientEnvironmentVariables);

    Map<String, String> expectedEnvironment =
        ImmutableMap.<String, String>builder()
            .putAll(expectedJava7Environment)
            .putAll(clientEnvironmentVariables)
            .build();
    List<String> expectedFlags =
        ImmutableList.of(
            "--allow_remote_shutdown", "--disable_update_check", pathToJava7Service.toString());
    List<String> expectedJvmArgs =
        ImmutableList.of(
            "-javaagent:"
                + fakeJavaSdkHome.resolve("agent/appengine-agent.jar").toAbsolutePath().toString());

    devServer.run(configuration);

    verify(devAppServerRunner, times(1))
        .runV1(
            expectedJvmArgs,
            expectedFlags,
            expectedEnvironment,
            java7Service /* workingDirectory */);
  }

  @Test
  public void testPrepareCommand_clientSuppliedAndAppEngineWebXmlEnvironmentVariables()
      throws AppEngineException, ProcessHandlerException, IOException {
    DefaultRunConfiguration configuration = new DefaultRunConfiguration();
    configuration.setServices(ImmutableList.of(java8Service1EnvVars));

    Map<String, String> clientEnvironmentVariables =
        ImmutableMap.of("mykey1", "myval1", "mykey2", "myval2");
    configuration.setEnvironment(clientEnvironmentVariables);

    List<String> expectedFlags =
        ImmutableList.of(
            "--allow_remote_shutdown",
            "--disable_update_check",
            "--no_java_agent",
            pathToJava8Service1WithEnvVars.toString());

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
        .runV1(
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
    Assert.assertFalse(devServer.isJava8(ImmutableList.of(java7Service)));
    Assert.assertEquals(0, testHandler.getLogs().size());
  }

  @Test
  public void testDetermineJavaRuntime_noWarningsJava7Multiple() throws AppEngineException {
    Assert.assertFalse(devServer.isJava8(ImmutableList.of(java7Service, java7Service)));
    Assert.assertEquals(0, testHandler.getLogs().size());
  }

  @Test
  public void testDetermineJavaRuntime_noWarningsJava8() throws AppEngineException {
    Assert.assertTrue(devServer.isJava8(ImmutableList.of(java8Service)));
    Assert.assertEquals(0, testHandler.getLogs().size());
  }

  @Test
  public void testDetermineJavaRuntime_noWarningsJava8Multiple() throws AppEngineException {
    Assert.assertTrue(devServer.isJava8(ImmutableList.of(java8Service, java8Service)));
    Assert.assertEquals(0, testHandler.getLogs().size());
  }

  @Test
  public void testDetermineJavaRuntime_mixedModeWarning() throws AppEngineException {

    Assert.assertTrue(devServer.isJava8(ImmutableList.of(java8Service, java7Service)));
    Assert.assertEquals(1, testHandler.getLogs().size());

    LogRecord logRecord = testHandler.getLogs().get(0);
    Assert.assertEquals(
        "Mixed runtimes java7/java8 detected, will use java8 settings", logRecord.getMessage());
    Assert.assertEquals(Level.WARNING, logRecord.getLevel());
  }

  @Test
  public void testWorkingDirectory_fallbackIfOneProject()
      throws ProcessHandlerException, AppEngineException, IOException {
    DefaultRunConfiguration configuration = new DefaultRunConfiguration();
    configuration.setServices(ImmutableList.of(java8Service));

    devServer.run(configuration);

    verify(devAppServerRunner).runV1(any(), any(), any(), eq(java8Service) /* workingDirectory */);
  }

  @Test
  public void testWorkingDirectory_noFallbackIfManyProjects()
      throws ProcessHandlerException, AppEngineException, IOException {
    DefaultRunConfiguration configuration = new DefaultRunConfiguration();
    configuration.setServices(ImmutableList.of(java8Service, java8Service));

    devServer.run(configuration);

    verify(devAppServerRunner).runV1(any(), any(), any(), eq(null) /* workingDirectory */);
  }

  @Test
  public void testGetLocalAppEngineEnvironmentVariables_java7() {
    Map<String, String> environment =
        CloudSdkAppEngineDevServer1.getLocalAppEngineEnvironmentVariables("java7");
    Assert.assertEquals(expectedJava7Environment, environment);
  }

  @Test
  public void testGetLocalAppEngineEnvironmentVariables_java8() {
    Map<String, String> environment =
        CloudSdkAppEngineDevServer1.getLocalAppEngineEnvironmentVariables("java8");
    Assert.assertEquals(expectedJava8Environment, environment);
  }

  @Test
  public void testGetLocalAppEngineEnvironmentVariables_other() {
    Map<String, String> environment =
        CloudSdkAppEngineDevServer1.getLocalAppEngineEnvironmentVariables("some_other_runtime");
    Map<String, String> expectedEnvironment =
        ImmutableMap.of("GAE_ENV", "localdev", "GAE_RUNTIME", "some_other_runtime");
    Assert.assertEquals(expectedEnvironment, environment);
  }

  @Test
  public void testGetGaeRuntimeJava_isJava8() {
    Assert.assertEquals("java8", CloudSdkAppEngineDevServer1.getGaeRuntimeJava(true));
  }

  @Test
  public void testGetGaeRuntimeJava_isNotJava8() {
    Assert.assertEquals("java7", CloudSdkAppEngineDevServer1.getGaeRuntimeJava(false));
  }
}
