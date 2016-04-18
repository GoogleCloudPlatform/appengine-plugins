package com.google.cloud.tools.app.impl.appcfg;

import com.google.appengine.tools.admin.AppCfg;
import com.google.cloud.tools.app.impl.executor.AppExecutor;
import com.google.cloud.tools.app.impl.executor.ExecutorException;

import java.nio.file.Path;
import java.util.List;

/**
 * Created by appu on 4/1/16.
 */
public class AppCfgExecutor implements AppExecutor {

  private final Path appengineSdk;

  public AppCfgExecutor(Path appengineSdk) {
    this.appengineSdk = appengineSdk;
  }

  @Override
  public int runApp(List<String> args) throws ExecutorException {
    // AppCfg requires this system property to be set.
    System.setProperty("appengine.impl.root", appengineSdk.toString());
    AppCfg.main(args.toArray(new String[args.size()]));
    return 0;
  }

}
