package com.google.cloud.tools.appengine.cloudsdk;

import com.google.cloud.tools.appengine.api.AppEngineException;

/**
 * User needs to run <samp>gcloud components install app-engine-java</samp>.
 */
public class AppEngineComponentsNotInstalledException extends AppEngineException {

  AppEngineComponentsNotInstalledException(String message) {
    super(message);
  }

}
