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

package com.google.cloud.tools.app.deploy.gcloud;

import com.google.cloud.tools.app.deploy.DeployResult;
import com.google.cloud.tools.app.deploy.gcloud.StringResultConverter;

public class DeployResultConverter implements StringResultConverter<DeployResult> {

  @Override
  public DeployResult getResult(String result) {
    DeployResult d = new DeployResult();
    d.data = result;
    return d;
  }
}
