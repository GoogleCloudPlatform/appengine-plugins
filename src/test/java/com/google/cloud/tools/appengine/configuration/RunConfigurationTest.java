/*
 * Copyright 2019 Google LLC.
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

package com.google.cloud.tools.appengine.configuration;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RunConfigurationTest {

  private RunConfiguration configuration;
  private List<String> inputFlags = new ArrayList<>();
  private List<Path> services = new ArrayList<>();

  @Before
  public void setUp() {
    inputFlags.add("foo");
    inputFlags.add("bar");
    List<String> additionalArguments = new ArrayList<>();
    Map<String, String> environment = new HashMap<>();
    configuration =
        RunConfiguration.builder(services)
            .additionalArguments(additionalArguments)
            .adminHost("adminHost")
            .adminPort(87)
            .allowSkippedFiles(true)
            .apiPort(88)
            .authDomain("authDomain")
            .automaticRestart(true)
            .clearDatastore(true)
            .customEntrypoint("customEntrypoint")
            .datastorePath(Paths.get(File.separator + "datastorepath"))
            .defaultGcsBucketName("defaultGcsBucketName")
            .devAppserverLogLevel("devAppserverLogLevel")
            .environment(environment)
            .host("host")
            .jvmFlags(inputFlags)
            .logLevel("logLevel")
            .maxModuleInstances(54)
            .port(999)
            .projectId("projectId")
            .pythonStartupArgs("pythonStartupArgs")
            .pythonStartupScript("pythonStartupScript")
            .runtime("runtime")
            .skipSdkUpdateCheck(true)
            .storagePath(Paths.get(File.separator + "storagePath"))
            .threadsafeOverride("threadsafeOverride")
            .useMtimeFileWatcher(true)
            .build();
  }

  @Test
  public void testJvmFlags() {
    inputFlags.add("baz");

    List<String> flags = configuration.getJvmFlags();
    Assert.assertEquals(2, flags.size());
    Assert.assertEquals("foo", flags.get(0));

    flags.set(0, "baz");
    Assert.assertEquals("foo", configuration.getJvmFlags().get(0));
  }

  @Test
  public void testJvmFlags_unset() {
    List<String> flags = RunConfiguration.builder(services).build().getJvmFlags();
    Assert.assertEquals(0, flags.size());
  }

  @Test
  public void testBuilder() {
    Assert.assertEquals(87, configuration.getAdminPort().intValue());
    Assert.assertEquals(88, configuration.getApiPort().intValue());
    Assert.assertEquals(999, configuration.getPort().intValue());
    Assert.assertEquals(54, configuration.getMaxModuleInstances().intValue());

    Assert.assertEquals("adminHost", configuration.getAdminHost());
    Assert.assertEquals("authDomain", configuration.getAuthDomain());
    Assert.assertEquals("customEntrypoint", configuration.getCustomEntrypoint());
    Assert.assertEquals("host", configuration.getHost());
    Assert.assertEquals("logLevel", configuration.getLogLevel());
    Assert.assertEquals("devAppserverLogLevel", configuration.getDevAppserverLogLevel());
    Assert.assertEquals("defaultGcsBucketName", configuration.getDefaultGcsBucketName());
    Assert.assertEquals("runtime", configuration.getRuntime());
    Assert.assertEquals("pythonStartupArgs", configuration.getPythonStartupArgs());
    Assert.assertEquals("pythonStartupScript", configuration.getPythonStartupScript());
    Assert.assertEquals("projectId", configuration.getProjectId());

    Assert.assertEquals(File.separator + "storagePath", configuration.getStoragePath().toString());
    Assert.assertEquals(File.separator + "datastorepath",
        configuration.getDatastorePath().toString());

    Assert.assertTrue(configuration.getUseMtimeFileWatcher());
    Assert.assertTrue(configuration.getSkipSdkUpdateCheck());
    Assert.assertTrue(configuration.getClearDatastore());
    Assert.assertTrue(configuration.getAutomaticRestart());
    Assert.assertTrue(configuration.getAllowSkippedFiles());
  }

  @Test
  public void testToBuilder() {

    RunConfiguration.Builder builder = configuration.toBuilder();
    configuration = builder.build();

    Assert.assertEquals(87, configuration.getAdminPort().intValue());
    Assert.assertEquals(88, configuration.getApiPort().intValue());
    Assert.assertEquals(999, configuration.getPort().intValue());
    Assert.assertEquals(54, configuration.getMaxModuleInstances().intValue());

    Assert.assertEquals("adminHost", configuration.getAdminHost());
    Assert.assertEquals("authDomain", configuration.getAuthDomain());
    Assert.assertEquals("customEntrypoint", configuration.getCustomEntrypoint());
    Assert.assertEquals("host", configuration.getHost());
    Assert.assertEquals("logLevel", configuration.getLogLevel());
    Assert.assertEquals("devAppserverLogLevel", configuration.getDevAppserverLogLevel());
    Assert.assertEquals("defaultGcsBucketName", configuration.getDefaultGcsBucketName());
    Assert.assertEquals("runtime", configuration.getRuntime());
    Assert.assertEquals("pythonStartupArgs", configuration.getPythonStartupArgs());
    Assert.assertEquals("pythonStartupScript", configuration.getPythonStartupScript());
    Assert.assertEquals("projectId", configuration.getProjectId());

    Assert.assertEquals(File.separator + "storagePath", configuration.getStoragePath().toString());
    Assert.assertEquals(File.separator + "datastorepath",
        configuration.getDatastorePath().toString());

    Assert.assertTrue(configuration.getUseMtimeFileWatcher());
    Assert.assertTrue(configuration.getSkipSdkUpdateCheck());
    Assert.assertTrue(configuration.getClearDatastore());
    Assert.assertTrue(configuration.getAutomaticRestart());
    Assert.assertTrue(configuration.getAllowSkippedFiles());
  }
}
