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

package com.google.cloud.tools.managedcloudsdk.process;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.SettableFuture;
import java.io.InputStream;
import java.util.concurrent.Callable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class AsyncStreamConsumerTest {

  @Mock private ListeningExecutorService mockExecutorService;
  @Mock private StreamConsumerFactory<String> mockStreamConsumerFactory;
  @Mock private InputStream mockInputStream;
  @Mock private Callable<String> mockCallable;
  @Mock private SettableFuture<String> mockFuture;

  @Before
  public void setupMocks() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testHandleStream() {
    Mockito.when(mockExecutorService.isShutdown()).thenReturn(false);
    Mockito.when(mockStreamConsumerFactory.newConsumer(mockInputStream)).thenReturn(mockCallable);

    new AsyncStreamConsumer<>(mockStreamConsumerFactory, mockExecutorService, mockFuture)
        .handleStream(mockInputStream);

    Mockito.verify(mockExecutorService).isShutdown();
    Mockito.verify(mockExecutorService).submit(mockCallable);
    Mockito.verify(mockExecutorService).shutdown();
    Mockito.verifyNoMoreInteractions(mockExecutorService);
  }

  @Test
  public void testHandleStream_failIfReused() {
    Mockito.when(mockExecutorService.isShutdown()).thenReturn(true);

    try {
      new AsyncStreamConsumer<>(mockStreamConsumerFactory, mockExecutorService, mockFuture)
          .handleStream(mockInputStream);
      Assert.fail("IllegalStateException expected but not thrown");
    } catch (IllegalStateException ex) {
      // pass
      Assert.assertEquals("Cannot re-use " + AsyncStreamConsumer.class.getName(), ex.getMessage());
    }
  }
}
