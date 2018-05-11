/*
 * Copyright 2017 Google Inc.
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

/**
 * Signals that there have been problems when parsing JSON input.
 *
 * <p>May signal general problems, including syntactic or semantic errors. However, the main reason
 * that this exception was introduced in the library is to wrap the {@link
 * com.google.gson.JsonSyntaxException} {@link RuntimeException} and make it a checked exception.
 */
public class JsonParseException extends Exception {

  public JsonParseException(Throwable cause) {
    super(cause);
  }

  public JsonParseException(String message) {
    super(message);
  }
}
