package com.google.cloud.tools.app.impl.cloudsdk.internal.sdk;


/**
 * Generic Cloud Sdk exception class
 */
public class CloudSdkConfigurationException extends RuntimeException {
  public CloudSdkConfigurationException() {
    super();
  }

  public CloudSdkConfigurationException(String message) {
    super(message);
  }

  public CloudSdkConfigurationException(String message, Throwable cause) {
    super(message, cause);
  }

  public CloudSdkConfigurationException(Throwable cause) {
    super(cause);
  }

  protected CloudSdkConfigurationException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
