/*
 * Copyright 2019 Google LLC.
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

package com.google.cloud.tools.appengine.configuration;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;

public class RunConfigurationTest {

  @Test
  public void testJvmFlags() {
    List<Path> services = new ArrayList<>();
    List<String> inputFlags = new ArrayList<>();
    inputFlags.add("foo");
    inputFlags.add("bar");
    
    RunConfiguration configuration = RunConfiguration
            .builder(services)
            .jvmFlags(inputFlags)
            .build();
            
    inputFlags.add("baz");
             
    List<String> flags = configuration.getJvmFlags();
    Assert.assertEquals(2, flags.size());
    Assert.assertEquals("foo", flags.get(0));
    
    flags.set(0, "baz");
    Assert.assertEquals("foo", configuration.getJvmFlags().get(0));
  }
  
  @Test
  public void testJvmFlags_unset() {
    List<Path> services = new ArrayList<>();
    
    RunConfiguration configuration = RunConfiguration
            .builder(services)
            .build();

    List<String> flags = configuration.getJvmFlags();
    Assert.assertEquals(0, flags.size());
  }
}
