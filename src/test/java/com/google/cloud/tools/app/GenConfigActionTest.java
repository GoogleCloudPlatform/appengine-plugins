/**
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
package com.google.cloud.tools.app;

import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * Unit tests for {@link GenConfigAction}.
 */
//@RunWith(MockitoJUnitRunner.class)
public class GenConfigActionTest {
/*
  @Mock
  private AppExecutor appExecutor;

  @Rule
  public TemporaryFolder tmpDir = new TemporaryFolder();

  private File source;

  @Before
  public void setUp() throws IOException {
    source = tmpDir.newFolder("source");
  }

  @Test
  public void testPrepareCommand_allFlags() throws ExecutorException {

    GenConfigConfiguration configuration = DefaultGenConfigConfiguration
        .newBuilder(source)
        .config("app.yaml")
        .custom(true)
        .runtime("java")
        .build();
    GenConfigAction action = new GenConfigAction(configuration, appExecutor);

    List<String> expected = ImmutableList
        .of("gen-config", source.toString(), "--config", "app.yaml", "--custom", "--runtime",
            "java");

    action.execute();
    verify(appExecutor, times(1)).runCommand(eq(expected));
  }

  @Test
  public void testPrepareCommand_noFlags() throws ExecutorException {

    GenConfigConfiguration configuration = DefaultGenConfigConfiguration
        .newBuilder(source)
        .build();

    GenConfigAction action = new GenConfigAction(configuration, appExecutor);

    List<String> expected = ImmutableList.of("gen-config", source.toString());

    action.execute();
    verify(appExecutor, times(1)).runCommand(eq(expected));
  }

  */
}
