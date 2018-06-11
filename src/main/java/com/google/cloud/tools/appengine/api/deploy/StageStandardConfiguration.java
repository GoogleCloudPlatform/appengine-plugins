package com.google.cloud.tools.appengine.api.deploy;

import java.io.File;
import javax.annotation.Nullable;

/**
 * Arguments needed to stage an App Engine standard environment application. Null return values
 * indicate that the configuration was not set, and thus assumes the tool default value.
 */
public interface StageStandardConfiguration {

  File getSourceDirectory();

  File getStagingDirectory();

  @Nullable
  File getDockerfile();

  @Nullable
  Boolean getEnableQuickstart();

  @Nullable
  Boolean getDisableUpdateCheck();

  @Nullable
  Boolean getEnableJarSplitting();

  @Nullable
  String getJarSplittingExcludes();

  @Nullable
  String getCompileEncoding();

  @Nullable
  Boolean getDeleteJsps();

  @Nullable
  Boolean getEnableJarClasses();

  @Nullable
  Boolean getDisableJarJsps();

  @Nullable
  String getRuntime();
}
