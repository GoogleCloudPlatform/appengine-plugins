/*
 * Copyright 2018 Google LLC. All Rights Reserved.
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

package com.google.cloud.tools.appengine.api.devserver;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class DefaultRunConfigurationTest {

  @Test
  public void testSetGetProjectId() {
    DefaultRunConfiguration defaultRunConfiguration = new DefaultRunConfiguration();
    assertNull(defaultRunConfiguration.getProjectId());
    defaultRunConfiguration.setProjectId("my-project");
    assertEquals("my-project", defaultRunConfiguration.getProjectId());
  }
}
