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

package com.google.cloud.tools.appengine.experimental;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

import com.google.cloud.tools.appengine.experimental.internal.cloudsdk.CloudSdkAppEngineRequestFactory;
import java.io.IOException;
import java.nio.file.Path;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

public class AppEngineRequestsTest {

  @Rule public ExpectedException exception = ExpectedException.none();
  @Rule public TemporaryFolder testDirectory = new TemporaryFolder();

  private Path sdkHome;

  @Before
  public void setup() throws IOException {
    sdkHome = testDirectory.newFolder("sdk").toPath();
  }

  @Test
  public void testNewRequestFactory_noConfig() {
    exception.expect(IllegalStateException.class);
    exception.expectMessage("No App Engine request factory implementation defined");

    AppEngineRequests.newRequestFactoryBuilder().build();
  }

  @Test
  public void testNewRequestFactory_cloudSdk() {
    AppEngineRequestFactory rf =
        AppEngineRequests.newRequestFactoryBuilder().cloudSdk(sdkHome).build();
    assertThat(rf, instanceOf(CloudSdkAppEngineRequestFactory.class));
  }
}
