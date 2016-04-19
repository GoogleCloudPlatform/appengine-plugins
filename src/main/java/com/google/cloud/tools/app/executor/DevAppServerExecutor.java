package com.google.cloud.tools.app.executor;

import java.util.List;
import java.util.concurrent.Future;

public interface DevAppServerExecutor {

  int runDevAppServer(List<String> args) throws ExecutorException;
  Future<Integer> runDevAppServerAsync(List<String> args) throws ExecutorException;

}
