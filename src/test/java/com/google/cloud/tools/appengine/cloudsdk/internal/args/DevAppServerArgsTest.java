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

import com.google.common.collect.ImmutableMap;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.junit.Test;

/** Unit tests for {@link DevAppServerArgs} */
public class DevAppServerArgsTest {

  @Test
  public void testGet_string() {
    assertEquals(Arrays.asList("--name1=value1"), DevAppServerArgs.get("name1", "value1"));
    assertEquals(Arrays.asList("--name2=value2"), DevAppServerArgs.get("name2", "value2"));

    assertEquals(Arrays.asList("--host=value1"), DevAppServerArgs.get("host", "value1"));
  }

  @Test
  public void testGet_strings() {
    List<String> params1 = Arrays.asList("value1a", "value1b");
    List<String> params2 = Arrays.asList("value2a", "value2b");
    assertEquals(
        Arrays.asList("--name1=value1a", "--name1=value1b"),
        DevAppServerArgs.get("name1", params1));
    assertEquals(
        Arrays.asList("--name2=value2a", "--name2=value2b"),
        DevAppServerArgs.get("name2", params2));
  }

  @Test
  public void testGet_boolean() {
    assertEquals(Collections.singletonList("--name1=true"), DevAppServerArgs.get("name1", true));
    assertEquals(Collections.singletonList("--name2=true"), DevAppServerArgs.get("name2", true));

    assertEquals(Collections.singletonList("--name=false"), DevAppServerArgs.get("name", false));
    assertEquals(Collections.emptyList(), DevAppServerArgs.get("name", (Boolean) null));
  }

  @Test
  public void testGet_integer() {
    assertEquals(Arrays.asList("--name1=1"), DevAppServerArgs.get("name1", 1));
    assertEquals(Arrays.asList("--name2=2"), DevAppServerArgs.get("name2", 2));

    assertEquals(Arrays.asList("--port=8080"), DevAppServerArgs.get("port", 8080));
  }

  @Test
  public void testGet_flaggedKeyValues() {
    Map<String, String> keyValues = ImmutableMap.of("key1", "val1", "key2", "val2");

    assertEquals(
        Arrays.asList("--name", "key1=val1", "--name", "key2=val2"),
        DevAppServerArgs.get("name", keyValues));
  }
}
