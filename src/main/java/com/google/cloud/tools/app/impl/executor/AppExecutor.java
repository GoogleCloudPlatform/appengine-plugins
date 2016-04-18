package com.google.cloud.tools.app.impl.executor;

import java.util.List;

/**
 * Created by appu on 3/30/16.
 */
public interface AppExecutor {

  int runApp(List<String> args) throws ExecutorException;
}
