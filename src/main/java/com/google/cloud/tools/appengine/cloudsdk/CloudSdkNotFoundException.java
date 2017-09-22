/*
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

package com.google.cloud.tools.appengine.cloudsdk;

import com.google.cloud.tools.appengine.api.AppEngineException;

/** The Cloud SDK could not be found in any of the expected locations. */
public class CloudSdkNotFoundException extends AppEngineException {

  public CloudSdkNotFoundException(String message) {
    super(message);
  }

  public CloudSdkNotFoundException(Throwable cause) {
    super(cause);
  }

  public CloudSdkNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }
}
