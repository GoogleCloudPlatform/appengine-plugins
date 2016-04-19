package com.google.cloud.tools.app.executor;

import com.google.appengine.tools.admin.AppCfg;

import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import javax.annotation.Nullable;

/**
 * Created by appu on 4/1/16.
 */
public class AppCfgStageExecutor implements StageExecutor {

  private final Path appengineSdk;

  public AppCfgStageExecutor(Path appengineSdk) {
    this.appengineSdk = appengineSdk;
  }

  @Override
  public int runStage(List<String> args, @Nullable Path dockerfile,
        @Nullable Path dockerfileDestination) throws IOException {
    args.add(0, "stage");
    // AppCfg requires this system property to be set.
    System.setProperty("appengine.sdk.root", appengineSdk.toString());
    AppCfg.main(args.toArray(new String[args.size()]));
    if (dockerfile != null && dockerfileDestination != null) {
      Files.copy(dockerfile, dockerfileDestination, StandardCopyOption.REPLACE_EXISTING);
    }
    return 0;
  }

}
