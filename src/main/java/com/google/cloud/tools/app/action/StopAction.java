/**
 * Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.cloud.tools.app.action;

import com.google.cloud.tools.app.config.StopConfiguration;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;

/**
 * Stops the local development server.
 */
public class StopAction implements Action {

  private static Logger logger = Logger.getLogger(StopAction.class.getName());
  private static final String DEFAULT_ADMIN_HOST = "localhost";
  private static final int DEFAULT_ADMIN_PORT = 8000;
  private HttpURLConnection connection;

  public StopAction(StopConfiguration configuration) throws IOException {
    Preconditions.checkNotNull(configuration);

    URL adminServerUrl = new URL(
        "http",
        configuration.getAdminHost() != null ? configuration.getAdminHost() : DEFAULT_ADMIN_HOST,
        configuration.getAdminPort() != null ? configuration.getAdminPort() : DEFAULT_ADMIN_PORT,
        "/quit");
    connection = (HttpURLConnection) adminServerUrl.openConnection();
  }

  /**
   * Issues a HTTP GET request to /quit of the admin port of the local development server admin
   * host.
   */
  @Override
  public int execute() throws IOException {
    connection.setReadTimeout(4000);
    connection.connect();
    connection.disconnect();
    int responseCode = connection.getResponseCode();
    if (responseCode < 200 || responseCode > 299) {
      logger.severe(
          "The development server responded with " + connection.getResponseMessage() + ".");
      return 1;
    }

    return 0;
  }

  @VisibleForTesting
  void setConnection(HttpURLConnection connection) {
    this.connection = connection;
  }
}
