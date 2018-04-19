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

package com.google.cloud.tools.appengine.cloudsdk;

import com.google.cloud.tools.appengine.api.AppEngineException;
import com.google.cloud.tools.appengine.api.debug.DefaultGenRepoInfoFileConfiguration;
import com.google.cloud.tools.appengine.cloudsdk.process.ProcessHandlerException;
import com.google.common.collect.ImmutableList;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class CloudSdkGenRepoInfoFileTest {

  @Test
  public void testNullSdk() {
    try {
      new CloudSdkGenRepoInfoFile(null);
      Assert.fail("allowed null SDK");
    } catch (NullPointerException expected) {
    }
  }

  @Test
  public void testGenerate() throws AppEngineException, ProcessHandlerException, IOException {
    GcloudRunner gcloudRunner = Mockito.mock(GcloudRunner.class);
    CloudSdkGenRepoInfoFile model = new CloudSdkGenRepoInfoFile(gcloudRunner);
    DefaultGenRepoInfoFileConfiguration configuration = new DefaultGenRepoInfoFileConfiguration();
    configuration.setOutputDirectory(new File("output"));
    configuration.setSourceDirectory(new File("source"));
    model.generate(configuration);

    List<String> arguments =
        ImmutableList.of(
            "gcloud",
            "beta",
            "debug",
            "source",
            "gen-repo-info-file",
            "--output-directory",
            "output",
            "--source-directory",
            "source");
    Mockito.verify(gcloudRunner).run(arguments, null);
  }
}
