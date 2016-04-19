package com.google.cloud.tools.app.executor;

import com.google.cloud.tools.app.internal.process.AsynchronousProcessRunnerRunner;
import com.google.cloud.tools.app.internal.process.ProcessRunner;
import com.google.cloud.tools.app.internal.process.ProcessRunnerException;
import com.google.cloud.tools.app.internal.process.SimpleProcessRunner;
import com.google.cloud.tools.app.internal.sdk.CloudSdk;
import com.google.cloud.tools.app.internal.sdk.CloudSdkCommandFactory;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Created by appu on 3/31/16.
 */
public class CloudSdkDevAppServerExecutor implements DevAppServerExecutor {

  private final CloudSdk sdk;

  public CloudSdkDevAppServerExecutor(Path cloudSdkHome) {
    this.sdk = new CloudSdk(cloudSdkHome);
  }

  @Override
  public int runDevAppServer(List<String> args) throws ExecutorException {
    ProcessRunner p = new SimpleProcessRunner();
    String[] command = new CloudSdkCommandFactory(sdk).getDevAppServerCommand(args);

    try {
      return p.run(command);
    } catch (ProcessRunnerException pe) {
      throw new ExecutorException(pe);
    }
  }

  @Override
  /**
   * It's possible we need to add a module count here
   */
  public Future<Integer> runDevAppServerAsync(List<String> args) throws ExecutorException {
    ProcessRunner p = new SimpleProcessRunner();
    String[] command = new CloudSdkCommandFactory(sdk).getDevAppServerCommand(args);

    return new AsynchronousProcessRunnerRunner().runAsynchronous(command, p);
  }
}
