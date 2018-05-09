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

package com.google.cloud.tools.managedcloudsdk.command;

import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.SettableFuture;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.concurrent.Callable;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class AsyncByteConsumerTest {

  private static final String TEST_STRING = "test line1\ntest line2\n";
  private InputStream fakeInputStream = new ByteArrayInputStream(TEST_STRING.getBytes());

  @Mock private ListeningExecutorService mockExecutorService;
  @Mock private ByteHandler mockByteHandler;
  @Mock private InputStream mockInputStream;
  @Mock private SettableFuture<String> mockFuture;

  @Test
  public void testHandleStream() {
    Mockito.when(mockExecutorService.isShutdown()).thenReturn(false);

    new AsyncByteConsumer(mockByteHandler, mockExecutorService, mockFuture)
        .handleStream(mockInputStream);

    Mockito.verify(mockExecutorService).isShutdown();
    Mockito.verify(mockExecutorService).submit(Mockito.any(Callable.class));
    Mockito.verify(mockExecutorService).shutdown();
    Mockito.verifyNoMoreInteractions(mockExecutorService);
  }

  @Test
  public void testHandleStream_failIfReused() {
    Mockito.when(mockExecutorService.isShutdown()).thenReturn(true);

    try {
      new AsyncByteConsumer(mockByteHandler, mockExecutorService, mockFuture)
          .handleStream(mockInputStream);
      Assert.fail("IllegalStateException expected but not thrown");
    } catch (IllegalStateException ex) {
      // pass
      Assert.assertEquals("Cannot re-use " + AsyncByteConsumer.class.getName(), ex.getMessage());
    }
  }

  @Test
  public void testConsumeBytes() throws Exception {

    new AsyncByteConsumer(mockByteHandler, mockExecutorService, mockFuture)
        .consumeBytes(fakeInputStream);

    ArgumentCaptor<byte[]> bytes = ArgumentCaptor.forClass(byte[].class);
    ArgumentCaptor<Integer> nBytes = ArgumentCaptor.forClass(Integer.class);
    Mockito.verify(mockByteHandler, Mockito.atLeastOnce()).bytes(bytes.capture(), nBytes.capture());

    int count = bytes.getAllValues().size();
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < count; i++) {
      result.append(new String(bytes.getAllValues().get(i), 0, nBytes.getAllValues().get(i)));
    }
    Assert.assertEquals(TEST_STRING, result.toString());

    Mockito.verify(mockByteHandler).getResult();
    Mockito.verifyNoMoreInteractions(mockByteHandler);
  }
}
