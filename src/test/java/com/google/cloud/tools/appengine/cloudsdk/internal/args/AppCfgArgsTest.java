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

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import org.junit.Test;

/** Unit tests for {@link AppCfgArgs} */
public class AppCfgArgsTest {

  @Test
  public void testGet_string() {
    assertEquals(Collections.singletonList("--name1=value1"), AppCfgArgs.get("name1", "value1"));
    assertEquals(Collections.singletonList("--name2=value2"), AppCfgArgs.get("name2", "value2"));
  }

  @Test
  public void testGet_boolean() {
    assertEquals(Collections.singletonList("--name1"), AppCfgArgs.get("name1", true));
    assertEquals(Collections.singletonList("--name2"), AppCfgArgs.get("name2", true));

    assertEquals(Collections.emptyList(), AppCfgArgs.get("name", false));
    assertEquals(Collections.emptyList(), AppCfgArgs.get("name", (Boolean) null));
  }
}
