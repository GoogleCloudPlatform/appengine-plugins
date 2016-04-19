package com.google.cloud.tools.app.internal.sdk;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * Created by appu on 3/29/16.
 */
public class CloudSdkCommandFactory {

  public CloudSdk cloudSdk;

  public CloudSdkCommandFactory(CloudSdk cloudSdk) {
    this.cloudSdk = cloudSdk;
  }

  public String[] getGCloudAppCommand(List<String> args) {
    List<String> command = Lists.newArrayList();
    if (System.getProperty("os.name").startsWith("Windows")) {
      command.add("cmd.exe");
      command.add("/c");
    }
    command.add(cloudSdk.getGCloudPath().toString());
    command.add("preview");
    command.add("app");
    command.addAll(args);
    return command.toArray(new String[command.size()]);
  }

  public String[] getDevAppServerCommand(List<String> args) {
    List<String> command = Lists.newArrayList();
    if (System.getProperty("os.name").startsWith("Windows")) {
      command.add("cmd.exe");
      command.add("/c");
    }
    command.add(cloudSdk.getDevAppServerPath().toString());
    command.addAll(args);
    return command.toArray(new String[command.size()]);
  }
}
