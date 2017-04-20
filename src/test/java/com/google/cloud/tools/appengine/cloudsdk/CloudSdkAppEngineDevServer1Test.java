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
import com.google.cloud.tools.appengine.api.devserver.DefaultStopConfiguration;
import com.google.cloud.tools.appengine.cloudsdk.internal.process.ProcessRunnerException;
import com.google.cloud.tools.test.utils.LogStoringHandler;
import com.google.cloud.tools.test.utils.SpyVerifier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link CloudSdkAppEngineDevServer}.
 */
@RunWith(MockitoJUnitRunner.class)
public class CloudSdkAppEngineDevServer1Test {

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  private Path fakeJavaSdkHome;
  private File fakeStoragePath;
  private File fakeDatastorePath;

  private LogStoringHandler testHandler;
  @Mock
  private CloudSdk sdk;

  private CloudSdkAppEngineDevServer1 devServer;

  private final Path pathToJava8Service = Paths.get("src/test/resources/projects/EmptyStandard8Project");
  private final File java8Service = pathToJava8Service.toFile();
  private final Path pathToJava7Service = Paths.get("src/test/resources/projects/EmptyStandard7Project");
  private final File java7Service = pathToJava7Service.toFile();
  private final Path pathToJava8Service1WithEnvVars = Paths.get("src/test/resources/projects/Standard8Project1EnvironmentVariables");
  private final File java8Service1EnvVars = pathToJava8Service1WithEnvVars.toFile();
  private final Path pathToJava8Service2WithEnvVars = Paths.get("src/test/resources/projects/Standard8Project2EnvironmentVariables");
  private final File java8Service2EnvVars = pathToJava8Service2WithEnvVars.toFile();
  private final Map<String, String> environment = Maps.newHashMap();


  @Before
  public void setUp() throws IOException {
    devServer = Mockito.spy(new CloudSdkAppEngineDevServer1(sdk));
    fakeJavaSdkHome = temporaryFolder.newFolder("java-sdk").toPath();
    fakeStoragePath = new File("storage/path");
    fakeDatastorePath = temporaryFolder.newFile("datastore.db");

    Mockito.when(sdk.getJavaAppEngineSdkPath()).thenReturn(fakeJavaSdkHome);

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
      Assert.assertEquals(ex.getMessage(), "Error connecting to http://localhost:7777/_ah/admin/quit");
    }
  }

  @Test
  public void testNullSdk() {
    try {
      new CloudSdkAppEngineDevServer1(null);
      Assert.fail("Allowed null SDK");
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

    // these params are not used by devappserver1 and will log warnings
    configuration.setAdminHost("adminHost");
    configuration.setAdminPort(8000);
    configuration.setAuthDomain("example.com");
    configuration.setAllowSkippedFiles(true);
    configuration.setApiPort(8091);
    configuration.setAutomaticRestart(false);
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

    SpyVerifier.newVerifier(configuration).verifyDeclaredSetters();

    List<String> expectedFlags = ImmutableList
        .of("--address=host", "--port=8090", "--default_gcs_bucket=buckets",
            "--allow_remote_shutdown", "--disable_update_check", "--no_java_agent",
            pathToJava8Service.toString());

    List<String> expectedJvmArgs = ImmutableList.of("-Dflag1", "-Dflag2",
        "-Duse_jetty9_runtime=true", "-D--enable_all_permissions=true");

    devServer.run(configuration);

    verify(sdk, times(1)).runDevAppServer1Command(expectedJvmArgs, expectedFlags, environment);

    SpyVerifier.newVerifier(configuration)
        .verifyDeclaredGetters(ImmutableMap.of("getServices", 5, "getJavaHomeDir", 2, "getJvmFlags", 2));

    // verify we are checking and ignoring these parameters
    Map<String, Object> paramWarnings = new HashMap<>();
    paramWarnings.put("adminHost", configuration.getAdminHost());
    paramWarnings.put("adminPort", configuration.getAdminPort());
    paramWarnings.put("allowSkippedFiles", configuration.getAllowSkippedFiles());
    paramWarnings.put("apiPort", configuration.getApiPort());
    paramWarnings.put("authDomain", configuration.getAuthDomain());
    paramWarnings.put("automaticRestart", configuration.getAutomaticRestart());
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
    verify(devServer, times(paramWarnings.size()))
        .checkAndWarnIgnored(Mockito.any(), Mockito.anyString());
  }

  @Test
  public void testPrepareCommand_booleanFlags() throws AppEngineException, ProcessRunnerException {
    DefaultRunConfiguration configuration = new DefaultRunConfiguration();

    configuration.setServices(ImmutableList.of(java8Service));

    List<String> expectedFlags = ImmutableList.of("--allow_remote_shutdown",
        "--disable_update_check", "--no_java_agent", pathToJava8Service.toString());
    List<String> expectedJvmArgs = ImmutableList.of("-Duse_jetty9_runtime=true",
            "-D--enable_all_permissions=true");
    devServer.run(configuration);
    verify(sdk, times(1)).runDevAppServer1Command(expectedJvmArgs, expectedFlags, environment);
  }

  @Test
  public void testPrepareCommand_noFlags() throws AppEngineException, ProcessRunnerException {

    DefaultRunConfiguration configuration = new DefaultRunConfiguration();
    configuration.setServices(ImmutableList.of(java8Service));

    List<String> expectedFlags = ImmutableList.of("--allow_remote_shutdown",
        "--disable_update_check", "--no_java_agent", pathToJava8Service.toString());

    List<String> expectedJvmArgs = ImmutableList.of("-Duse_jetty9_runtime=true",
            "-D--enable_all_permissions=true");

    devServer.run(configuration);

    verify(sdk, times(1)).runDevAppServer1Command(expectedJvmArgs, expectedFlags, environment);
  }

  @Test
  public void testPrepareCommand_noFlagsJava7() throws AppEngineException, ProcessRunnerException {

    DefaultRunConfiguration configuration = new DefaultRunConfiguration();
    configuration.setServices(ImmutableList.of(java7Service));

    List<String> expectedFlags = ImmutableList.of("--allow_remote_shutdown",
        "--disable_update_check", pathToJava7Service.toString());
    List<String> expectedJvmArgs = ImmutableList
        .of("-javaagent:" + fakeJavaSdkHome.resolve("agent/appengine-agent.jar").toAbsolutePath()
            .toString());

    devServer.run(configuration);

    verify(sdk, times(1)).runDevAppServer1Command(expectedJvmArgs, expectedFlags, environment);
  }

  @Test
  public void testPrepareCommand_noFlagsMultiModule() throws AppEngineException, ProcessRunnerException {

    DefaultRunConfiguration configuration = new DefaultRunConfiguration();
    configuration.setServices(ImmutableList.of(java7Service, java8Service));

    List<String> expectedFlags = ImmutableList.of("--allow_remote_shutdown",
        "--disable_update_check", "--no_java_agent", pathToJava7Service.toString(),
        pathToJava8Service.toString());

    List<String> expectedJvmArgs = ImmutableList.of("-Duse_jetty9_runtime=true",
        "-D--enable_all_permissions=true");

    devServer.run(configuration);

    verify(sdk, times(1)).runDevAppServer1Command(expectedJvmArgs, expectedFlags, environment);
  }

  @Test
  public void testPrepareCommand_environmentVariables() throws AppEngineException, ProcessRunnerException {
    DefaultRunConfiguration configuration = new DefaultRunConfiguration();
    configuration.setServices(ImmutableList.of(java8Service1EnvVars));

    List<String> expectedFlags = ImmutableList.of("--allow_remote_shutdown",
        "--disable_update_check", "--no_java_agent", pathToJava8Service1WithEnvVars.toString());

    List<String> expectedJvmArgs = ImmutableList.of("-Duse_jetty9_runtime=true",
            "-D--enable_all_permissions=true");

    Map<String, String> expectedEnvironment = ImmutableMap.of("key1", "val1", "key2", "val2");

    devServer.run(configuration);

    verify(sdk, times(1)).runDevAppServer1Command(expectedJvmArgs, expectedFlags, expectedEnvironment);
  }

  @Test
  public void testPrepareCommand_multipleServicesDuplicateEnvironmentVariables() throws AppEngineException, ProcessRunnerException {
    DefaultRunConfiguration configuration = new DefaultRunConfiguration();
    configuration.setServices(ImmutableList.of(java8Service1EnvVars, java8Service2EnvVars));

    List<String> expectedFlags = ImmutableList.of("--allow_remote_shutdown",
        "--disable_update_check", "--no_java_agent", pathToJava8Service1WithEnvVars.toString(),
        pathToJava8Service2WithEnvVars.toString());

    List<String> expectedJvmArgs = ImmutableList.of("-Duse_jetty9_runtime=true",
            "-D--enable_all_permissions=true");

    Map<String, String> expectedEnvironment = ImmutableMap.of(
        "key1", "val1", "keya", "vala", "key2", "duplicated-key", "keyc", "valc");

    devServer.run(configuration);

    verify(sdk, times(1)).runDevAppServer1Command(expectedJvmArgs, expectedFlags, expectedEnvironment);
  }

  @Test
  public void testCheckAndWarnIgnored_withSetValue() {
    devServer.checkAndWarnIgnored(new Object(), "testName");

    Assert.assertEquals(1, testHandler.getLogs().size());

    LogRecord logRecord = testHandler.getLogs().get(0);
    Assert.assertEquals("testName only applies to Dev Appserver v2 and will be ignored by Dev Appserver v1", logRecord.getMessage());
    Assert.assertEquals(Level.WARNING, logRecord.getLevel());
  }

  @Test
  public void testCheckAndWarnIgnored_withUnsetValue() {
    devServer.checkAndWarnIgnored(null, "testName");

    Assert.assertEquals(0, testHandler.getLogs().size());
  }

  @Test
  public void testDetermineJavaRuntime_noWarningsJava7() {
    Assert.assertFalse(devServer.isJava8(ImmutableList.of(java7Service)));
    Assert.assertEquals(0, testHandler.getLogs().size());
  }

  @Test
  public void testDetermineJavaRuntime_noWarningsJava7Multiple() {
    Assert.assertFalse(devServer.isJava8(ImmutableList.of(java7Service, java7Service)));
    Assert.assertEquals(0, testHandler.getLogs().size());
  }

  @Test
  public void testDetermineJavaRuntime_noWarningsJava8() {
    Assert.assertTrue(devServer.isJava8(ImmutableList.of(java8Service)));
    Assert.assertEquals(0, testHandler.getLogs().size());
  }

  @Test
  public void testDetermineJavaRuntime_noWarningsJava8Multiple() {
    Assert.assertTrue(devServer.isJava8(ImmutableList.of(java8Service, java8Service)));
    Assert.assertEquals(0, testHandler.getLogs().size());
  }

  @Test
  public void testDetermineJavaRuntime_mixedModeWarning() {

    Assert.assertTrue(devServer.isJava8(ImmutableList.of(java8Service, java7Service)));
    Assert.assertEquals(1, testHandler.getLogs().size());

    LogRecord logRecord = testHandler.getLogs().get(0);
    Assert.assertEquals("Mixed runtimes java7/java8 detected, will use java8 settings", logRecord.getMessage());
    Assert.assertEquals(Level.WARNING, logRecord.getLevel());
  }
}
