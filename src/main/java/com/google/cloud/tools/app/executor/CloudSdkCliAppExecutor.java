package com.google.cloud.tools.app.executor;

import com.google.cloud.tools.app.internal.process.ProcessRunner;
import com.google.cloud.tools.app.internal.process.ProcessRunnerException;
import com.google.cloud.tools.app.internal.process.SimpleProcessRunner;
import com.google.cloud.tools.app.internal.sdk.CloudSdk;
import com.google.cloud.tools.app.internal.sdk.CloudSdkCommandFactory;

import java.nio.file.Path;
import java.util.List;

/**
 * Created by appu on 3/30/16.
 */
public class CloudSdkCliAppExecutor implements AppExecutor {

  private final CloudSdk sdk;

  public CloudSdkCliAppExecutor(Path cloudSdkHome) {
    this.sdk = new CloudSdk(cloudSdkHome);
  }

  @Override
  public int runApp(List<String> args) throws ExecutorException {

    ProcessRunner p = new SimpleProcessRunner();
    String[] command = new CloudSdkCommandFactory(sdk).getGCloudAppCommand(args);

    try {
      return p.run(command);
    } catch (ProcessRunnerException pe) {
      throw new ExecutorException(pe);
    }
  }
}
