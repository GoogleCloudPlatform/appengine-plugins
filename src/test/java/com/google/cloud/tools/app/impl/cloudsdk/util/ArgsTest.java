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

package com.google.cloud.tools.app.impl.cloudsdk.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Unit tests for {@link Args}
 */
public class ArgsTest {

  @Test
  public void testKeyValueArgsMapping() {
    Map<String, Double> versionToTrafficSplitMapping =
        new LinkedHashMap<>(); // Preserve the order for the assertion
    versionToTrafficSplitMapping.put("v1", 0.2);
    versionToTrafficSplitMapping.put("v2", 0.3);
    versionToTrafficSplitMapping.put("v3", 0.5);

    String args = Args.keyValues(versionToTrafficSplitMapping);

    Assert.assertEquals("v1=0.2,v2=0.3,v3=0.5", args);
  }
}
