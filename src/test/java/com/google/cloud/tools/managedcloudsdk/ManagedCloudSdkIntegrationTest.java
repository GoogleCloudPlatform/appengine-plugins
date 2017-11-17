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

import com.google.cloud.tools.managedcloudsdk.components.SdkComponent;
import com.google.cloud.tools.managedcloudsdk.gcloud.GcloudCommandFactory;
import com.google.cloud.tools.managedcloudsdk.process.CommandExitException;
import com.google.common.util.concurrent.ListenableFuture;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Integration tests for {@link ManagedCloudSdk} and all supporting classes. */
public class ManagedCloudSdkIntegrationTest {

  @Rule public TemporaryFolder tempDir = new TemporaryFolder();

  private static final String FIXED_VERSION = "169.0.0";
  private final MessageListener testListener = new StdOutMessageListener();
  private final SdkComponent testComponent = SdkComponent.APP_ENGINE_JAVA;

  @Test
  public void testManagedCloudSdk_versionedInstall()
      throws BadCloudSdkVersionException, UnsupportedOsException, ExecutionException, IOException,
          CommandExitException, InterruptedException {
    ManagedCloudSdk testSdk =
        new ManagedCloudSdk(
            new Version(FIXED_VERSION), tempDir.getRoot().toPath(), OsInfo.getSystemOsInfo());

    Assert.assertFalse(testSdk.isInstalled());
    Assert.assertFalse(testSdk.hasComponent(testComponent));
    Assert.assertFalse(testSdk.isUpToDate());

    ListenableFuture<Path> installFuture = testSdk.newInstaller().downloadSdk(testListener);
    installFuture.get();

    Assert.assertTrue(testSdk.isInstalled());
    Assert.assertFalse(testSdk.hasComponent(testComponent));
    Assert.assertTrue(testSdk.isUpToDate());

    ListenableFuture<Void> componentFuture =
        testSdk.newComponentInstaller().installComponent(testComponent, testListener);
    componentFuture.get();

    Assert.assertTrue(testSdk.isInstalled());
    Assert.assertTrue(testSdk.hasComponent(testComponent));
    Assert.assertTrue(testSdk.isUpToDate());

    // make sure we cant update a versioned cloud sdk
    Assert.assertEquals(null, testSdk.newUpdater());
  }

  @Test
  public void testManagedCloudSdk_latestInstall()
      throws BadCloudSdkVersionException, UnsupportedOsException, ExecutionException, IOException,
          CommandExitException, InterruptedException {
    ManagedCloudSdk testSdk =
        new ManagedCloudSdk(Version.LATEST, tempDir.getRoot().toPath(), OsInfo.getSystemOsInfo());

    Assert.assertFalse(testSdk.isInstalled());
    Assert.assertFalse(testSdk.hasComponent(testComponent));
    Assert.assertFalse(testSdk.isUpToDate());

    ListenableFuture<Path> installFuture = testSdk.newInstaller().downloadSdk(testListener);
    installFuture.get();

    Assert.assertTrue(testSdk.isInstalled());
    Assert.assertFalse(testSdk.hasComponent(testComponent));
    Assert.assertTrue(testSdk.isUpToDate());

    ListenableFuture<Void> componentFuture =
        testSdk.newComponentInstaller().installComponent(testComponent, testListener);
    componentFuture.get();

    Assert.assertTrue(testSdk.isInstalled());
    Assert.assertTrue(testSdk.hasComponent(testComponent));
    Assert.assertTrue(testSdk.isUpToDate());
  }

  @Test
  public void testManagedCloudSdk_updateLatest()
      throws ExecutionException, IOException, CommandExitException, UnsupportedOsException,
          InterruptedException {
    ManagedCloudSdk testSdk =
        new ManagedCloudSdk(Version.LATEST, tempDir.getRoot().toPath(), OsInfo.getSystemOsInfo());

    Assert.assertFalse(testSdk.isInstalled());
    Assert.assertFalse(testSdk.isUpToDate());

    ListenableFuture<Path> installFuture = testSdk.newInstaller().downloadSdk(testListener);
    installFuture.get();

    Assert.assertTrue(testSdk.isInstalled());
    Assert.assertTrue(testSdk.isUpToDate());

    // forcibly downgrade the cloud SDK so we can test updating.
    new GcloudCommandFactory(testSdk.getGcloud())
        .newRunner(
            Arrays.asList("components", "update", "--quiet", "--version=" + FIXED_VERSION),
            testListener)
        .run();

    Assert.assertTrue(testSdk.isInstalled());
    Assert.assertFalse(testSdk.isUpToDate());

    ListenableFuture<Void> updateFuture = testSdk.newUpdater().update(testListener);
    updateFuture.get();

    Assert.assertTrue(testSdk.isInstalled());
    Assert.assertTrue(testSdk.isUpToDate());
  }

  static class StdOutMessageListener implements MessageListener {
    @Override
    public void message(String rawString) {
      System.out.print(rawString);
    }
  }
}
