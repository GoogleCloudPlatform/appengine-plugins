package com.google.cloud.tools.app.impl.cloudsdk;

import com.google.cloud.tools.app.impl.executor.AppExecutor;
import com.google.cloud.tools.app.impl.executor.ExecutorException;
import com.google.cloud.tools.app.impl.cloudsdk.internal.process.ProcessRunner;
import com.google.cloud.tools.app.impl.cloudsdk.internal.process.ProcessRunnerException;
import com.google.cloud.tools.app.impl.cloudsdk.internal.sdk.CloudSdkCommandFactory;

import java.util.List;

/**
 * Created by appu on 3/30/16.
 */
public class CloudSdkAppExecutor implements AppExecutor {

  private CloudSdkCommandFactory cloudSdkCommandFactory;
  private ProcessRunner processRunner;

  public CloudSdkAppExecutor(
      CloudSdkCommandFactory cloudSdkCommandFactory,
      ProcessRunner processRunner) {
    this.cloudSdkCommandFactory = cloudSdkCommandFactory;
    this.processRunner = processRunner;
  }

  @Override
  public int runApp(List<String> args) throws ExecutorException {

    String[] command = cloudSdkCommandFactory.getGCloudAppCommand(args);

    try {
      return processRunner.run(command);
    } catch (ProcessRunnerException pe) {
      throw new ExecutorException(pe);
    }
  }
}
