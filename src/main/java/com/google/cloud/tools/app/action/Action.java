package com.google.cloud.tools.app.action;

import com.google.cloud.tools.app.executor.ExecutorException;

import java.io.IOException;

/**
 * Created by appu on 3/31/16.
 */
public interface Action {

  public int execute() throws IOException, ExecutorException;
}
