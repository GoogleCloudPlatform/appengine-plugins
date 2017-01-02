package com.google.cloud.tools.appengine.cloudsdk;

import java.io.File;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.google.cloud.tools.appengine.api.debug.DefaultGenRepoInfoFileConfiguration;
import com.google.cloud.tools.appengine.cloudsdk.internal.process.ProcessRunnerException;
import com.google.common.collect.ImmutableList;

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
  public void testGenerate() throws ProcessRunnerException {
    CloudSdk sdk = Mockito.mock(CloudSdk.class);
    CloudSdkGenRepoInfoFile model = new CloudSdkGenRepoInfoFile(sdk);
    DefaultGenRepoInfoFileConfiguration configuration = new DefaultGenRepoInfoFileConfiguration();
    configuration.setOutputDirectory(new File("output"));
    configuration.setSourceDirectory(new File("source"));
    model.generate(configuration);
    
    List<String> arguments = ImmutableList.of("gen-repo-info-file", "--output-directory", "output",
        "--source-directory", "source");
    Mockito.verify(sdk).runSourceCommand(arguments);
  }


}
