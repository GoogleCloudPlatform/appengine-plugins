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

import java.util.Map;

/**
 * Identifies the traffic split for the version(s) of a given service or services in {@link
 * AppEngineServices}.
 */
public interface TrafficSplitConfiguration extends ServicesSelectionConfiguration {

  Map<String, Double> getVersionToTrafficSplit();
}
