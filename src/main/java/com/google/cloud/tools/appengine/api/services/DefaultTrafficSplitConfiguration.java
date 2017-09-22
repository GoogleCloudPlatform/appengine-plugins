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

package com.google.cloud.tools.appengine.api.services;

import com.google.cloud.tools.appengine.api.DefaultConfiguration;
import java.util.Collection;
import java.util.Map;

/** Plain Java bean implementation of {@link TrafficSplitConfiguration}. */
public class DefaultTrafficSplitConfiguration extends DefaultConfiguration
    implements TrafficSplitConfiguration {

  private Collection<String> services;
  private Map<String, Double> versionToTrafficSplit;

  @Override
  public Collection<String> getServices() {
    return services;
  }

  public void setServices(Collection<String> services) {
    this.services = services;
  }

  @Override
  public Map<String, Double> getVersionToTrafficSplit() {
    return versionToTrafficSplit;
  }

  public void setVersionToTrafficSplit(Map<String, Double> versionToTrafficSplit) {
    this.versionToTrafficSplit = versionToTrafficSplit;
  }
}
