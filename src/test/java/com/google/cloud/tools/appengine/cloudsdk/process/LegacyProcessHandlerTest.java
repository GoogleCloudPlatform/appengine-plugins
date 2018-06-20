/*
 * Copyright 2018 Google Inc.
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

package com.google.cloud.tools.appengine.cloudsdk.process;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

import com.google.cloud.tools.appengine.cloudsdk.internal.process.WaitingProcessOutputLineListener;
import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class LegacyProcessHandlerTest {

  @Mock private ProcessOutputLineListener stdOut;
  @Mock private ProcessOutputLineListener stdErr;
  @Mock private ProcessStartListener start;
  @Mock private ProcessExitListener exit;
  @Mock private LegacyProcessHandler.Builder.DevAppServerAsyncOutputWatcherFactory watcherFactory;
  @Mock private WaitingProcessOutputLineListener watcher;
  private final List<ProcessOutputLineListener> stdOutListeners = new ArrayList<>();
  private final List<ProcessOutputLineListener> stdErrListeners = new ArrayList<>();
  private final List<ProcessStartListener> startListeners = new ArrayList<>();
  private final List<ProcessExitListener> exitListeners = new ArrayList<>();

  @Before
  public void setUp() {
    when(watcherFactory.newLineListener(anyInt())).thenReturn(watcher);
  }

  @Test
  public void testBuilder_async() {
    new LegacyProcessHandler.Builder(
            stdOutListeners, stdErrListeners, startListeners, exitListeners, watcherFactory)
        .addStdOutLineListener(stdOut)
        .addStdErrLineListener(stdErr)
        .setExitListener(exit)
        .setStartListener(start)
        .buildDevAppServerAsync(10);

    assertEquals(ImmutableList.of(stdOut, watcher), stdOutListeners);
    assertEquals(ImmutableList.of(stdErr, watcher), stdErrListeners);
    assertEquals(ImmutableList.of(start), startListeners);
    assertEquals(ImmutableList.of(exit, watcher), exitListeners);
  }

  @Test
  public void testBuilder_sync() {
    new LegacyProcessHandler.Builder(
            stdOutListeners, stdErrListeners, startListeners, exitListeners, watcherFactory)
        .addStdOutLineListener(stdOut)
        .addStdErrLineListener(stdErr)
        .setExitListener(exit)
        .setStartListener(start)
        .build();

    assertEquals(ImmutableList.of(stdOut), stdOutListeners);
    assertEquals(ImmutableList.of(stdErr), stdErrListeners);
    assertEquals(ImmutableList.of(start), startListeners);
    assertEquals(ImmutableList.of(exit), exitListeners);
  }
}
