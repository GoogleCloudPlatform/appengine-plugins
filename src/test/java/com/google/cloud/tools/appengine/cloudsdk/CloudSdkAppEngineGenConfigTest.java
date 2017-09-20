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

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.cloud.tools.appengine.api.AppEngineException;
import com.google.cloud.tools.appengine.api.genconfig.DefaultGenConfigParams;
import com.google.cloud.tools.appengine.cloudsdk.internal.process.ProcessRunnerException;
import com.google.cloud.tools.test.utils.SpyVerifier;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

/** Unit tests for {@link com.google.cloud.tools.appengine.cloudsdk.CloudSdkAppEngineGenConfig}. */
@RunWith(MockitoJUnitRunner.class)
public class CloudSdkAppEngineGenConfigTest {

  @Mock private CloudSdk sdk;

  @Rule public TemporaryFolder tmpDir = new TemporaryFolder();

  private File source;

  private CloudSdkAppEngineGenConfig genConfig;

  @Before
  public void setUp() throws IOException {
    source = tmpDir.newFolder("source");
    genConfig = new CloudSdkAppEngineGenConfig(sdk);
  }

  @Test
  public void testPrepareCommand_allFlags() throws Exception {

    DefaultGenConfigParams params = Mockito.spy(new DefaultGenConfigParams());
    params.setSourceDirectory(source);
    params.setConfig("app.yaml");
    params.setCustom(true);
    params.setRuntime("java");

    SpyVerifier.newVerifier(params).verifyDeclaredSetters();

    List<String> expected =
        ImmutableList.of(
            "gen-config",
            source.toString(),
            "--config",
            "app.yaml",
            "--custom",
            "--runtime",
            "java");

    genConfig.genConfig(params);

    verify(sdk, times(1)).runAppCommand(eq(expected));
    SpyVerifier.newVerifier(params)
        .verifyDeclaredGetters(ImmutableMap.<String, Integer>of("getSourceDirectory", 4));
  }

  @Test
  public void testPrepareCommand_booleanFlags() throws AppEngineException, ProcessRunnerException {

    DefaultGenConfigParams params = new DefaultGenConfigParams();
    params.setSourceDirectory(source);
    params.setCustom(false);

    List<String> expected = ImmutableList.of("gen-config", source.toString(), "--no-custom");

    genConfig.genConfig(params);

    verify(sdk, times(1)).runAppCommand(eq(expected));
  }

  @Test
  public void testPrepareCommand_noFlags() throws AppEngineException, ProcessRunnerException {
    DefaultGenConfigParams params = new DefaultGenConfigParams();
    params.setSourceDirectory(source);

    List<String> expected = ImmutableList.of("gen-config", source.toString());

    genConfig.genConfig(params);

    verify(sdk, times(1)).runAppCommand(eq(expected));
  }
}
