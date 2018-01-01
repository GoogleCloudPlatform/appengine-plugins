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
import com.google.cloud.tools.managedcloudsdk.install.SdkInstallerException;
import com.google.cloud.tools.managedcloudsdk.install.UnknownArchiveTypeException;
import java.io.IOException;
import java.util.Arrays;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/** Tests for full functionality of the {@link ManagedCloudSdk}. */
public class ManagedCloudSdkTest {

  @Rule public TemporaryFolder tempDir = new TemporaryFolder();

  private static final String FIXED_VERSION = "174.0.0";
  private final MessageCollector testListener = new MessageCollector();
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
          UnknownArchiveTypeException, CommandExecutionException, SdkInstallerException {
    ManagedCloudSdk testSdk =
        new ManagedCloudSdk(
            new Version(FIXED_VERSION), tempDir.getRoot().toPath(), OsInfo.getSystemOsInfo());

    Assert.assertFalse(testSdk.isInstalled());
    Assert.assertFalse(testSdk.hasComponent(testComponent));
    Assert.assertFalse(testSdk.isUpToDate());

    testSdk.newInstaller().install(testListener);

    Assert.assertTrue(testSdk.isInstalled());
    Assert.assertFalse(testSdk.hasComponent(testComponent));
    Assert.assertTrue(testSdk.isUpToDate());

    testSdk.newComponentInstaller().installComponent(testComponent, testListener);

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
          CommandExitException, UnknownArchiveTypeException, IOException, SdkInstallerException {
    ManagedCloudSdk testSdk =
        new ManagedCloudSdk(Version.LATEST, tempDir.getRoot().toPath(), OsInfo.getSystemOsInfo());

    Assert.assertFalse(testSdk.isInstalled());
    Assert.assertFalse(testSdk.isUpToDate());

    testSdk.newInstaller().install(testListener);

    Assert.assertTrue(testSdk.isInstalled());
    Assert.assertTrue(testSdk.isUpToDate());

    // Forcibly downgrade the cloud SDK so we can test updating.
    CommandRunner.newRunner()
        .run(
            Arrays.asList(
                testSdk.getGcloud().toString(),
                "components",
                "update",
                "--quiet",
                "--version=" + FIXED_VERSION),
            null,
            null,
            testListener);

    Assert.assertTrue(testSdk.isInstalled());
    Assert.assertFalse(testSdk.isUpToDate());

    testSdk.newUpdater().update(testListener);

    Assert.assertTrue(testSdk.isInstalled());
    Assert.assertFalse(testSdk.hasComponent(testComponent));
    Assert.assertTrue(testSdk.isUpToDate());

    testSdk.newComponentInstaller().installComponent(testComponent, testListener);

    Assert.assertTrue(testSdk.isInstalled());
    Assert.assertTrue(testSdk.hasComponent(testComponent));
    Assert.assertTrue(testSdk.isUpToDate());
  }

  private static class MessageCollector implements MessageListener {
    StringBuilder output = new StringBuilder("");

    @Override
    public void message(String rawString) {
      output.append(rawString);
    }

    public String getOutput() {
      return output.toString();
    }
  }
}
