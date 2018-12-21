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

package com.google.cloud.tools.appengine.operations;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;

import com.google.cloud.tools.appengine.AppEngineException;
import com.google.cloud.tools.appengine.operations.cloudsdk.process.ProcessHandlerException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AuthTest {
  @Mock private GcloudRunner gcloudRunner;
  @Rule public TemporaryFolder tmpDir = new TemporaryFolder();

  @Test
  public void testNullSdk() {
    try {
      new Auth(null);
      Assert.fail("allowed null runner");
    } catch (NullPointerException expected) {
      // pass
    }
  }

  @Test
  public void testLogin_withUser() throws AppEngineException, ProcessHandlerException, IOException {
    String testUsername = "potato@potato.com";
    new Auth(gcloudRunner).login(testUsername);
    Mockito.verify(gcloudRunner).run(eq(Arrays.asList("auth", "login", testUsername)), isNull());
  }

  @Test
  public void testLogin_withBadUser() {
    String testUsername = "potato@pota@to.com";
    try {
      new Auth(gcloudRunner).login(testUsername);
      Assert.fail("Should have failed with bad user.");
    } catch (AppEngineException e) {
      Assert.assertThat(
          e.getMessage(), CoreMatchers.containsString("Invalid email address: " + testUsername));
      // pass
    }
  }

  @Test
  public void testLogin_withNullUser() throws AppEngineException {
    try {
      new Auth(gcloudRunner).login(null);
      Assert.fail("Should have failed with bad user.");
    } catch (NullPointerException npe) {
      // pass
    }
  }

  @Test
  public void testLogin_noUser() throws ProcessHandlerException, AppEngineException, IOException {
    new Auth(gcloudRunner).login();
    Mockito.verify(gcloudRunner).run(eq(Arrays.asList("auth", "login")), isNull());
  }

  @Test
  public void testActivateServiceAccount()
      throws ProcessHandlerException, IOException, AppEngineException {
    Path jsonKeyFile = tmpDir.newFile("json-keys").toPath();
    new Auth(gcloudRunner).activateServiceAccount(jsonKeyFile);
    Mockito.verify(gcloudRunner)
        .run(
            eq(
                Arrays.asList(
                    "auth",
                    "activate-service-account",
                    "--key-file",
                    jsonKeyFile.toAbsolutePath().toString())),
            isNull());
  }

  @Test
  public void testActivateServiceAccount_badKeyFile() throws AppEngineException {
    Path jsonKeyFile = tmpDir.getRoot().toPath().resolve("non-existant-file");
    try {
      new Auth(gcloudRunner).activateServiceAccount(jsonKeyFile);
      Assert.fail("Should have failed with bad keyfile.");
    } catch (IllegalArgumentException e) {
      Assert.assertThat(
          e.getMessage(), CoreMatchers.containsString("File does not exist: " + jsonKeyFile));
      // pass
    }
  }
}
