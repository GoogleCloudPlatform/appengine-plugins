package com.google.cloud.tools.app.impl.appcfg;

import java.nio.file.Path;
import java.util.List;

/**
 * Created by appu on 4/1/16.
 */
public class AppEngineSdk {

  private final Path appengineSdk;

  public AppEngineSdk(Path appengineSdk) {
    this.appengineSdk = appengineSdk;
  }

  public void runCommand(List<String> args) {
    // AppEngineSdk requires this system property to be set.
    System.setProperty("appengine.impl.root", appengineSdk.toString());
    com.google.appengine.tools.admin.AppCfg.main(args.toArray(new String[args.size()]));
  }

}
