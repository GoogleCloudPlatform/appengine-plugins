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

package com.google.cloud.tools.appengine.api.devserver;

import java.util.HashMap;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;

public class DefaultRunConfigurationTest {

  private DefaultRunConfiguration configuration = new DefaultRunConfiguration();
  
  @Test 
  public void testEnvironmentVariablesInitiallyEmpty() {
    Assert.assertTrue(configuration.getEnvironmentVariables().isEmpty());
  }
  
  @Test 
  public void testEnvironmentVariablesNullKey() {
    Map<String, String> environment = new HashMap<>();
    environment.put(null, "foo");
    try {
      configuration.setEnvironmentVariables(environment);
      Assert.fail();
    } catch (NullPointerException expected) {
    }
  }
  
  @Test 
  public void testEnvironmentVariablesNullValue() {
    Map<String, String> environment = new HashMap<>();
    environment.put("foo", null);
    try {
      configuration.setEnvironmentVariables(environment);
      Assert.fail();
    } catch (NullPointerException expected) {
    }
  }
  
  @Test 
  public void testEnvironmentVariables_equalsInName() {
    Map<String, String> environment = new HashMap<>();
    environment.put("foo=bar", "baz");
    try {
      configuration.setEnvironmentVariables(environment);
      Assert.fail();
    } catch (IllegalArgumentException expected) {
      Assert.assertTrue(expected.getMessage().contains("foo=bar"));
    }
  }
  
  @Test 
  public void testEnvironmentVariables_NulInName() {
    Map<String, String> environment = new HashMap<>();
    environment.put("foo\u0000", "baz");
    try {
      configuration.setEnvironmentVariables(environment);
      Assert.fail();
    } catch (IllegalArgumentException expected) {
      Assert.assertTrue(expected.getMessage().contains("foo\u0000"));
    }
  }
  
}
