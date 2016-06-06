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

package com.google.cloud.tools.appengine.cloudsdk.internal.args;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link AppCfgArgs}
 */
public class AppCfgArgsTest {

  @Test
  public void testGet_string() {
    assertEquals(AppCfgArgs.get("name1", "value1"), Collections.singletonList("--name1=value1"));
    assertEquals(AppCfgArgs.get("name2", "value2"), Collections.singletonList("--name2=value2"));
  }

  @Test
  public void testGet_boolean() {
    assertEquals(AppCfgArgs.get("name1", true), Collections.singletonList("--name1"));
    assertEquals(AppCfgArgs.get("name2", true), Collections.singletonList("--name2"));

    assertEquals(AppCfgArgs.get("name", false), Collections.emptyList());
    assertEquals(AppCfgArgs.get("name", (Boolean) null), Collections.emptyList());
  }
}
