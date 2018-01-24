/*
 * Copyright 2018 Google Inc.
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
import com.google.cloud.tools.managedcloudsdk.install.SdkInstallerException;
import java.io.IOException;

public class Main {
  public static void main(String[] args) throws BadCloudSdkVersionException, UnsupportedOsException, InterruptedException, CommandExecutionException, SdkInstallerException, CommandExitException, IOException {
    ManagedCloudSdk testSdk = ManagedCloudSdk.newManagedSdk();

    testSdk.newInstaller().install(new MessageListener() {
      @Override
      public void message(String rawString) {
        System.out.print(rawString);
      }
    });
  }
}
