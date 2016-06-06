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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link DevAppServerArgs}
 */
public class DevAppServerArgsTest {

  @Test
  public void testGet_string() {
    assertEquals(DevAppServerArgs.get("name1", "value1"), Arrays.asList("--name1", "value1"));
    assertEquals(DevAppServerArgs.get("name2", "value2"), Arrays.asList("--name2", "value2"));
  }

  @Test
  public void testGet_strings() {
    List<String> params1 = Arrays.asList("value1a", "value1b");
    List<String> params2 = Arrays.asList("value2a", "value2b");
    assertEquals(DevAppServerArgs.get("name1", params1),
        Arrays.asList("--name1", "value1a", "--name1", "value1b"));
    assertEquals(DevAppServerArgs.get("name2", params2),
        Arrays.asList("--name2", "value2a", "--name2", "value2b"));
  }

  @Test
  public void testGet_boolean() {
    assertEquals(DevAppServerArgs.get("name1", true), Collections.singletonList("--name1"));
    assertEquals(DevAppServerArgs.get("name2", true), Collections.singletonList("--name2"));

    assertEquals(DevAppServerArgs.get("name", false), Collections.emptyList());
    assertEquals(DevAppServerArgs.get("name", (Boolean) null), Collections.emptyList());
  }

  @Test
  public void testGet_integer() {
    assertEquals(DevAppServerArgs.get("name1", 1), Arrays.asList("--name1", "1"));
    assertEquals(DevAppServerArgs.get("name2", 2), Arrays.asList("--name2", "2"));
  }
}
