/*
 * Copyright 2017 Google Inc.
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

package com.google.cloud.tools.managedcloudsdk;

import com.google.cloud.tools.managedcloudsdk.command.CommandExecutionException;
import com.google.cloud.tools.managedcloudsdk.command.CommandExitException;
import com.google.cloud.tools.managedcloudsdk.command.CommandRunner;
import com.google.cloud.tools.managedcloudsdk.components.SdkComponent;
import com.google.cloud.tools.managedcloudsdk.components.WindowsBundledPythonCopierTestHelper;
import com.google.cloud.tools.managedcloudsdk.install.SdkInstallerException;
import com.google.common.collect.ImmutableMap;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/** Tests for full functionality of the {@link ManagedCloudSdk}. */
public class ManagedCloudSdkTest {

  @Rule public TemporaryFolder tempDir = new TemporaryFolder();

  private static final String FIXED_VERSION = "178.0.0";
  private final MessageCollector testListener = new MessageCollector();
  private final ProgressListener testProgressListener = new NullProgressListener();
  private final SdkComponent testComponent = SdkComponent.APP_ENGINE_JAVA;

  @Rule
  public TestWatcher watchman =
      new TestWatcher() {
        @Override
        protected void failed(Throwable e, Description description) {
          System.out.println(description + "\n" + testListener.getOutput());
        }
      };

  @Test
  public void testManagedCloudSdk_fixedVersion()
      throws BadCloudSdkVersionException, UnsupportedOsException, IOException, CommandExitException,
          InterruptedException, ManagedSdkVerificationException, ManagedSdkVersionMismatchException,
          CommandExecutionException, SdkInstallerException {
    ManagedCloudSdk testSdk =
        new ManagedCloudSdk(
            new Version(FIXED_VERSION), tempDir.getRoot().toPath(), OsInfo.getSystemOsInfo());

    Assert.assertFalse(testSdk.isInstalled());
    Assert.assertFalse(testSdk.hasComponent(testComponent));
    Assert.assertFalse(testSdk.isUpToDate());

    testSdk.newInstaller().install(testProgressListener, testListener);

    Assert.assertTrue(testSdk.isInstalled());
    Assert.assertFalse(testSdk.hasComponent(testComponent));
    Assert.assertTrue(testSdk.isUpToDate());

    testSdk
        .newComponentInstaller()
        .installComponent(testComponent, testProgressListener, testListener);

    Assert.assertTrue(testSdk.isInstalled());
    Assert.assertTrue(testSdk.hasComponent(testComponent));
    Assert.assertTrue(testSdk.isUpToDate());

    // Make sure we can't update a versioned cloud SDK.
    try {
      testSdk.newUpdater();
      Assert.fail("UnsupportedOperationException expected but not thrown");
    } catch (UnsupportedOperationException ex) {
      Assert.assertEquals("Cannot update a fixed version SDK.", ex.getMessage());
    }
  }

  @Test
  public void testManagedCloudSdk_latest()
      throws UnsupportedOsException, ManagedSdkVerificationException,
          ManagedSdkVersionMismatchException, InterruptedException, CommandExecutionException,
          CommandExitException, IOException, SdkInstallerException {
    ManagedCloudSdk testSdk =
        new ManagedCloudSdk(Version.LATEST, tempDir.getRoot().toPath(), OsInfo.getSystemOsInfo());

    Assert.assertFalse(testSdk.isInstalled());
    Assert.assertFalse(testSdk.isUpToDate());

    testSdk.newInstaller().install(testProgressListener, testListener);

    Assert.assertTrue(testSdk.isInstalled());
    Assert.assertTrue(testSdk.isUpToDate());

    // Forcibly downgrade the cloud SDK so we can test updating.
    downgradeCloudSdk(testSdk);

    Assert.assertTrue(testSdk.isInstalled());
    Assert.assertFalse(testSdk.isUpToDate());

    testSdk.newUpdater().update(testProgressListener, testListener);

    Assert.assertTrue(testSdk.isInstalled());
    Assert.assertFalse(testSdk.hasComponent(testComponent));
    Assert.assertTrue(testSdk.isUpToDate());

    testSdk
        .newComponentInstaller()
        .installComponent(testComponent, testProgressListener, testListener);

    Assert.assertTrue(testSdk.isInstalled());
    Assert.assertTrue(testSdk.hasComponent(testComponent));
    Assert.assertTrue(testSdk.isUpToDate());
  }

  private static final Path cloudSdkPartialPath =
      Paths.get("google-cloud-tools-java").resolve("managed-cloud-sdk");

  @Test
  public void testGetOsSpecificManagedSdk_windowsStandard() throws IOException {
    Path userHome = tempDir.getRoot().toPath();
    Path localAppData = Files.createDirectories(userHome.resolve("AppData").resolve("Local"));
    Properties fakeProperties = getFakeProperties(userHome.toString());
    Path windowsPath =
        ManagedCloudSdk.getOsSpecificManagedSdkHome(
            OsInfo.Name.WINDOWS,
            fakeProperties,
            ImmutableMap.of("LOCALAPPDATA", localAppData.toString()));

    Assert.assertEquals(localAppData.resolve(cloudSdkPartialPath), windowsPath);
  }

  @Test
  public void testGetOsSpecificManagedSdk_macStandard() throws IOException {
    Path userHome = tempDir.getRoot().toPath();
    Properties fakeProperties = getFakeProperties(userHome.toString());
    Path expectedPath =
        Files.createDirectories(userHome.resolve("Library").resolve("Application Support"));
    Path macPath =
        ManagedCloudSdk.getOsSpecificManagedSdkHome(
            OsInfo.Name.MAC, fakeProperties, Collections.<String, String>emptyMap());
    Assert.assertEquals(expectedPath.resolve(cloudSdkPartialPath), macPath);
  }

  @Test
  public void testGetOsSpecificManagedSdk_linuxStandard() {
    Path userHome = tempDir.getRoot().toPath();
    Properties fakeProperties = getFakeProperties(userHome.toString());
    Path expectedPath = userHome.resolve(".cache").resolve(cloudSdkPartialPath);

    Path linuxPath =
        ManagedCloudSdk.getOsSpecificManagedSdkHome(
            OsInfo.Name.LINUX, fakeProperties, Collections.<String, String>emptyMap());
    Assert.assertEquals(expectedPath, linuxPath);
  }

  @Test
  public void testGetOsSpecificManagedSdk_windowsFallbackLocalAppDataEnvNotSet() {
    Path userHome = tempDir.getRoot().toPath();
    Properties fakeProperties = getFakeProperties(userHome.toString());
    Path expectedPath = userHome.resolve(".cache").resolve(cloudSdkPartialPath);

    Path windowsPath =
        ManagedCloudSdk.getOsSpecificManagedSdkHome(
            OsInfo.Name.WINDOWS, fakeProperties, Collections.<String, String>emptyMap());

    Assert.assertEquals(expectedPath, windowsPath);
  }

  @Test
  public void testGetOsSpecificManagedSdk_windowsFallbackLocalAppDataDoesntExist() {
    Path userHome = tempDir.getRoot().toPath();
    Path localAppData = userHome.resolve("AppData").resolve("Local"); // not created
    Properties fakeProperties = getFakeProperties(userHome.toString());
    Path expectedPath = userHome.resolve(".cache").resolve(cloudSdkPartialPath);

    Path windowsPath =
        ManagedCloudSdk.getOsSpecificManagedSdkHome(
            OsInfo.Name.WINDOWS,
            fakeProperties,
            ImmutableMap.of("LOCALAPPDATA", localAppData.toString()));

    Assert.assertEquals(expectedPath, windowsPath);
  }

  @Test
  public void testGetOsSpecificManagedSdk_macFallback() {
    Path userHome = tempDir.getRoot().toPath();
    Properties fakeProperties = getFakeProperties(userHome.toString());
    Path expectedPath = userHome.resolve(".cache").resolve(cloudSdkPartialPath);

    Path macPath =
        ManagedCloudSdk.getOsSpecificManagedSdkHome(
            OsInfo.Name.MAC, fakeProperties, Collections.<String, String>emptyMap());
    Assert.assertEquals(expectedPath, macPath);
  }

  private static Properties getFakeProperties(String userHome) {
    Properties properties = new Properties();
    properties.put("user.home", userHome);
    return properties;
  }

  public void downgradeCloudSdk(ManagedCloudSdk testSdk)
      throws InterruptedException, CommandExitException, CommandExecutionException,
          UnsupportedOsException {
    Map<String, String> env = null;
    if (OsInfo.getSystemOsInfo().name().equals(OsInfo.Name.WINDOWS)) {
      env = WindowsBundledPythonCopierTestHelper.newInstance(testSdk.getGcloud()).copyPython();
    }
    CommandRunner.newRunner()
        .run(
            Arrays.asList(
                testSdk.getGcloud().toString(),
                "components",
                "update",
                "--quiet",
                "--version=" + FIXED_VERSION),
            null,
            env,
            testListener);
  }
}
