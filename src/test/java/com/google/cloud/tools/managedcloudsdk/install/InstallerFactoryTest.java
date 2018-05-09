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

package com.google.cloud.tools.managedcloudsdk.install;

import com.google.cloud.tools.managedcloudsdk.OsInfo;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/** Tests for {@link InstallerFactory}. * */
@RunWith(Parameterized.class)
public class InstallerFactoryTest {

  @Rule public TemporaryFolder tmp = new TemporaryFolder();

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {new OsInfo(OsInfo.Name.LINUX, null), UnixInstallScriptProvider.class},
          {new OsInfo(OsInfo.Name.MAC, null), UnixInstallScriptProvider.class},
          {new OsInfo(OsInfo.Name.WINDOWS, null), WindowsInstallScriptProvider.class}
        });
  }

  @Parameterized.Parameter(0)
  public OsInfo os;

  @Parameterized.Parameter(1)
  public Class<InstallScriptProvider> expectedInstallScriptProviderClass;

  @Test
  public void testNewInstaller_latestVersion() {
    Installer installer = new InstallerFactory(os, false).newInstaller(null, null, null);
    Assert.assertEquals(
        expectedInstallScriptProviderClass, installer.getInstallScriptProvider().getClass());
  }
}
