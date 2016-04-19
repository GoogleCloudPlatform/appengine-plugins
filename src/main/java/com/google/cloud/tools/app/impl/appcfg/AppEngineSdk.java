package com.google.cloud.tools.app.impl.appcfg;

import java.nio.file.Path;
import java.util.List;

/**
 * App Engine SDK CLI wrapper.
 */
public class AppEngineSdk {

  private final Path appengineSdk;

  public AppEngineSdk(Path appengineSdk) {
    this.appengineSdk = appengineSdk;
  }

  /**
   * Executes an App Engine SDK CLI command synchronously.
   */
  public void runCommand(List<String> args) {
    // AppEngineSdk requires this system property to be set.
    System.setProperty("appengine.impl.root", appengineSdk.toString());
    com.google.appengine.tools.admin.AppCfg.main(args.toArray(new String[args.size()]));
  }

}
