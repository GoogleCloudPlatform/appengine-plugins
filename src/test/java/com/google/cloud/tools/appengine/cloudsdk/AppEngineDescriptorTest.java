/*
 * Copyright 2017 Google Inc.
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

package com.google.cloud.tools.appengine.cloudsdk;

import com.google.cloud.tools.appengine.AppEngineDescriptor;
import com.google.common.collect.ImmutableMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for {@link AppEngineDescriptorTest}.
 */
@RunWith(MockitoJUnitRunner.class)
public class AppEngineDescriptorTest {

  private AppEngineDescriptor appEngineDescriptor;
  private final Path pathToService = Paths.get("src/test/resources/projects/Standard8Project2EnvironmentVariables/WEB-INF/appengine-web.xml");
  private final File javaService = pathToService.toFile();

  @Before
  public void setUp() throws IOException {
    appEngineDescriptor = AppEngineDescriptor.parse(new FileInputStream(javaService));
  }

  @Test
  public void testParseTextValues() {
    String projectId = appEngineDescriptor.getProjectId();
    String service = appEngineDescriptor.getServiceId();
    String runtime = appEngineDescriptor.getRuntime();
    String version = appEngineDescriptor.getProjectVersion();

    assertEquals("my-app", projectId);
    assertEquals("my-service", service);
    assertEquals("java8", runtime);
    assertEquals("9", version);
  }

  @Test
  public void testParseAttributeMapValues() {
    Map<String, String> environment = appEngineDescriptor.getEnvironment();
    Map<String, String> expectedEnvironment = ImmutableMap.of(
        "keya", "vala", "key2", "duplicated-key", "keyc", "valc");

    assertEquals(expectedEnvironment, environment);
  }
}
