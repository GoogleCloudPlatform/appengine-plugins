package com.google.cloud.tools.app.executor;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import javax.annotation.Nullable;

public interface StageExecutor {

  int runStage(List<String> args, @Nullable Path dockerfile, @Nullable Path dockerfileDestination)
      throws IOException;

}
